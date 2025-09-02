package com.ucb.smartpark.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ucb.smartpark.features.auth.presentation.LoginScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        // Limpia el stack para que el usuario no pueda volver al login con el botón de atrás
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(route = Screen.Home.route) {
            // pantalla principal de la app (mapa de parqueos, etc.)
            // Por ahora, solo un placeholder:
            // HomeScreen()
        }
    }
}