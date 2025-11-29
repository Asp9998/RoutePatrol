package com.aryanspatel.routepatrol.data.remote.mapper

import com.aryanspatel.routepatrol.data.local.entity.TripEntity
import com.aryanspatel.routepatrol.data.remote.dtos.TripDto
import com.aryanspatel.routepatrol.domain.model.Trip

fun TripDto.toEntity(): TripEntity = TripEntity(
    tripId = tripId,
    fleetCode = fleetCode,
    driverId = driverId,
    driverName = driverName,
    vehicle = vehicle,
    startedAt = startedAt,
    endedAt = endedAt,
    isActive = isActive,
    lastLat = lastLat,
    lastLng = lastLng,
    lastLocationTimestamp = lastLocationTimestamp
)

fun TripEntity.toDomain(): Trip = Trip(
    id = tripId,
    fleetCode = fleetCode,
    driverId = driverId,
    driverName = driverName,
    vehicle = vehicle,
    startedAt = startedAt,
    endedAt = endedAt,
    isActive = isActive,
    lastLat = lastLat,
    lastLng = lastLng,
    lastLocationTimeStamp = lastLocationTimestamp
)