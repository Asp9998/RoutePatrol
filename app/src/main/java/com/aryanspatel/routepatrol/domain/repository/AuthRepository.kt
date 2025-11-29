package com.aryanspatel.routepatrol.domain.repository

interface AuthRepository {
    suspend fun ensureLoggedIn(): String
}