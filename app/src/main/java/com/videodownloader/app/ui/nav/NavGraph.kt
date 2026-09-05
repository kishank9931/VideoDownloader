package com.videodownloader.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.videodownloader.app.MainViewModel
import com.videodownloader.app.ui.screens.FilesScreen
import com.videodownloader.app.ui.screens.HistoryScreen
import com.videodownloader.app.ui.screens.HomeScreen

private sealed class Tab(val route: String, val label: String) {
    object Home : Tab("home", "Download")
    object History : Tab("history", "History")
    object Files : Tab("files", "Files")
}

private val tabs = listOf(Tab.Home, Tab.History, Tab.Files)

@Composable
fun AppNavGraph(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination

                tabs.forEach { tab ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == tab.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                when (tab) {
                                    Tab.Home -> Icons.Filled.Download
                                    Tab.History -> Icons.Filled.History
                                    Tab.Files -> Icons.Filled.Folder
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Home.route) {
                HomeScreen(viewModel)
            }
            composable(Tab.History.route) {
                HistoryScreen(viewModel)
            }
            composable(Tab.Files.route) {
                FilesScreen()
            }
        }
    }
}
