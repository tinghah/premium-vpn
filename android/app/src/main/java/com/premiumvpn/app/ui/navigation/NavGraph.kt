package com.premiumvpn.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.premiumvpn.app.ui.screens.HomeScreen
import com.premiumvpn.app.ui.screens.KeyInputScreen
import com.premiumvpn.app.ui.screens.LoginScreen
import com.premiumvpn.app.ui.screens.StatsScreen

object Routes {
    const val HOME = "home"
    const val ADD_KEY = "add_key"
    const val LOGIN = "login"
    const val STATS = "stats/{keyId}"

    fun stats(keyId: String) = "stats/$keyId"
}

@Composable
fun PremiumNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddKey = { navController.navigate(Routes.ADD_KEY) },
                onLogin = { navController.navigate(Routes.LOGIN) },
                onKeyClick = { keyId -> navController.navigate(Routes.stats(keyId)) }
            )
        }

        composable(Routes.ADD_KEY) {
            KeyInputScreen(
                onBack = { navController.popBackStack() },
                onKeyAdded = { navController.popBackStack() }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.STATS,
            arguments = listOf(navArgument("keyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val keyId = backStackEntry.arguments?.getString("keyId") ?: return@composable
            StatsScreen(
                keyId = keyId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
