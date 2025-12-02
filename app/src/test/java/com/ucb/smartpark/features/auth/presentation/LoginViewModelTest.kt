package com.ucb.smartpark.features.auth.presentation

import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ucb.smartpark.MainDispatcherRule
import com.ucb.smartpark.features.auth.domain.usecase.LoginUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loginUseCase: LoginUseCase = mockk()
    private lateinit var viewModel: LoginViewModel

    @Test
    fun `onSignInClick should emit launchGoogleSignIn event`() = runTest {
        // Arrange
        viewModel = LoginViewModel(loginUseCase)

        // Act & Assert
        // Turbine escucha el flujo de eventos
        viewModel.launchGoogleSignIn.test {
            viewModel.onSignInClick()

            // Esperamos que emita Unit
            assertEquals(Unit, awaitItem())
        }
    }

    @Test
    fun `onGoogleSignInSuccess should update state to loading then success`() = runTest {
        // Arrange
        viewModel = LoginViewModel(loginUseCase)
        val account = mockk<GoogleSignInAccount>()

        // 💡 CAMBIO CLAVE: Usamos coAnswers y agregamos un delay(10)
        // Esto simula que la red tarda un poco, permitiendo capturar el estado "Cargando"
        coEvery { loginUseCase(account) } coAnswers {
            kotlinx.coroutines.delay(100) // Simula espera de red
            Result.success(Unit)
        }

        // Act & Assert
        viewModel.state.test {
            // 1. Estado inicial
            val initialState = awaitItem()
            assertEquals(false, initialState.isLoading)

            // Acción
            viewModel.onGoogleSignInSuccess(account)

            // 2. Estado Loading = true (Ahora sí lo atraparemos porque hay delay)
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)

            // 3. Estado Final (Loading = false, Error = null)
            val finalState = awaitItem()
            assertEquals(false, finalState.isLoading)
            assertNull(finalState.error)
        }
    }

    @Test
    fun `onGoogleSignInSuccess should emit loginEvent on success`() = runTest {
        // Arrange
        viewModel = LoginViewModel(loginUseCase)
        val account = mockk<GoogleSignInAccount>()
        coEvery { loginUseCase(account) } returns Result.success(Unit)

        // Act & Assert (Probamos el evento de navegación)
        viewModel.loginEvent.test {
            viewModel.onGoogleSignInSuccess(account)
            assertEquals(Unit, awaitItem())
        }
    }

    @Test
    fun `onGoogleSignInSuccess should show error if UseCase fails`() = runTest {
        // Arrange
        viewModel = LoginViewModel(loginUseCase)
        val account = mockk<GoogleSignInAccount>()
        val errorMsg = "El correo debe ser institucional (@ucb.edu.bo)"

        // 💡 CAMBIO CLAVE: Delay antes del fallo
        coEvery { loginUseCase(account) } coAnswers {
            kotlinx.coroutines.delay(100)
            Result.failure(Exception(errorMsg))
        }

        // Act & Assert
        viewModel.state.test {
            awaitItem() // Inicial

            viewModel.onGoogleSignInSuccess(account)

            // Ahora sí nos da tiempo de ver el loading
            val loadingState = awaitItem()
            assertEquals(true, loadingState.isLoading)

            val errorState = awaitItem() // Loading = false, con Error
            assertEquals(false, errorState.isLoading)
            assertEquals(errorMsg, errorState.error)
        }
    }
}