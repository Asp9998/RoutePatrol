package com.aryanspatel.routepatrol.data.remote.mapper

import com.aryanspatel.routepatrol.data.local.entity.TripLocationEntity
import com.aryanspatel.routepatrol.data.remote.dtos.TripLocationDto
import com.aryanspatel.routepatrol.domain.model.TripLocation

fun TripLocationDto.toEntity(): TripLocationEntity = TripLocationEntity(
    tripId = tripId,
    lat = lat,
    lng = lng,
    timestamp = timeStamp
)

fun TripLocationEntity.toDomain(): TripLocation = TripLocation(
    tripId = tripId,
    lat = lat,
    lng = lng,
    timestamp = timestamp
)