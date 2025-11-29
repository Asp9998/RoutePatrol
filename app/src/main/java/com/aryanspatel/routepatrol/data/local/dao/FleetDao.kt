package com.aryanspatel.routepatrol.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.aryanspatel.routepatrol.data.local.entity.FleetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FleetDao {
    @Upsert
    suspend fun upsertFleet(fleet: FleetEntity)

    @Upsert
    suspend fun upsertFleets(fleets: List<FleetEntity>)

    @Query("SELECT * FROM fleets WHERE code = :code LIMIT 1")
    suspend fun getFleetByCode(code: String): FleetEntity?

    @Query("SELECT * FROM fleets ORDER BY created_at DESC")
    fun observeFleets(): Flow<List<FleetEntity>>

    @Delete
    suspend fun deleteFleet(fleet: FleetEntity)

    @Query("DELETE FROM fleets")
    suspend fun clearFleets()
}