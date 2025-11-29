package com.aryanspatel.routepatrol.data.remote.dtos

data class TripLocationDto(
    val tripId: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val timeStamp: Long = 0L,
)
