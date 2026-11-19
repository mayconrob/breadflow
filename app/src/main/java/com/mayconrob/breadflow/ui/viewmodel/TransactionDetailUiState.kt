package com.mayconrob.breadflow.ui.viewmodel

import androidx.annotation.StringRes
import com.mayconrob.breadflow.domain.enums.TransactionType
import com.mayconrob.breadflow.domain.model.Category
import com.mayconrob.breadflow.domain.model.Tag

data class TransactionDetailUiState(
    val transactionId: Long = 0L,
    val description: String = "",
    val amountText: String = "",
    val type: TransactionType? = null,
    val category: Category? = null,
    val note: String = "",
    val dateTimestamp: Long = 0L,
    val createdAtTimestamp: Long = 0L,
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    @field:StringRes val errorMessage: Int? = null,
)
