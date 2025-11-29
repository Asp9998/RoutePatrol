package com.aryanspatel.routepatrol.domain.repository

import com.aryanspatel.routepatrol.domain.model.Trip
import com.aryanspatel.routepatrol.domain.model.TripLocation
import com.aryanspatel.routepatrol.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    suspend fun startTrip(
        fleetCode: String,
        driverProfile: UserProfile
    ): Trip

    suspend fun endTrip(
        fleetCode: String,
        tripId: String
    )

    suspend fun updateLastLocation(
        fleetCode: String,
        tripId: String,
        lat: Double,
        lng: Double,
        timestamp: Long
    )

    suspend fun addTripLocation(
        fleetCode: String,
        tripId: String,
        lat: Double,
        lng: Double,
        timestamp: Long
    )

    fun observeActiveTrip(
        fleetCode: String,
        driverId: String
    ): Flow<Trip?>

    fun observeLocationsForTrip(
        tripId: String
    ): Flow<List<TripLocation>>


}
