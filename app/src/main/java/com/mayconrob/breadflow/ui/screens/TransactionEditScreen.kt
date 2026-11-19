package com.mayconrob.breadflow.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
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
import com.mayconrob.breadflow.ui.viewmodel.TransactionEditEvent
import com.mayconrob.breadflow.ui.viewmodel.TransactionEditUiState
import com.mayconrob.breadflow.ui.viewmodel.TransactionEditViewModel

@Composable
fun TransactionEditRoute(
    onBackClick: () -> Unit,
    viewModel: TransactionEditViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val allTags by viewModel.allAvailableTags.collectAsState()

    TransactionEditScreen(
        state = state,
        categories = categories,
        availableTags = allTags,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditScreen(
    state: TransactionEditUiState,
    categories: List<Category>,
    availableTags: List<Tag>,
    onEvent: (TransactionEditEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    val handleBackNavigation = {
        showExitConfirmationDialog = true
    }

    BackHandler(enabled = true) {
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
                        text = stringResource(R.string.label_edit_transaction),
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
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentIndigo)
            }
        } else {
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
                    onTypeSelected = { onEvent(TransactionEditEvent.OnTypeChanged(it)) }
                )

                // 2. Campo do Valor ("Hero Amount")
                HeroAmountField(
                    amountText = state.amountText,
                    onAmountChanged = { onEvent(TransactionEditEvent.OnAmountChanged(it)) },
                    accentColor = dynamicAccentColor
                )

                // 3. Campo Descrição
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { onEvent(TransactionEditEvent.OnDescriptionChanged(it)) },
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
                    onCategorySelected = { onEvent(TransactionEditEvent.OnCategorySelected(it)) },
                    isEnabled = state.type != null
                )

                // 5. Data da Transação
                TransactionDatePickerRow(
                    timestamp = state.dateTimestamp,
                    onDateSelected = { onEvent(TransactionEditEvent.OnDateSelected(it)) }
                )

                // 6. Seção Expansível (Observação e Tags)
                TransactionExtraOptionsSection(
                    note = state.note,
                    onNoteChanged = { onEvent(TransactionEditEvent.OnNoteChanged(it)) },
                    tags = state.tags,
                    newTagInput = state.newTagInput,
                    onNewTagInputChanged = { onEvent(TransactionEditEvent.OnNewTagInputChanged(it)) },
                    availableTags = availableTags,
                    onAddTag = { onEvent(TransactionEditEvent.OnAddTag(it)) },
                    onRemoveTag = { onEvent(TransactionEditEvent.OnRemoveTag(it)) }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 7. Botão Salvar
                Button(
                    onClick = { onEvent(TransactionEditEvent.OnSaveTransaction(onSuccess = onBackClick)) },
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
    }

    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = { onEvent(TransactionEditEvent.OnDismissError) },
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
                TextButton(onClick = { onEvent(TransactionEditEvent.OnDismissError) }) {
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
