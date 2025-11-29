package com.aryanspatel.routepatrol.data.repository

import com.aryanspatel.routepatrol.data.local.dao.UserProfileDao
import com.aryanspatel.routepatrol.domain.mapper.toDomain
import com.aryanspatel.routepatrol.domain.mapper.toEntity
import com.aryanspatel.routepatrol.domain.model.UserProfile
import com.aryanspatel.routepatrol.domain.repository.UserProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dao: UserProfileDao,
): UserProfileRepository {
    override suspend fun saveUserProfile(
        userId: String,
        name: String,
        fleetCode: String,
        role: String
    ) {
        val profile = UserProfile(
            id = userId,
            name = name,
            fleetCode = fleetCode,
            role = role
        )

        // Firestore: fleets/{code}/users/{uid}
        val data = mapOf(
            "id" to profile.id,
            "name" to profile.name,
            "fleetCode" to profile.fleetCode,
            "role" to profile.role
        )

        firestore.collection("fleets")
            .document(fleetCode)
            .collection("users")
            .document(userId)
            .set(data)
            .await()

        // Local cache
        dao.upsertUserProfile(profile.toEntity())
    }

    override suspend fun getUserProfile(userId: String): UserProfile? {
        val local = dao.getUserProfileById(userId)?.toDomain()
        // For Day 1, we only rely on local. Remote load (without fleetCode) can come later.
        return local
    }
}