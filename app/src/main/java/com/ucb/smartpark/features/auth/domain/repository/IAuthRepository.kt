package com.ucb.smartpark.features.auth.domain.repository

// La interfaz define qué acciones se pueden realizar, pero no cómo se hacen.
interface IAuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
}