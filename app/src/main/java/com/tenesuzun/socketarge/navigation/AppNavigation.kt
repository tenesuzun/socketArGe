package com.tenesuzun.socketarge.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tenesuzun.socketarge.ui.screen.ChatScreen
import com.tenesuzun.socketarge.ui.screen.DirectMessageScreen
import com.tenesuzun.socketarge.ui.screen.NotificationScreen

sealed class Screen(val route: String) {
    object Notifications : Screen("notifications")
    object Chat : Screen("chat")
    object DirectMessage : Screen("direct_message")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Notifications.route
    ) {
        composable(Screen.Notifications.route) {
            NotificationScreen(
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onNavigateToDirectMessage = { navController.navigate(Screen.DirectMessage.route) }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DirectMessage.route) {
            DirectMessageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}