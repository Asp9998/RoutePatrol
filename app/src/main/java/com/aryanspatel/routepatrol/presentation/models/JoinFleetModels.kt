package com.aryanspatel.routepatrol.presentation.models

import com.aryanspatel.routepatrol.domain.model.Fleet
import com.aryanspatel.routepatrol.domain.model.UserRole

data class JoinFleetUiState(
    val userName: String = "",
    val fleetCode: String = "",
    val userRole: UserRole = UserRole.DRIVER,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

sealed class JoinFleetEvent {
    data class FleetJoined(val fleet: Fleet) : JoinFleetEvent()
    data class ShowError(val message: String) : JoinFleetEvent()
}