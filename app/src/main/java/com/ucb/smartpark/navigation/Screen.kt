package com.ucb.smartpark.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home  : Screen("home_screen")      // si quieres mantenerla
    object Parking : Screen("parking_screen") // ← NUEVO
}