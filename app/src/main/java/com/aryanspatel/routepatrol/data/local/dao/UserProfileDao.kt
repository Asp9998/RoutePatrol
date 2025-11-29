package com.aryanspatel.routepatrol.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aryanspatel.routepatrol.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Upsert
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    @Upsert
    suspend fun upsertUserProfiles(profiles: List<UserProfileEntity>)

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getUserProfileById(id: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun observeUserProfileById(id: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE fleet_code = :fleetCode")
    fun observeUsersForFleet(fleetCode: String): Flow<List<UserProfileEntity>>

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteUserProfileById(id: String)

    @Query("DELETE FROM user_profiles")
    suspend fun clearUserProfiles()
}