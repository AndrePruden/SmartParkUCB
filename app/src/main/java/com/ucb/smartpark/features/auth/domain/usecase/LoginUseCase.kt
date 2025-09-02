package com.ucb.smartpark.features.auth.domain.usecase

import com.ucb.smartpark.features.auth.domain.repository.IAuthRepository

// El caso de uso contiene la lógica de negocio pura.
class LoginUseCase(private val repository: IAuthRepository) {

    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        if (!email.endsWith("@ucb.edu.bo")) {
            return Result.failure(Exception("El correo debe ser institucional (@ucb.edu.bo)"))
        }
        if (password != "parqueoUCB") {
            return Result.failure(Exception("Contraseña incorrecta"))
        }
        return repository.login(email, password)
    }
}