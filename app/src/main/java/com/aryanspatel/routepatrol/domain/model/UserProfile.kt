package com.aryanspatel.routepatrol.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val fleetCode: String,
    val role: String,
    val vehicle: String? = null
)

enum class UserRole{
    DRIVER, VIEWER
}