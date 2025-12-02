package com.ucb.smartpark.features.auth.domain.usecase

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ucb.smartpark.features.auth.domain.repository.IAuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginUseCaseTest {

    // 1. Mockeamos el repositorio (simulamos su comportamiento)
    private val repository: IAuthRepository = mockk()

    // 2. Instanciamos la clase que vamos a probar
    private val loginUseCase = LoginUseCase(repository)

    @Test
    fun `should return failure when email is null`() = runTest {
        // Arrange (Preparar)
        val account = mockk<GoogleSignInAccount>()
        every { account.email } returns null // Simulamos que no tiene email

        // Act (Actuar)
        val result = loginUseCase(account)

        // Assert (Verificar)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == "No se pudo obtener el correo de la cuenta.")
    }

    @Test
    fun `should return failure when email is not from ucb domain`() = runTest {
        // Arrange
        val account = mockk<GoogleSignInAccount>()
        every { account.email } returns "usuario@gmail.com" // Dominio incorrecto

        // Act
        val result = loginUseCase(account)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message == "El correo debe ser institucional (@ucb.edu.bo)")

        // Verificamos que NUNCA se llamó al repositorio, porque falló antes
        coVerify(exactly = 0) { repository.loginWithGoogle(any()) }
    }

    @Test
    fun `should call repository when email is valid and has token`() = runTest {
        // Arrange
        val fakeToken = "token_valido_123"
        val account = mockk<GoogleSignInAccount>()
        every { account.email } returns "estudiante@ucb.edu.bo"
        every { account.idToken } returns fakeToken

        // Simulamos que el repositorio responde ÉXITO
        coEvery { repository.loginWithGoogle(fakeToken) } returns Result.success(Unit)

        // Act
        val result = loginUseCase(account)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.loginWithGoogle(fakeToken) }
    }
}