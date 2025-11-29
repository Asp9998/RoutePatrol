package com.aryanspatel.routepatrol.domain.mapper

import com.aryanspatel.routepatrol.data.local.entity.FleetEntity
import com.aryanspatel.routepatrol.domain.model.Fleet

fun FleetEntity.toDomain(): Fleet =
    Fleet(
        code = code,
        fleetName = fleetName,
        userName = userName,
        createdAt = createdAt
    )

fun Fleet.toEntity(): FleetEntity =
    FleetEntity(
        code = code,
        fleetName = fleetName,
        userName = userName,
        createdAt = createdAt
    )