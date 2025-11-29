package com.aryanspatel.routepatrol.domain.repository

import com.aryanspatel.routepatrol.domain.model.Fleet

interface FleetRepository {
    suspend fun createFleet(fleetCode: String, fleetName: String, userName: String): Fleet
    suspend fun joinFleet(code: String): Fleet
    suspend fun getFleet(code: String): Fleet?
}