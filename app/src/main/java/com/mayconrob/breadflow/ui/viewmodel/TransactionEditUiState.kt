package com.mayconrob.breadflow.ui.viewmodel

import androidx.annotation.StringRes
import com.mayconrob.breadflow.domain.enums.TransactionType

data class TransactionEditUiState(
    val transactionId: Long = 0L,
    val description: String = "",
    val amountText: String = "",
    val type: TransactionType? = null,
    val selectedCategoryId: Long? = null,
    val note: String = "",
    val dateTimestamp: Long = System.currentTimeMillis(),
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val newTagInput: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    @param:StringRes val errorMessage: Int? = null
)
