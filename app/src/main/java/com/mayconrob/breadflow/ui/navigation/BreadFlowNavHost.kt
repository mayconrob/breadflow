package com.mayconrob.breadflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mayconrob.breadflow.ui.screens.AddTransactionRoute
import com.mayconrob.breadflow.ui.screens.CategoriesRoute
import com.mayconrob.breadflow.ui.screens.DashboardRoute
import com.mayconrob.breadflow.ui.screens.TransactionListRoute

@Composable
fun BreadFlowNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardRoute(
                onNavigateToAddTransaction = { type ->
                    navController.navigate(Screen.AddTransaction.createRoute(type))
                }
            )
        }
        composable(Screen.Extrato.route) {
            TransactionListRoute()
        }
        composable(Screen.Categorias.route) {
            CategoriesRoute()
        }
        composable(
            route = Screen.AddTransaction.ROUTE,
            arguments = listOf(
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            AddTransactionRoute(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
