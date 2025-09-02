package com.ucb.smartpark.features.auth.data.repository

import com.ucb.smartpark.features.auth.domain.repository.IAuthRepository
import kotlinx.coroutines.delay

// La implementación del repositorio. llamadas a una API o base de datos.
class AuthRepository : IAuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> {
        delay(1000)
        println("Login exitoso para el usuario: $email")
        return Result.success(Unit)
    }
}