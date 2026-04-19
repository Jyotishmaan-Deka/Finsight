package com.example.finsight.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.finsight.presentation.screens.goals.AddEditGoalScreen
import com.example.finsight.presentation.screens.goals.GoalsScreen
import com.example.finsight.presentation.screens.home.HomeScreen
import com.example.finsight.presentation.screens.insights.InsightsScreen
import com.example.finsight.presentation.screens.settings.SettingsScreen
import com.example.finsight.presentation.screens.transactions.AddEditTransactionScreen
import com.example.finsight.presentation.screens.transactions.TransactionsScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(220)) + slideInHorizontally(
                animationSpec = tween(220),
                initialOffsetX = { it / 4 }
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                animationSpec = tween(200),
                targetOffsetX = { -it / 4 }
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220)) + slideInHorizontally(
                animationSpec = tween(220),
                initialOffsetX = { -it / 4 }
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(200)) + slideOutHorizontally(
                animationSpec = tween(200),
                targetOffsetX = { it / 4 }
            )
        }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.createRoute()) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToEditTransaction = { id -> navController.navigate(Screen.AddTransaction.createRoute(id)) }
            )
        }

        composable(Screen.Transactions.route) {
            TransactionsScreen(
                onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.createRoute()) },
                onNavigateToEditTransaction = { id -> navController.navigate(Screen.AddTransaction.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddTransaction.route,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStack ->
            val transactionId = backStack.arguments?.getLong("transactionId") ?: -1L
            AddEditTransactionScreen(
                transactionId = if (transactionId == -1L) null else transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Goals.route) {
            GoalsScreen(
                onNavigateToAddGoal = { navController.navigate(Screen.AddGoal.createRoute()) },
                onNavigateToEditGoal = { id -> navController.navigate(Screen.AddGoal.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddGoal.route,
            arguments = listOf(
                navArgument("goalId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStack ->
            val goalId = backStack.arguments?.getLong("goalId") ?: -1L
            AddEditGoalScreen(
                goalId = if (goalId == -1L) null else goalId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Insights.route) {
            InsightsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
