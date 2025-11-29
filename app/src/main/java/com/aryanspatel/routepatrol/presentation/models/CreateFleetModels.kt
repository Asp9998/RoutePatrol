package com.aryanspatel.routepatrol.presentation.models

import com.aryanspatel.routepatrol.domain.model.Fleet
import com.aryanspatel.routepatrol.domain.model.UserRole

data class CreateFleetUiState(
    val userName: String = "",
    val fleetName: String = "",
    val fleetCode: String = "",
    val userRole: UserRole = UserRole.VIEWER,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

sealed class CreateFleetEvent {
    data class FleetCreated(val fleet: Fleet) : CreateFleetEvent()
    data class ShowError(val message: String) : CreateFleetEvent()
}