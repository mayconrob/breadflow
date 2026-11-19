package com.mayconrob.breadflow.ui.viewmodel

sealed interface TransactionDetailEvent {
    data object OnDismissError : TransactionDetailEvent
    data object OnShowDeleteConfirmation : TransactionDetailEvent
    data object OnDismissDeleteConfirmation : TransactionDetailEvent
    data class OnConfirmDelete(val onSuccess: () -> Unit) : TransactionDetailEvent
}
