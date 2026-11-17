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
import com.mayconrob.breadflow.ui.screens.HomeRoute
import com.mayconrob.breadflow.ui.screens.TransactionDetailRoute
import com.mayconrob.breadflow.ui.screens.TransactionEditRoute
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
            HomeRoute(
                onNavigateToAddTransaction = { type ->
                    navController.navigate(Screen.AddTransaction.createRoute(type))
                },
                onNavigateToTransactionDetail = { id ->
                    navController.navigate(Screen.TransactionDetail.createRoute(id))
                }
            )
        }
        composable(Screen.Extrato.route) {
            TransactionListRoute(
                onNavigateToEditTransaction = { id ->
                    navController.navigate(Screen.EditTransaction.createRoute(id))
                },
                onNavigateToTransactionDetail = { id ->
                    navController.navigate(Screen.TransactionDetail.createRoute(id))
                }
            )
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
        composable(
            route = Screen.EditTransaction.ROUTE,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                }
            )
        ) {
            TransactionEditRoute(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.TransactionDetail.ROUTE,
            arguments = listOf(
                navArgument("transactionId") {
                    type = NavType.LongType
                }
            )
        ) {
            TransactionDetailRoute(
                onBackClick = { navController.popBackStack() },
                onEditClick = { id ->
                    navController.navigate(Screen.EditTransaction.createRoute(id)) {
                        popUpTo(Screen.TransactionDetail.ROUTE) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
