package com.mayconrob.breadflow.ui.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayconrob.breadflow.R
import com.mayconrob.breadflow.domain.enums.TransactionType
import com.mayconrob.breadflow.domain.model.Category
import com.mayconrob.breadflow.domain.model.Tag
import com.mayconrob.breadflow.domain.model.Transaction
import com.mayconrob.breadflow.domain.repository.ICategoriaRepository
import com.mayconrob.breadflow.domain.repository.ITagRepository
import com.mayconrob.breadflow.domain.repository.ITransacaoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransactionEditViewModel @Inject constructor(
    private val transacaoRepository: ITransacaoRepository,
    private val categoriaRepository: ICategoriaRepository,
    private val tagRepository: ITagRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>("transactionId")
        ?: savedStateHandle.get<String>("transactionId")?.toLongOrNull()
        ?: 0L

    private val _uiState = MutableStateFlow(TransactionEditUiState(transactionId = transactionId))
    val uiState: StateFlow<TransactionEditUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<Category>> = categoriaRepository.all
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allAvailableTags: StateFlow<List<Tag>> = tagRepository.all
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadTransaction()
    }

    private fun loadTransaction() {
        if (transactionId <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = R.string.message_error_default
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val item = transacaoRepository.all.map { list ->
                list.find { it.transaction.id == transactionId }
            }.firstOrNull()

            if (item != null) {
                val tx = item.transaction
                val formattedAmount =
                    if (tx.amount > BigDecimal.ZERO) currencyFormatter.format(tx.amount) else ""
                _uiState.update { current ->
                    current.copy(
                        transactionId = tx.id,
                        description = tx.description,
                        amountText = formattedAmount,
                        type = tx.type,
                        selectedCategoryId = tx.categoryId,
                        note = tx.note ?: "",
                        dateTimestamp = tx.dateTimestamp,
                        createdAtTimestamp = tx.createdAtTimestamp,
                        tags = tx.tags.map { it.name },
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = R.string.message_error_default
                    )
                }
            }
        }
    }

    fun onEvent(event: TransactionEditEvent) {
        when (event) {
            is TransactionEditEvent.OnTypeChanged -> onTypeChanged(event.type)
            is TransactionEditEvent.OnDescriptionChanged -> onDescriptionChanged(event.description)
            is TransactionEditEvent.OnAmountChanged -> onAmountChanged(event.rawInput)
            is TransactionEditEvent.OnCategorySelected -> onCategorySelected(event.categoryId)
            is TransactionEditEvent.OnNoteChanged -> onNoteChanged(event.note)
            is TransactionEditEvent.OnDateSelected -> onDateSelected(event.timestamp)
            is TransactionEditEvent.OnNewTagInputChanged -> onNewTagInputChanged(event.input)
            is TransactionEditEvent.OnAddTag -> addTag(event.name)
            is TransactionEditEvent.OnRemoveTag -> removeTag(event.name)
            is TransactionEditEvent.OnDismissError -> clearError()
            is TransactionEditEvent.OnSaveTransaction -> saveTransaction(event.onSuccess)
        }
    }

    private fun onTypeChanged(type: TransactionType) {
        _uiState.update { current ->
            val currentCategory = categories.value.find { it.id == current.selectedCategoryId }
            val isSameType = currentCategory?.type == type
            current.copy(
                type = type,
                selectedCategoryId = if (isSameType) current.selectedCategoryId else null,
                errorMessage = null
            )
        }
    }

    private fun onDescriptionChanged(desc: String) {
        _uiState.update { it.copy(description = desc, errorMessage = null) }
    }

    private fun onAmountChanged(rawInput: String) {
        val digits = rawInput.filter { it.isDigit() }.take(11)
        val parsedCents = digits.toLongOrNull() ?: 0L
        val formatted = if (parsedCents == 0L) {
            ""
        } else {
            val bd = BigDecimal(parsedCents).movePointLeft(2)
            currencyFormatter.format(bd)
        }
        _uiState.update { it.copy(amountText = formatted, errorMessage = null) }
    }

    private fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, errorMessage = null) }
    }

    private fun onNoteChanged(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    private fun onDateSelected(timestamp: Long) {
        _uiState.update { it.copy(dateTimestamp = timestamp) }
    }

    private fun onNewTagInputChanged(input: String) {
        _uiState.update { it.copy(newTagInput = input) }
    }

    private fun addTag(rawName: String) {
        val trimmed = rawName.trim().removePrefix("#")
        if (trimmed.isBlank()) return

        _uiState.update { current ->
            when {
                current.tags.size >= 2 -> {
                    current.copy(errorMessage = R.string.error_max_tags_limit)
                }

                current.tags.any { it.equals(trimmed, ignoreCase = true) } -> {
                    current.copy(
                        newTagInput = "",
                        errorMessage = null
                    )
                }

                else -> {
                    current.copy(
                        tags = current.tags + trimmed,
                        newTagInput = "",
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun removeTag(tag: String) {
        _uiState.update { current ->
            current.copy(tags = current.tags.filterNot { it.equals(tag, ignoreCase = true) })
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun saveTransaction(onSuccess: () -> Unit) {
        val state = _uiState.value
        val digits = state.amountText.filter { it.isDigit() }
        val amountParsed = if (digits.isBlank()) null else BigDecimal(digits).movePointLeft(2)

        if (state.type == null) {
            _uiState.update { it.copy(errorMessage = R.string.text_select_transaction_type) }
            return
        }
        if (amountParsed == null || amountParsed <= BigDecimal.ZERO) {
            _uiState.update { it.copy(errorMessage = R.string.text_greater_than_zero) }
            return
        }
        if (state.selectedCategoryId == null) {
            _uiState.update { it.copy(errorMessage = R.string.text_select_category) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val domainTags = state.tags.take(10).map { Tag(name = it) }
            val transaction = Transaction(
                id = state.transactionId,
                description = state.description.trim(),
                amount = amountParsed,
                type = state.type,
                categoryId = state.selectedCategoryId,
                note = state.note.trim().ifBlank { null },
                dateTimestamp = state.dateTimestamp,
                createdAtTimestamp = state.createdAtTimestamp,
                tags = domainTags
            )
            transacaoRepository.update(transaction)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }

    companion object {
        private val ptBrSymbols = DecimalFormatSymbols(Locale("pt", "BR"))
        private val currencyFormatter = DecimalFormat("#,##0.00", ptBrSymbols)
    }
}
