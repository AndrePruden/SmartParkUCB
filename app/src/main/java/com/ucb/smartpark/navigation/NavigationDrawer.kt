package com.ucb.smartpark.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationDrawer(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    data object Parking : NavigationDrawer(
        label = "Parqueo",
        selectedIcon = Icons.Filled.DirectionsCar,
        unselectedIcon = Icons.Outlined.DirectionsCar,
        route = Screen.Parking.route
    )

    data object Notifications : NavigationDrawer(
        label = "Notificaciones",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications,
        route = Screen.Notifications.route
    )

    // Lo llamamos "Salir" o "Login" para que sea semántico
    data object Logout : NavigationDrawer(
        label = "Cerrar Sesión",
        selectedIcon = Icons.Filled.ExitToApp,
        unselectedIcon = Icons.Outlined.ExitToApp,
        route = Screen.Login.route
    )
}