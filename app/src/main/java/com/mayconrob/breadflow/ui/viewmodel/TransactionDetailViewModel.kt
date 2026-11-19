package com.mayconrob.breadflow.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayconrob.breadflow.R
import com.mayconrob.breadflow.common.Formatters
import com.mayconrob.breadflow.domain.model.TransactionWithCategory
import com.mayconrob.breadflow.domain.repository.ITransacaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transacaoRepository: ITransacaoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>("transactionId")
        ?: savedStateHandle.get<String>("transactionId")?.toLongOrNull()
        ?: 0L

    private val _uiState = MutableStateFlow(TransactionDetailUiState(transactionId = transactionId))
    val uiState: StateFlow<TransactionDetailUiState> = _uiState.asStateFlow()

    private var currentItem: TransactionWithCategory? = null

    init {
        loadTransaction()
    }

    private fun loadTransaction() {
        if (transactionId <= 0L) {
            _uiState.update { it.copy(isLoading = false, errorMessage = R.string.message_error_default) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val item = transacaoRepository.all.map { list ->
                list.find { it.transaction.id == transactionId }
            }.firstOrNull()

            if (item != null) {
                currentItem = item
                val tx = item.transaction
                val formattedAmount = Formatters.formatCurrency(tx.amount)
                _uiState.update { current ->
                    current.copy(
                        transactionId = tx.id,
                        description = tx.description,
                        amountText = formattedAmount,
                        type = tx.type,
                        category = item.category,
                        note = tx.note ?: "",
                        dateTimestamp = tx.dateTimestamp,
                        createdAtTimestamp = tx.createdAtTimestamp,
                        tags = tx.tags,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = R.string.message_error_default) }
            }
        }
    }

    fun onEvent(event: TransactionDetailEvent) {
        when (event) {
            is TransactionDetailEvent.OnDismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            is TransactionDetailEvent.OnShowDeleteConfirmation -> {
                _uiState.update { it.copy(showDeleteConfirmation = true) }
            }
            is TransactionDetailEvent.OnDismissDeleteConfirmation -> {
                _uiState.update { it.copy(showDeleteConfirmation = false) }
            }
            is TransactionDetailEvent.OnConfirmDelete -> {
                deleteTransaction(event.onSuccess)
            }
        }
    }

    private fun deleteTransaction(onSuccess: () -> Unit) {
        val target = currentItem?.transaction ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, showDeleteConfirmation = false) }
            try {
                transacaoRepository.delete(target)
                _uiState.update { it.copy(isDeleting = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = R.string.message_error_default
                    )
                }
            }
        }
    }
}
