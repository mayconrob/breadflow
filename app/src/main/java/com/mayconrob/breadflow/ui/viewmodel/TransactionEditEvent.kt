package com.mayconrob.breadflow.ui.viewmodel

import com.mayconrob.breadflow.domain.enums.TransactionType

sealed interface TransactionEditEvent {
    data class OnTypeChanged(val type: TransactionType) : TransactionEditEvent
    data class OnDescriptionChanged(val description: String) : TransactionEditEvent
    data class OnAmountChanged(val rawInput: String) : TransactionEditEvent
    data class OnCategorySelected(val categoryId: Long) : TransactionEditEvent
    data class OnNoteChanged(val note: String) : TransactionEditEvent
    data class OnDateSelected(val timestamp: Long) : TransactionEditEvent
    data class OnNewTagInputChanged(val input: String) : TransactionEditEvent
    data class OnAddTag(val name: String) : TransactionEditEvent
    data class OnRemoveTag(val name: String) : TransactionEditEvent
    data object OnDismissError : TransactionEditEvent
    data class OnSaveTransaction(val onSuccess: () -> Unit) : TransactionEditEvent
}
