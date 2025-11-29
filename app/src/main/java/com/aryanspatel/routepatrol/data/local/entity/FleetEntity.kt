package com.aryanspatel.routepatrol.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fleets")
data class FleetEntity(
    @PrimaryKey
    @ColumnInfo(name = "code")
    val code: String,

    @ColumnInfo(name = "fleet_name")
    val fleetName: String,

    @ColumnInfo(name = "user_name")
    val userName: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
