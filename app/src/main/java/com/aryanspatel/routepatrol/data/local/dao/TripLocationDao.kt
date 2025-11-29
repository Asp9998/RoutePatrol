package com.aryanspatel.routepatrol.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aryanspatel.routepatrol.data.local.entity.TripLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripLocationDao {
    @Upsert
    suspend fun upsertLocation(location: TripLocationEntity)

    @Query("""
        SELECT * FROM trip_locations
        WHERE trip_id = :tripId
        ORDER BY timestamp ASC
    """)
    fun observeLocationsForTrip(tripId: String): Flow<List<TripLocationEntity>>
}