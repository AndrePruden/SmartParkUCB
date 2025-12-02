package com.ucb.smartpark.features.auth.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mockeamos el ViewModel completo
    private val mockViewModel = mockk<LoginViewModel>(relaxed = true)

    @Test
    fun loginScreen_showsTitleAndButton() {
        // Arrange
        // Simulamos el estado inicial del ViewModel
        every { mockViewModel.state } returns MutableStateFlow(LoginState())

        // Act
        composeTestRule.setContent {
            // Inyectamos el mock manualmente, evitando Koin
            LoginScreen(
                onLoginSuccess = {},
                viewModel = mockViewModel
            )
        }

        // Assert
        // Verificamos que el botón existe y tiene el texto correcto
        composeTestRule.onNodeWithText("CONTINUAR CON GOOGLE")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun loginScreen_showsError_whenStateHasError() {
        // Arrange
        val errorMessage = "Error de conexión"
        every { mockViewModel.state } returns MutableStateFlow(
            LoginState(error = errorMessage)
        )

        // Act
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                viewModel = mockViewModel
            )
        }

        // Assert
        // Verificamos que el error aparece en pantalla
        composeTestRule.onNodeWithText(errorMessage)
            .assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsLoading_whenStateIsLoading() {
        // Arrange
        every { mockViewModel.state } returns MutableStateFlow(
            LoginState(isLoading = true)
        )

        // Act
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                viewModel = mockViewModel
            )
        }

        // Assert
        // 1. Verificamos que el botón existe (por su tag) y está deshabilitado
        composeTestRule.onNodeWithTag("loginButton")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        // 2. Verificamos que el indicador de carga se muestra
        composeTestRule.onNodeWithTag("loadingIndicator")
            .assertIsDisplayed()

        // 3. Opcional: Confirmamos que el texto YA NO está
        composeTestRule.onNodeWithText("CONTINUAR CON GOOGLE")
            .assertDoesNotExist()
    }

    @Test
    fun loginScreen_callsSignIn_onClick() {
        // Arrange
        every { mockViewModel.state } returns MutableStateFlow(LoginState())

        // Act
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                viewModel = mockViewModel
            )
        }

        // Click en el botón
        composeTestRule.onNodeWithText("CONTINUAR CON GOOGLE").performClick()

        // Assert
        // Verificamos que se llamó a la función del ViewModel
        io.mockk.verify { mockViewModel.onSignInClick() }
    }
}