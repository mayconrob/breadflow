package com.mayconrob.breadflow.ui.viewmodel

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
class AddTransactionViewModel @Inject constructor(
    private val transacaoRepository: ITransacaoRepository,
    private val categoriaRepository: ICategoriaRepository,
    private val tagRepository: ITagRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val initialTypeArg: String? = savedStateHandle.get<String>("type")
    private val initialType: TransactionType? = initialTypeArg?.let {
        runCatching { TransactionType.valueOf(it) }.getOrNull()
    }

    private val _uiState = MutableStateFlow(AddTransactionUiState(type = initialType))
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<Category>> = categoriaRepository.all
        .map { list ->
            list.sortedWith(
                compareBy { category ->
                    val nameLower = category.name.trim().lowercase()
                    if (nameLower.startsWith("outros") || nameLower.startsWith("outras")) 1 else 0
                }
            )
        }
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

    fun onTypeChanged(type: TransactionType) {
        _uiState.update { current ->
            current.copy(
                type = type,
                selectedCategoryId = null,
                errorMessage = null
            )
        }
    }

    fun onDescriptionChanged(desc: String) {
        _uiState.update { it.copy(description = desc, errorMessage = null) }
    }

    fun onAmountChanged(rawInput: String) {
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

    fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId, errorMessage = null) }
    }

    fun onNoteChanged(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onDateSelected(timestamp: Long) {
        _uiState.update { it.copy(dateTimestamp = timestamp) }
    }

    fun onNewTagInputChanged(input: String) {
        _uiState.update { it.copy(newTagInput = input) }
    }

    fun addTag(rawName: String) {
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

    fun removeTag(tag: String) {
        _uiState.update { current ->
            current.copy(tags = current.tags.filterNot { it.equals(tag, ignoreCase = true) })
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveTransaction(onSuccess: () -> Unit) {
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
                description = state.description.trim(),
                amount = amountParsed,
                type = state.type,
                categoryId = state.selectedCategoryId,
                note = state.note.trim().ifBlank { null },
                dateTimestamp = state.dateTimestamp,
                createdAtTimestamp = System.currentTimeMillis(),
                tags = domainTags
            )
            transacaoRepository.insert(transaction)
            _uiState.update { it.copy(isSaving = false) }
            onSuccess()
        }
    }

    companion object {
        private val ptBrSymbols = DecimalFormatSymbols(Locale("pt", "BR"))
        private val currencyFormatter = DecimalFormat("#,##0.00", ptBrSymbols)

    }
}
