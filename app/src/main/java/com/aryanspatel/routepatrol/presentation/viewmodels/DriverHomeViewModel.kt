package com.aryanspatel.routepatrol.presentation.viewmodels

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.routepatrol.data.datastore.Preferences
import com.aryanspatel.routepatrol.domain.model.UserProfile
import com.aryanspatel.routepatrol.domain.repository.AuthRepository
import com.aryanspatel.routepatrol.domain.repository.TripRepository
import com.aryanspatel.routepatrol.location.RoutePatrolLocationService
import com.aryanspatel.routepatrol.presentation.models.DriverHomeEvent
import com.aryanspatel.routepatrol.presentation.models.DriverHomeUiState
import com.aryanspatel.routepatrol.presentation.models.MapPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverHomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val repo: TripRepository,
    @ApplicationContext private val appContext: Context
): ViewModel() {

    private val _uiState = MutableStateFlow(DriverHomeUiState())
    val uiState: StateFlow<DriverHomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DriverHomeEvent>()
    val events: SharedFlow<DriverHomeEvent> = _events

    private var currentFleetCode: String? = null
    private var currentDriverId: String? = null
    private var currentTripId: String? = null

    init {
        observeSession()
    }

    private fun observeSession(){
        viewModelScope.launch {
            // suspend call – just get the current session once
            val session = Preferences.getSession(appContext)

            if (session == null) {
                // No session: you can emit an event or just return
                _events.emit(
                    DriverHomeEvent.ShowMessage("No active session. Please join or create a fleet.")
                )
                return@launch
            }

            // These vars already exist in your VM
            currentFleetCode = session.fleetCode
            currentDriverId = session.userId

            // Start observing active trip for this driver & fleet
            observeActiveTrip(
                fleetCode = session.fleetCode,
                driverId = session.userId
            )

            _uiState.update { it.copy(
                driverName = session.userName,
                vehicle = session.vehicleName,
                fleetCode = session.fleetCode
            ) }
        }
    }

    private fun observeActiveTrip(fleetCode: String, driverId: String){
        viewModelScope.launch {
            repo.observeActiveTrip(fleetCode, driverId)
                .collect { trip ->
                    if(trip == null){
                        currentTripId = null
                        _uiState.update {
                            it.copy(
                                activeTrip = null,
                                lastLat = null,
                                lastLng = null
                            )
                        }
                    } else{
                        currentTripId = trip.id
                        _uiState.update {
                            it.copy(
                                activeTrip = trip,
                                lastLat = trip.lastLat,
                                lastLng = trip.lastLng
                            )
                        }

                        observeTripLocations(trip.id)
                    }
                }
        }
    }

    private fun observeTripLocations(tripId: String){
        viewModelScope.launch {
            repo.observeLocationsForTrip(tripId)
                .collect { locations ->
                    val last = locations.lastOrNull()
                    if(last != null){
                        _uiState.update {
                            it.copy(
                                lastLat = last.lat,
                                lastLng = last.lng,
                                pathPoints = locations.map { loc ->
                                    MapPoint(loc.lat, loc.lng)
                                }
                            )
                        }
                    }
                }
        }
    }

    fun onLocationPermissionChanged(granted: Boolean){
        _uiState.update { it.copy(hasLocationPermission = granted) }

        val state = _uiState.value
        if(granted && state.isRequestingStartTrip) {
            startTripInternal()
        } else if(!granted && state.isRequestingStartTrip){
            viewModelScope.launch {
                _events.emit(
                    DriverHomeEvent.ShowMessage(
                        "Location permission is required to track the trip."
                    )
                )
            }
            _uiState.update { it.copy(isRequestingStartTrip = false) }
        }
    }

    fun onStartTripClicked() {
        val state = _uiState.value

        if(state.activeTrip != null){
            viewModelScope.launch {
                _events.emit(DriverHomeEvent.ShowMessage("Trip is already running."))
            }
            return
        }

        if(!state.hasLocationPermission){
            viewModelScope.launch {
                _events.emit(DriverHomeEvent.RequestLocationPermission)
            }
            _uiState.update { it.copy(isRequestingStartTrip = true) }
            return
        }

        startTripInternal()
    }

    private fun startTripInternal(){
        val fleetCode =  currentFleetCode
        val driverId = currentDriverId

        if(fleetCode == null || driverId == null){
            viewModelScope.launch {
                _events.emit(DriverHomeEvent.ShowMessage("Missing fleet or driver info."))
            }
            _uiState.update { it.copy(isRequestingStartTrip = false) }
            return
        }
        
        viewModelScope.launch {
            authRepository.ensureLoggedIn()
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val session = Preferences.getSession(appContext)
                if (session == null) {
                    _events.emit(DriverHomeEvent.ShowMessage("No active session. Please join or create a fleet."))
                    return@launch
                }

                val trip = repo.startTrip(
                    fleetCode = fleetCode,
                    driverProfile = UserProfile(
                        id = session.userId,
                        name = session.userName,
                        fleetCode = session.fleetCode,
                        role = session.role.name,
                        vehicle = session.vehicleName
                    )
                )

                startLocationService(
                    fleetCode = fleetCode,
                    tripId = trip.id
                )

                currentTripId = trip.id

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRequestingStartTrip = false,
                        activeTrip = trip
                    )
                }

                _events.emit(DriverHomeEvent.ShowMessage("Trip started."))
            } catch (e: Exception){
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRequestingStartTrip = false,
                        errorMessage = e.message ?: "Failed to start trip."
                    )
                }
                _events.emit(DriverHomeEvent.ShowMessage("Failed to start trip. $e"))
            }
        }
    }

    fun onEndTripClicked(context: Context){
        val fleetCode = currentFleetCode
        val tripId = currentTripId

        if(fleetCode == null || tripId == null){
            viewModelScope.launch {
                _events.emit(DriverHomeEvent.ShowMessage("No active trip to end."))
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update{it.copy(isLoading = true, errorMessage = null)}

                repo.endTrip(
                    fleetCode = fleetCode,
                    tripId = tripId
                )

                stopLocationService()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        activeTrip = null,
                        lastLat = null,
                        lastLng = null
                    )
                }

                _events.emit(DriverHomeEvent.ShowMessage("Trip ended."))

            } catch (e: Exception){
                _uiState.update {
                    it.copy (
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to end trip"
                    )
                }
                _events.emit((DriverHomeEvent.ShowMessage("Failed to end trip.")))
            }

        }
    }

    private fun startLocationService(fleetCode: String, tripId: String){
        val intent = RoutePatrolLocationService.createStartIntent(
            context = appContext,
            fleetCode = fleetCode,
            tripId = tripId
        )
        ContextCompat.startForegroundService(appContext, intent)
    }

    private fun stopLocationService(){
        val intent = RoutePatrolLocationService.createStopIntent(appContext)
        appContext.startService(intent)
    }

}