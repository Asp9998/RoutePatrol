package com.aryanspatel.routepatrol.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aryanspatel.routepatrol.domain.model.UserRole
import com.aryanspatel.routepatrol.domain.repository.AuthRepository
import com.aryanspatel.routepatrol.domain.repository.FleetRepository
import com.aryanspatel.routepatrol.presentation.models.JoinFleetEvent
import com.aryanspatel.routepatrol.presentation.models.JoinFleetUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinFleetViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val fleetRepository: FleetRepository
): ViewModel(){

    private val _uiState = MutableStateFlow(JoinFleetUiState())
    val uiState: StateFlow<JoinFleetUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<JoinFleetEvent>()
    val events: SharedFlow<JoinFleetEvent> = _events.asSharedFlow()


    fun onUserNameeChanged(userName: String) {
        _uiState.update {
            it.copy(
                userName = userName.trim(), // normalize if you want
                errorMessage = null
            )
        }
    }

    fun onFleetCodeChanged(code: String) {
        _uiState.update {
            it.copy(
                fleetCode = code.uppercase().trim(), // normalize if you want
                errorMessage = null
            )
        }
    }

    fun onRoleChanged(role: UserRole){
        _uiState.update {
            it.copy(
                userRole = role,
                errorMessage = null
            )
        }
    }

    fun onJoinClicked() {
        val current = _uiState.value
        val code = current.fleetCode.trim()
        val userName = current.userName.trim()

        if (code.isEmpty() || userName.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Credentials code cannot be empty")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            try {
                // Ensure Firebase user exists (anon sign-in if needed)
                authRepository.ensureLoggedIn()

                // Try to join the fleet from Firestore + cache in Room
                val fleet = fleetRepository.joinFleet(code)

                _uiState.update { it.copy(isSubmitting = false) }
                _events.emit(JoinFleetEvent.FleetJoined(fleet))
            } catch (e: IllegalArgumentException) {
                val message = e.message ?: "Fleet not found"
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = message,
                    )
                }
                _events.emit(JoinFleetEvent.ShowError(message))
            } catch (e: Exception) {
                val message = e.message ?: "Failed to join fleet"
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = message
                    )
                }
                _events.emit(JoinFleetEvent.ShowError(message))
            }
        }
    }
}