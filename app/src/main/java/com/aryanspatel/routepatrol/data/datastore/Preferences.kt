package com.aryanspatel.routepatrol.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aryanspatel.routepatrol.domain.model.UserRole
import kotlinx.coroutines.flow.first

object Preferences {
    private val Context.dataStore by preferencesDataStore("prefs")
    private val USER_ID = stringPreferencesKey("user_id")
    private val USER_NAME = stringPreferencesKey("user_name")
    private val FLEET_CODE = stringPreferencesKey("fleet_code")
    private val ROLE = stringPreferencesKey("role")
    private val FLEET_NAME = stringPreferencesKey("fleet_name")
    private val VEHICLE_NAME = stringPreferencesKey("vehicle_name")

    suspend fun setSession(context: Context, session: UserSession) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = session.userId
            prefs[USER_NAME] = session.userName
            prefs[FLEET_CODE] = session.fleetCode
            prefs[ROLE] = session.role.name

            if (session.fleetName != null) {
                prefs[FLEET_NAME] = session.fleetName
            } else {
                prefs.remove(FLEET_NAME)
            }
            if (session.vehicleName != null) {
                prefs[VEHICLE_NAME] = session.vehicleName
            } else {
                prefs.remove(VEHICLE_NAME)
            }
        }
    }

    suspend fun clearSession(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun getSession(context: Context): UserSession? {
        val prefs = context.dataStore.data.first() // suspends until first value

        val userId = prefs[USER_ID]
        val userName = prefs[USER_NAME]
        val fleetCode = prefs[FLEET_CODE]
        val roleRaw = prefs[ROLE]
        val fleetName = prefs[FLEET_NAME]
        val vehicleName = prefs[VEHICLE_NAME]

        if (userId.isNullOrBlank() ||
            userName.isNullOrBlank() ||
            fleetCode.isNullOrBlank() ||
            roleRaw.isNullOrBlank()
        ) {
            return null
        }

        val role = runCatching { UserRole.valueOf(roleRaw) }.getOrNull() ?: return null

        return UserSession(
            userId = userId,
            fleetCode = fleetCode,
            fleetName = fleetName ?: "Fleet $fleetCode",
            userName = userName,
            vehicleName = vehicleName,
            role = role
        )
    }


}

data class UserSession(
    val userId: String,
    val fleetCode: String,
    val fleetName: String? = null,
    val userName: String,
    val vehicleName: String? = null,
    val role: UserRole
)