package com.example.finsight.presentation.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Transactions : Screen("transactions")
    object AddTransaction : Screen("add_transaction?transactionId={transactionId}") {
        fun createRoute(transactionId: Long? = null) =
            if (transactionId != null) "add_transaction?transactionId=$transactionId"
            else "add_transaction?transactionId=-1"
    }
    object Goals : Screen("goals")
    object AddGoal : Screen("add_goal?goalId={goalId}") {
        fun createRoute(goalId: Long? = null) =
            if (goalId != null) "add_goal?goalId=$goalId"
            else "add_goal?goalId=-1"
    }
    object Insights : Screen("insights")
    object Settings : Screen("settings")
}
