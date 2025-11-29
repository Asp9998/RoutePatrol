package com.aryanspatel.routepatrol.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.routepatrol.data.datastore.Preferences
import com.aryanspatel.routepatrol.data.datastore.UserSession
import com.aryanspatel.routepatrol.domain.model.UserRole
import com.aryanspatel.routepatrol.domain.repository.AuthRepository
import com.aryanspatel.routepatrol.domain.repository.FleetRepository
import com.aryanspatel.routepatrol.domain.usecase.FleetCodeGenerator
import com.aryanspatel.routepatrol.presentation.models.CreateFleetEvent
import com.aryanspatel.routepatrol.presentation.models.CreateFleetUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateFleetViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val fleetRepository: FleetRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(CreateFleetUiState())
    val uiState: StateFlow<CreateFleetUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CreateFleetEvent>()
    val events: SharedFlow<CreateFleetEvent> = _events.asSharedFlow()

    fun onFleetNameChanged(name: String) {
        _uiState.update {
            it.copy(
                fleetName = name,
                errorMessage = null
            )
        }
    }

    fun onGenerateFleetCode() {
        val code = FleetCodeGenerator.generateFleetCode()
        _uiState.update {
            it.copy(
                fleetCode = code,
                errorMessage = null
            )
        }
    }

    fun onUserNameChanged(name: String) {
        _uiState.update {
            it.copy(
                userName = name,
                errorMessage = null
            )
        }
    }

    fun onRoleChanged(role: UserRole) {
        _uiState.update {
            it.copy(
                userRole = role,
                errorMessage = null
            )
        }
    }

    fun onSaveSession(context: Context){
        val userId = UUID.randomUUID().toString()
        val state = _uiState.value
        viewModelScope.launch {
            Preferences.setSession(
                context = context,
                session = UserSession(
                    userId = userId,
                    fleetCode = state.fleetCode,
                    fleetName = state.fleetName.trim(),
                    userName = state.userName.trim(),
                    role = state.userRole
                )
            )
        }
    }

    fun onCreateFleetClicked() {
        val current = _uiState.value
        val trimmedUserName = current.userName.trim()
        val trimmedFleetName = current.fleetName.trim()
        val fleetCode = current.fleetCode


        // Simple validation
        if (trimmedUserName.isEmpty() || trimmedFleetName.isEmpty() || fleetCode.isEmpty()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Credentials cannot be empty"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            try {
                // Ensure we have a Firebase user (anonymous if needed)
                authRepository.ensureLoggedIn()

                // Create fleet in Firestore + cache in Room
                val fleet = fleetRepository.createFleet(fleetCode = fleetCode, fleetName = trimmedFleetName, userName = trimmedUserName)

                _uiState.update { it.copy(isSubmitting = false) }

                _events.emit(CreateFleetEvent.FleetCreated(fleet))
            } catch (e: Exception) {
                val message = e.message ?: "Failed to create fleet"
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = message
                    )
                }
                _events.emit(CreateFleetEvent.ShowError(message))
            }
        }
    }


}