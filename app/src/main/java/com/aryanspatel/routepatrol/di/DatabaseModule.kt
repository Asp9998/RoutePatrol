package com.aryanspatel.routepatrol.di

import android.content.Context
import androidx.room.Room
import com.aryanspatel.routepatrol.data.local.ApplicationDatabase
import com.aryanspatel.routepatrol.data.local.dao.FleetDao
import com.aryanspatel.routepatrol.data.local.dao.TripDao
import com.aryanspatel.routepatrol.data.local.dao.TripLocationDao
import com.aryanspatel.routepatrol.data.local.dao.UserProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context) : ApplicationDatabase =
        Room.databaseBuilder(ctx, ApplicationDatabase::class.java, "application_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideFleetDao(db: ApplicationDatabase) : FleetDao =
        db.fleetDao()

    @Provides @Singleton
    fun provideUserProfileDao(db: ApplicationDatabase) : UserProfileDao =
        db.userProfileDao()

    @Provides @Singleton
    fun provideTripDao(db: ApplicationDatabase): TripDao =
        db.tripDao()

    @Provides @Singleton
    fun provideTripLocationDao(db: ApplicationDatabase): TripLocationDao =
        db.tripLocationDao()
}
