package com.aryanspatel.routepatrol.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles",
        foreignKeys = [
            ForeignKey(
                entity = FleetEntity::class,
                parentColumns = ["code"],
                childColumns = ["fleet_code"],
                onDelete = ForeignKey.CASCADE
            )
],
    indices = [Index("fleet_code")]
)
data class UserProfileEntity(
    @PrimaryKey

    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "fleet_code")
    val fleetCode: String,

    @ColumnInfo(name = "role")
    val role: String
)