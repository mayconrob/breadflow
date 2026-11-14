package com.mayconrob.breadflow.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayconrob.breadflow.R
import com.mayconrob.breadflow.domain.enums.TransactionType
import com.mayconrob.breadflow.domain.model.Category
import com.mayconrob.breadflow.domain.model.Tag
import com.mayconrob.breadflow.ui.components.CategoryDropdownField
import com.mayconrob.breadflow.ui.components.HeroAmountField
import com.mayconrob.breadflow.ui.components.TransactionDatePickerRow
import com.mayconrob.breadflow.ui.components.TransactionExtraOptionsSection
import com.mayconrob.breadflow.ui.components.TransactionTypeSelector
import com.mayconrob.breadflow.ui.theme.AccentIndigo
import com.mayconrob.breadflow.ui.theme.DarkCardBorder
import com.mayconrob.breadflow.ui.theme.DarkSurface
import com.mayconrob.breadflow.ui.theme.ExpenseRed
import com.mayconrob.breadflow.ui.theme.IncomeGreen
import com.mayconrob.breadflow.ui.viewmodel.AddTransactionUiState
import com.mayconrob.breadflow.ui.viewmodel.AddTransactionViewModel

@Composable
fun AddTransactionRoute(
    onBackClick: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val allTags by viewModel.allAvailableTags.collectAsState()

    AddTransactionScreen(
        state = state,
        categories = categories,
        availableTags = allTags,
        onTypeChanged = viewModel::onTypeChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
        onAmountChanged = viewModel::onAmountChanged,
        onCategorySelected = viewModel::onCategorySelected,
        onNoteChanged = viewModel::onNoteChanged,
        onDateSelected = viewModel::onDateSelected,
        onNewTagInputChanged = viewModel::onNewTagInputChanged,
        onAddTag = viewModel::addTag,
        onRemoveTag = viewModel::removeTag,
        onDismissError = viewModel::clearError,
        onSaveClick = { viewModel.saveTransaction(onSuccess = onBackClick) },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    state: AddTransactionUiState,
    categories: List<Category>,
    availableTags: List<Tag>,
    onTypeChanged: (TransactionType) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onCategorySelected: (Long) -> Unit,
    onNoteChanged: (String) -> Unit,
    onDateSelected: (Long) -> Unit,
    onNewTagInputChanged: (String) -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    onDismissError: () -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges = state.type != null ||
        state.amountText.isNotBlank() ||
        state.description.isNotBlank() ||
        state.selectedCategoryId != null ||
        state.note.isNotBlank() ||
        state.tags.isNotEmpty() ||
        state.newTagInput.isNotBlank()

    val handleBackNavigation = {
        if (hasUnsavedChanges) {
            showExitConfirmationDialog = true
        } else {
            onBackClick()
        }
    }

    // Intercepta botão voltar físico/sistema e gestos nativos do Android
    BackHandler(enabled = hasUnsavedChanges) {
        showExitConfirmationDialog = true
    }

    val dynamicAccentColor = when (state.type) {
        TransactionType.ENTRY -> IncomeGreen
        TransactionType.EXIT -> ExpenseRed
        null -> MaterialTheme.colorScheme.onSurface
    }

    val filteredCategories = remember(categories, state.type) {
        if (state.type == null) {
            emptyList()
        } else {
            categories
                .filter { it.type == state.type }
                .sortedWith(
                    compareBy { category ->
                        val nameLower = category.name.trim().lowercase()
                        if (nameLower.startsWith(context.getString(R.string.text_other_male)) ||
                            nameLower.startsWith(context.getString(R.string.text_other_female))
                        ) 1 else 0
                    }
                )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.text_new_transaction),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBackNavigation) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.text_cancel)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Seletor de Tipo (Entrada vs Saída)
            TransactionTypeSelector(
                selectedType = state.type,
                onTypeSelected = onTypeChanged
            )

            // 2. Campo do Valor ("Hero Amount")
            HeroAmountField(
                amountText = state.amountText,
                onAmountChanged = onAmountChanged,
                accentColor = dynamicAccentColor
            )

            // 3. Campo Descrição
            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChanged,
                label = { Text(stringResource(R.string.label_description_optional)) },
                placeholder = { Text(stringResource(R.string.placeholder_add_transaction_description)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentIndigo,
                    unfocusedBorderColor = DarkCardBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Campo Categoria (Dropdown customizado)
            CategoryDropdownField(
                categories = filteredCategories,
                selectedCategoryId = state.selectedCategoryId,
                onCategorySelected = onCategorySelected,
                isEnabled = state.type != null
            )

            // 5. Data da Transação
            TransactionDatePickerRow(
                timestamp = state.dateTimestamp,
                onDateSelected = onDateSelected
            )

            // 6. Seção Expansível (Observação e Tags)
            TransactionExtraOptionsSection(
                note = state.note,
                onNoteChanged = onNoteChanged,
                tags = state.tags,
                newTagInput = state.newTagInput,
                onNewTagInputChanged = onNewTagInputChanged,
                availableTags = availableTags,
                onAddTag = onAddTag,
                onRemoveTag = onRemoveTag
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Botão Salvar
            Button(
                onClick = onSaveClick,
                enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentIndigo
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.text_save_transaction),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = {
                Text(
                    text = stringResource(R.string.title_error_default),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = stringResource(id = state.errorMessage),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissError) {
                    Text(stringResource(R.string.text_understood))
                }
            }
        )
    }

    if (showExitConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmationDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.title_exit_transaction_confirmation),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.message_exit_transaction_confirmation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = { showExitConfirmationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentIndigo)
                ) {
                    Text(stringResource(R.string.action_continue))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitConfirmationDialog = false
                        onBackClick()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_go_back),
                        color = ExpenseRed
                    )
                }
            }
        )
    }
}
