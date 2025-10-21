package com.ucb.smartpark.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import com.ucb.smartpark.navigation.NavigationDrawer
import com.ucb.smartpark.navigation.AppNavigation
import com.ucb.smartpark.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController: NavHostController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val items = listOf(
        NavigationDrawer.Profile,
        NavigationDrawer.Dollar
    )

    // Detectar ruta actual
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 🔹 Si estamos en login, no mostrar Drawer ni AppBar
    if (currentRoute == Screen.Login.route) {
        AppNavigation(navController = navController)
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(260.dp)) {
                    Box(
                        modifier = Modifier.width(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "SmartPark", style = MaterialTheme.typography.titleMedium)
                    }

                    items.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("SmartPark") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menú")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                AppNavigation(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
