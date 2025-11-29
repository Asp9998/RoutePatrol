package com.aryanspatel.routepatrol.domain.mapper

import com.aryanspatel.routepatrol.data.local.entity.UserProfileEntity
import com.aryanspatel.routepatrol.domain.model.UserProfile

fun UserProfileEntity.toDomain(): UserProfile =
    UserProfile(
        id = id,
        name = name,
        fleetCode = fleetCode,
        role = role
    )

fun UserProfile.toEntity(): UserProfileEntity =
    UserProfileEntity(
        id = id,
        name = name,
        fleetCode = fleetCode,
        role = role
    )