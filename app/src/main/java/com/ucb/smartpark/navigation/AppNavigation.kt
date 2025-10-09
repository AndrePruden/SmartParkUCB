package com.ucb.smartpark.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ucb.smartpark.features.auth.presentation.LoginScreen
import com.ucb.smartpark.features.parking.presentation.ParkingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.Parking.route   // ← arranca en Parking
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Parking.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ← Asegúrate de registrar esta ruta!
        composable(Screen.Parking.route) {
            ParkingScreen()
        }

        // (Opcional) si mantienes Home:
        composable(Screen.Home.route) {
            ParkingScreen() // o tu HomeScreen contenedor
        }
    }
}
