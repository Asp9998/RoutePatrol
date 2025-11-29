package com.aryanspatel.routepatrol.presentation.models

import com.aryanspatel.routepatrol.domain.model.Trip

data class DriverHomeUiState(
    val driverName: String = "",
    val vehicle: String? = null,
    val fleetCode: String = "",
    val isLoading: Boolean = false,
    val hasLocationPermission: Boolean = false,

    val activeTrip: Trip? = null,
    val lastLat: Double? = null,
    val lastLng: Double? = null,

    val pathPoints: List<MapPoint> = emptyList(),

    val errorMessage: String? = null,
    val isRequestingStartTrip: Boolean = false, // we tried to start, waiting for permission
)

data class MapPoint(
    val lat: Double,
    val lng: Double
)

sealed interface DriverHomeEvent {
    object RequestLocationPermission : DriverHomeEvent
    data class ShowMessage(val message: String) : DriverHomeEvent
}
