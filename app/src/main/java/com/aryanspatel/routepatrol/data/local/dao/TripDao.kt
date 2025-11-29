package com.aryanspatel.routepatrol.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aryanspatel.routepatrol.data.local.entity.TripEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Upsert
    suspend fun upsertTrip(trip: TripEntity)

    @Query("UPDATE trips SET is_active = :isActive, ended_at = :endedAt WHERE trip_id = :tripId")
    suspend fun updateTripStatus(tripId: String, isActive: Boolean, endedAt: Long?)

    @Query("""
        UPDATE trips
        SET last_lat = :lat,
            last_lng = :lng,
            last_location_timestamp = :timestamp
        WHERE trip_id = :tripId
    """)
    suspend fun updateLastLocation(
        tripId: String,
        lat: Double,
        lng: Double,
        timestamp: Long
    )

    @Query("""
        SELECT * FROM trips
        WHERE fleet_code = :fleetCode AND driver_id = :driverId AND is_active = 1
        LIMIT 1
    """)
    fun observeActiveTrip(
        fleetCode: String,
        driverId: String
    ): Flow<TripEntity?>
}