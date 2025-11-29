package com.aryanspatel.routepatrol.data.repository

import com.aryanspatel.routepatrol.data.local.dao.FleetDao
import com.aryanspatel.routepatrol.domain.mapper.toDomain
import com.aryanspatel.routepatrol.domain.mapper.toEntity
import com.aryanspatel.routepatrol.domain.model.Fleet
import com.aryanspatel.routepatrol.domain.repository.FleetRepository
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Singleton
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.random.Random

@Singleton
class FleetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dao: FleetDao
): FleetRepository {

    override suspend fun createFleet(fleetCode: String, fleetName: String, userName: String): Fleet {

        val createdAt = System.currentTimeMillis()
        val fleet = Fleet(
            code = fleetCode,
            fleetName = fleetName,
            userName = userName,
            createdAt = createdAt
        )

        // Firestore: fleets/{code}
        val data = mapOf(
            "code" to fleet.code,
            "fleetName" to fleet.fleetName,
            "userName" to fleet.userName,
            "createdAt" to fleet.createdAt
        )

        firestore.collection("fleets")
            .document(fleetCode)
            .set(data)
            .await()

        dao.upsertFleet(fleet.toEntity())
        return fleet
    }

    override suspend fun joinFleet(code: String): Fleet {
        val snapshot = firestore.collection("fleets")
            .document(code)
            .get()
            .await()

        if (!snapshot.exists()) {
            throw IllegalArgumentException("Fleet with code $code not found.")
        }

        val fleetName = snapshot.getString("fleetName") ?: "Unnamed Fleet"
        val userName = snapshot.getString("userName") ?: "Unnamed Fleet"
        val createdAt = snapshot.getLong("createdAt") ?: 0L

        val fleet = Fleet(
            code = code,
            fleetName = fleetName,
            userName = userName,
            createdAt = createdAt
        )

        dao.upsertFleet(fleet.toEntity())

        return fleet
    }

    override suspend fun getFleet(code: String): Fleet? {
        // First try local
        val local = dao.getFleetByCode(code)?.toDomain()
        if (local != null) return local

        // try remote
        val snapshot = firestore.collection("fleets")
            .document(code)
            .get()
            .await()

        if (!snapshot.exists()) return null

        val fleetName = snapshot.getString("fleetName") ?: "Unnamed Fleet"
        val userName = snapshot.getString("userName") ?: "Unnamed Fleet"
        val createdAt = snapshot.getLong("createdAt") ?: 0L

        val fleet = Fleet(
            code = code,
            fleetName = fleetName,
            userName = userName,
            createdAt = createdAt
        )

        // Save locally for next time
        dao.upsertFleet(fleet.toEntity())

        return fleet
    }
}