package com.ucb.smartpark.features.auth.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ucb.smartpark.features.auth.domain.repository.IAuthRepository

// El caso de uso contiene la lógica de negocio pura.
class LoginUseCase(private val repository: IAuthRepository) {

    suspend operator fun invoke(account: GoogleSignInAccount): Result<Unit> {
        val email = account.email
        if (email == null) {
            return Result.failure(Exception("No se pudo obtener el correo de la cuenta."))
        }

        // 🚀 ¡AQUÍ ESTÁ LA VALIDACIÓN DE DOMINIO!
        if (!email.endsWith("@ucb.edu.bo")) {
            return Result.failure(Exception("El correo debe ser institucional (@ucb.edu.bo)"))
        }

        // Si es válido, obtenemos el token y se lo pasamos al repositorio
        val idToken = account.idToken
        if (idToken == null) {
            return Result.failure(Exception("No se pudo obtener el token de Google."))
        }

        return repository.loginWithGoogle(idToken)
    }
}