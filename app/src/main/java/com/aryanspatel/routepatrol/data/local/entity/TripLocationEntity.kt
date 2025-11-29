package com.aryanspatel.routepatrol.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "trip_locations",
    primaryKeys = ["trip_id", "timestamp"],
    indices = [Index("trip_id")])
data class TripLocationEntity (

    @ColumnInfo(name = "trip_id")
    val tripId: String,

    @ColumnInfo(name = "lat")
    val lat: Double,

    @ColumnInfo(name = "lng")
    val lng: Double,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,
//    val speed: Float?,
//    val bearing: Float?
)
