package com.mayconrob.breadflow.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.ui.graphics.vector.ImageVector
import com.mayconrob.breadflow.R
import com.mayconrob.breadflow.domain.enums.TransactionType

sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    data object Dashboard : Screen(
        route = "dashboard",
        titleRes = R.string.text_home,
        icon = Icons.Default.Dashboard
    )

    data object Extrato : Screen(
        route = "extrato",
        titleRes = R.string.text_bank_statement,
        icon = Icons.AutoMirrored.Filled.ListAlt
    )

    data object Categorias : Screen(
        route = "categorias",
        titleRes = R.string.text_categories,
        icon = Icons.Default.Category
    )


    data object AddTransaction {
        const val ROUTE = "add_transaction?type={type}"
        fun createRoute(type: TransactionType? = null): String {
            return if (type != null) "add_transaction?type=${type.name}" else "add_transaction"
        }
    }

    data object EditTransaction {
        const val ROUTE = "edit_transaction/{transactionId}"
        fun createRoute(transactionId: Long): String {
            return "edit_transaction/$transactionId"
        }
    }

    data object TransactionDetail {
        const val ROUTE = "transaction_detail/{transactionId}"
        fun createRoute(transactionId: Long): String {
            return "transaction_detail/$transactionId"
        }
    }

    companion object {
        val bottomNavItems = listOf(Dashboard, Extrato, Categorias)
    }
}
