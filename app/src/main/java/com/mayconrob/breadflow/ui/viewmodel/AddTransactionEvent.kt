package com.mayconrob.breadflow.ui.viewmodel

import com.mayconrob.breadflow.domain.enums.TransactionType

sealed interface AddTransactionEvent {
    data class OnTypeChanged(val type: TransactionType) : AddTransactionEvent
    data class OnDescriptionChanged(val description: String) : AddTransactionEvent
    data class OnAmountChanged(val rawInput: String) : AddTransactionEvent
    data class OnCategorySelected(val categoryId: Long) : AddTransactionEvent
    data class OnNoteChanged(val note: String) : AddTransactionEvent
    data class OnDateSelected(val timestamp: Long) : AddTransactionEvent
    data class OnNewTagInputChanged(val input: String) : AddTransactionEvent
    data class OnAddTag(val name: String) : AddTransactionEvent
    data class OnRemoveTag(val name: String) : AddTransactionEvent
    data object OnDismissError : AddTransactionEvent
    data class OnSaveTransaction(val onSuccess: () -> Unit) : AddTransactionEvent
}
