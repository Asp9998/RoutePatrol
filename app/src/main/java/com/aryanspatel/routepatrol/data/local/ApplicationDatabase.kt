package com.aryanspatel.routepatrol.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aryanspatel.routepatrol.data.local.dao.FleetDao
import com.aryanspatel.routepatrol.data.local.dao.TripDao
import com.aryanspatel.routepatrol.data.local.dao.TripLocationDao
import com.aryanspatel.routepatrol.data.local.dao.UserProfileDao
import com.aryanspatel.routepatrol.data.local.entity.FleetEntity
import com.aryanspatel.routepatrol.data.local.entity.TripEntity
import com.aryanspatel.routepatrol.data.local.entity.TripLocationEntity
import com.aryanspatel.routepatrol.data.local.entity.UserProfileEntity

@Database(entities = [
    FleetEntity::class,
    UserProfileEntity::class,
    TripEntity::class,
    TripLocationEntity::class
                     ],
    version = 2)
abstract class ApplicationDatabase : RoomDatabase() {

    abstract fun fleetDao(): FleetDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun tripDao(): TripDao
    abstract fun tripLocationDao(): TripLocationDao
}