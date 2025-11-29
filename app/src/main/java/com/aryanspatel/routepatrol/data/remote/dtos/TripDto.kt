package com.aryanspatel.routepatrol.data.remote.dtos

data class TripDto(
    val tripId: String = "",
    val fleetCode: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val vehicle: String? = null,
    val startedAt: Long = 0L,
    val endedAt: Long? = null,
    val isActive: Boolean = true,

    val lastLat: Double? = null,
    val lastLng: Double? = null,
    val lastLocationTimestamp: Long? = null
)