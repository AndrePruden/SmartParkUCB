package com.ucb.smartpark.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Login
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationDrawer(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String) {


    data object Profile : NavigationDrawer("Profile", Icons.Filled.Login,
        Icons.Outlined.Login, Screen.Login.route
    )
    data object Dollar : NavigationDrawer("Parking",
        Icons.Filled.Home, Icons.Outlined.Home,
        Screen.Parking.route)

}
