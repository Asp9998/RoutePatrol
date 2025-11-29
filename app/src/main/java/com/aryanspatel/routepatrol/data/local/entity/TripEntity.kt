package com.aryanspatel.routepatrol.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "trips",
    indices = [Index("fleet_code"), Index("driver_id")])
data class TripEntity(
    @PrimaryKey
    @ColumnInfo(name = "trip_id")
    val tripId: String,

    @ColumnInfo(name = "fleet_code")
    val fleetCode: String,

    @ColumnInfo(name = "driver_id")
    val driverId: String,

    @ColumnInfo(name = "driver_name")
    val driverName: String,

    @ColumnInfo(name = "vehicle")
    val vehicle: String?,

    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    @ColumnInfo(name = "ended_at")
    val endedAt: Long?,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,

    @ColumnInfo(name = "last_lat")
    val lastLat: Double?,

    @ColumnInfo(name = "last_lng")
    val lastLng: Double?,

    @ColumnInfo(name = "last_location_timestamp")
    val lastLocationTimestamp: Long?
)
