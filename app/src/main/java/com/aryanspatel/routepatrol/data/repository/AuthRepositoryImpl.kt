package com.aryanspatel.routepatrol.data.repository

import com.aryanspatel.routepatrol.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun ensureLoggedIn(): String {
        val current = auth.currentUser
        if (current != null) {
            return current.uid
        }

        val result = auth.signInAnonymously().await()
        val user = result.user ?: error("Anonymous sign-in failed")
        return user.uid
    }
}