package com.aryanspatel.routepatrol.domain.repository

interface LocationTracker {
    fun startTracking(tripId: String, fleetCode: String, driverId: String)
    fun stopTracking()
}