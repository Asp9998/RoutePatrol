package com.aryanspatel.routepatrol.di

import com.aryanspatel.routepatrol.data.repository.AuthRepositoryImpl
import com.aryanspatel.routepatrol.data.repository.FleetRepositoryImpl
import com.aryanspatel.routepatrol.data.repository.TripRepositoryImpl
import com.aryanspatel.routepatrol.data.repository.UserProfileRepositoryImpl
import com.aryanspatel.routepatrol.domain.repository.AuthRepository
import com.aryanspatel.routepatrol.domain.repository.FleetRepository
import com.aryanspatel.routepatrol.domain.repository.TripRepository
import com.aryanspatel.routepatrol.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds @Singleton
    abstract fun bindFleetRepository(
        impl: FleetRepositoryImpl
    ): FleetRepository

    @Binds @Singleton
    abstract fun bindUserRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds @Singleton
    abstract fun bindTripRepository(
        impl: TripRepositoryImpl
    ): TripRepository


}
