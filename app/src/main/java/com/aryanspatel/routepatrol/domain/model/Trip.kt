package com.aryanspatel.routepatrol.domain.model

data class Trip (
    val id: String,
    val fleetCode: String,
    val driverId: String,
    val driverName: String,
    val vehicle: String?,
    val startedAt: Long,
    val endedAt: Long?,
    val isActive: Boolean,
    val lastLat: Double? = null,
    val lastLng: Double? = null,
    val lastLocationTimeStamp: Long? = null
)

data class TripLocation(
    val tripId: String,
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)