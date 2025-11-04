package com.ucb.smartpark.features.auth.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ucb.smartpark.features.auth.domain.repository.IAuthRepository
import kotlinx.coroutines.tasks.await

// La implementación del repositorio. llamadas a una API o base de datos.
class AuthRepository(
    private val auth: FirebaseAuth
) : IAuthRepository {

    override suspend fun loginWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}