package com.aryanspatel.routepatrol.data.repository

import com.aryanspatel.routepatrol.data.local.dao.TripDao
import com.aryanspatel.routepatrol.data.local.dao.TripLocationDao
import com.aryanspatel.routepatrol.data.remote.dtos.TripDto
import com.aryanspatel.routepatrol.data.remote.dtos.TripLocationDto
import com.aryanspatel.routepatrol.data.remote.mapper.toDomain
import com.aryanspatel.routepatrol.data.remote.mapper.toEntity
import com.aryanspatel.routepatrol.domain.model.Trip
import com.aryanspatel.routepatrol.domain.model.TripLocation
import com.aryanspatel.routepatrol.domain.model.UserProfile
import com.aryanspatel.routepatrol.domain.repository.TripRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.String

@Singleton
class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao,
    private val tripLocationDao: TripLocationDao,
    private val db: FirebaseFirestore
): TripRepository {
    override suspend fun startTrip(
        fleetCode: String,
        driverProfile: UserProfile
    ): Trip = withContext(Dispatchers.IO) {
        val tripRef = tripsCollection(fleetCode)
        val newDoc = tripRef.document()
        val tripId = newDoc.id
        val now = System.currentTimeMillis()

        val dto = TripDto(
            tripId = tripId,
            fleetCode = fleetCode,
            driverId = driverProfile.id,
            driverName = driverProfile.name,
            vehicle = driverProfile.vehicle,
            startedAt = now,
            isActive = true
        )

        newDoc.set(dto).await()

        tripDao.upsertTrip(dto.toEntity())

        dto.toEntity().toDomain()
    }

    override suspend fun endTrip(fleetCode: String, tripId: String) =
        withContext(Dispatchers.IO){
            val now = System.currentTimeMillis()
            tripDoc(fleetCode, tripId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "endedAt" to now
                    )
                ).await()

            tripDao.updateTripStatus(tripId, isActive = false, endedAt = now)
        }

    override suspend fun updateLastLocation(
        fleetCode: String,
        tripId: String,
        lat: Double,
        lng: Double,
        timestamp: Long
    ) = withContext(Dispatchers.IO){
        val updates = mapOf(
            "lastLat" to lat,
            "lastLng" to lng,
            "lastLocationTimeStamp" to timestamp
        )

        tripDoc(fleetCode, tripId).update(updates).await()
        tripDao.updateLastLocation(tripId, lat, lng, timestamp)
    }

    override suspend fun addTripLocation(
        fleetCode: String,
        tripId: String,
        lat: Double,
        lng: Double,
        timestamp: Long
    ) = withContext(Dispatchers.IO){
        val dto = TripLocationDto(
            tripId = tripId,
            lat = lat,
            lng = lng,
            timeStamp = timestamp
        )

        locationsCollection(fleetCode, tripId)
            .add(dto)
            .await()

        tripLocationDao.upsertLocation(
            dto.toEntity()
        )
    }

    override fun observeActiveTrip(fleetCode: String, driverId: String): Flow<Trip?> {
        return tripDao.observeActiveTrip(fleetCode, driverId)
            .map { entity -> entity?.toDomain() }
            .flowOn(Dispatchers.IO)
    }

    override fun observeLocationsForTrip(tripId: String): Flow<List<TripLocation>> {
        return tripLocationDao.observeLocationsForTrip(tripId)
            .map { entities ->
                entities.map { entity ->
                    TripLocation(
                        tripId = entity.tripId,
                        lat = entity.lat,
                        lng = entity.lng,
                        timestamp = entity.timestamp
                    )
                }
            }.flowOn(Dispatchers.IO)
    }

    private fun fleetsCollection() =
        db.collection("fleets")

    private fun tripsCollection(fleetCode: String) =
        fleetsCollection().document(fleetCode).collection("trips")

    private fun tripDoc(fleetCode: String, tripId: String) =
        tripsCollection(fleetCode).document(tripId)

    private fun locationsCollection(fleetCode: String, tripId: String) =
        tripDoc(fleetCode, tripId).collection("locations")

}