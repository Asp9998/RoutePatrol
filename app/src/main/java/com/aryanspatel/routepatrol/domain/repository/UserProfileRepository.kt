package com.aryanspatel.routepatrol.domain.repository

import com.aryanspatel.routepatrol.domain.model.UserProfile

interface UserProfileRepository {
    suspend fun saveUserProfile(
        userId: String,
        name: String,
        fleetCode: String,
        role: String
    )
    suspend fun getUserProfile(userId: String): UserProfile?
}