package com.example.plantastic.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.plantastic.ui.components.BottomNavigationBar
import com.example.plantastic.ui.screens.CameraScreen
import com.example.plantastic.ui.screens.ChatScreen
import com.example.plantastic.ui.screens.HistoryScreen
import com.example.plantastic.ui.screens.HomeScreen
import com.example.plantastic.ui.screens.ProfileScreen
import com.example.plantastic.ui.screens.ResultScreen
import com.example.plantastic.ui.screens.ScanResultDetailScreen
import com.example.plantastic.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object Result : Screen("result/{imageUri}") {
        fun createRoute(imageUri: Uri): String = "result/${Uri.encode(imageUri.toString())}"
    }
    data object ScanResultDetail : Screen("scan_result/{scanId}") {
        fun createRoute(scanId: String): String = "scan_result/$scanId"
    }
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object Settings : Screen("settings")
    data object Chat : Screen("chat/{scanId}") {
        fun createRoute(scanId: String): String = "chat/$scanId"
    }
}

@Composable
fun NavigationGraph(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.History.route,
        Screen.Profile.route
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onScanClick = {
                        navController.navigate(Screen.Camera.route)
                    },
                    onHistoryItemClick = { scanId ->
                        navController.navigate(Screen.ScanResultDetail.createRoute(scanId))
                    },
                    onChatClick = { scanId ->
                        navController.navigate(Screen.Chat.createRoute(scanId))
                    }
                )
            }

            composable(Screen.Camera.route) {
                CameraScreen(
                    onImageCaptured = { uri ->
                        navController.navigate(Screen.Result.createRoute(uri)) {
                            popUpTo(Screen.Camera.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.Result.route,
                arguments = listOf(
                    navArgument("imageUri") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val imageUriString = backStackEntry.arguments?.getString("imageUri") ?: ""
                val imageUri = Uri.parse(Uri.decode(imageUriString))

                ResultScreen(
                    imageUri = imageUri,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onGoHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToChat = { scanId ->
                        navController.navigate(Screen.Chat.createRoute(scanId))
                    }
                )
            }

            composable(
                route = Screen.ScanResultDetail.route,
                arguments = listOf(
                    navArgument("scanId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
                ScanResultDetailScreen(
                    scanId = scanId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onGoHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToChat = {
                        navController.navigate(Screen.Chat.createRoute(scanId))
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    onItemClick = { scanId ->
                        navController.navigate(Screen.ScanResultDetail.createRoute(scanId))
                    },
                    onChatClick = { scanId ->
                        navController.navigate(Screen.Chat.createRoute(scanId))
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("scanId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val scanId = backStackEntry.arguments?.getString("scanId") ?: ""
                ChatScreen(
                    scanId = scanId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
