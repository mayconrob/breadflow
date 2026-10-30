package com.mayconrob.breadflow.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import com.mayconrob.breadflow.R
import com.mayconrob.breadflow.common.Formatters
import com.mayconrob.breadflow.domain.enums.TransactionType
import com.mayconrob.breadflow.domain.model.Category
import com.mayconrob.breadflow.domain.model.Tag
import com.mayconrob.breadflow.ui.theme.AccentIndigo
import com.mayconrob.breadflow.ui.theme.DarkCardBorder
import com.mayconrob.breadflow.ui.theme.DarkSurface
import com.mayconrob.breadflow.ui.theme.ExpenseRed
import com.mayconrob.breadflow.ui.theme.IncomeGreen
import com.mayconrob.breadflow.ui.viewmodel.AddTransactionUiState
import com.mayconrob.breadflow.ui.viewmodel.AddTransactionViewModel
import java.util.Calendar

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val focusManager = LocalFocusManager.current

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    val categoryListScrollState = rememberScrollState()
    var showMoreOptions by remember { mutableStateOf(state.note.isNotBlank() || state.tags.isNotEmpty()) }

    LaunchedEffect(state.note.isNotBlank(), state.tags.isNotEmpty()) {
        if (state.note.isNotBlank() || state.tags.isNotEmpty()) {
            showMoreOptions = true
        }
    }

    var amountTextFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = state.amountText, selection = TextRange(state.amountText.length)
            )
        )
    }

    LaunchedEffect(state.amountText) {
        if (amountTextFieldValue.text != state.amountText) {
            amountTextFieldValue = TextFieldValue(
                text = state.amountText, selection = TextRange(state.amountText.length)
            )
        }
    }

    val isEntry = state.type == TransactionType.ENTRY
    val isExit = state.type == TransactionType.EXIT

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

    val selectedCategory = remember(categories, state.selectedCategoryId) {
        categories.find { it.id == state.selectedCategoryId }
    }

    val calendar = remember(state.dateTimestamp) {
        Calendar.getInstance().apply { timeInMillis = state.dateTimestamp }
    }

    val datePickerDialog = remember(context, state.dateTimestamp) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                onDateSelected(newCal.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val (startToday, endToday) = remember { Formatters.getTodayRange() }
    val isToday = state.dateTimestamp in startToday..endToday
    val formattedDateText = if (isToday) {
        stringResource(R.string.text_today_date, Formatters.formatShortDate(state.dateTimestamp))
    } else {
        Formatters.formatShortDate(state.dateTimestamp)
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
                }, navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.text_cancel)
                        )
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }, containerColor = MaterialTheme.colorScheme.background, modifier = modifier.fillMaxSize()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pílula Entrada
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isEntry) IncomeGreen.copy(alpha = 0.18f) else DarkSurface)
                        .border(
                            width = if (isEntry) 2.dp else 1.dp,
                            color = if (isEntry) IncomeGreen else DarkCardBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onTypeChanged(TransactionType.ENTRY) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isEntry) IncomeGreen else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.5f
                            ),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.text_entry),
                            fontWeight = if (isEntry) FontWeight.Bold else FontWeight.Medium,
                            color = if (isEntry) IncomeGreen else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.7f
                            ),
                            fontSize = 14.sp
                        )
                    }
                }

                // Pílula Saída
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isExit) ExpenseRed.copy(alpha = 0.18f) else DarkSurface)
                        .border(
                            width = if (isExit) 2.dp else 1.dp,
                            color = if (isExit) ExpenseRed else DarkCardBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onTypeChanged(TransactionType.EXIT) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = if (isExit) ExpenseRed else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.5f
                            ),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.text_exit),
                            fontWeight = if (isExit) FontWeight.Bold else FontWeight.Medium,
                            color = if (isExit) ExpenseRed else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.7f
                            ),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 2. Campo do Valor ("Hero Amount") com destaque visual e comportamento estilo banco
            OutlinedTextField(
                value = amountTextFieldValue, onValueChange = { newValue ->
                    if (newValue.text != amountTextFieldValue.text) {
                        onAmountChanged(newValue.text)
                    } else {
                        // Mantém o cursor sempre na extremidade direita após os centavos
                        amountTextFieldValue =
                            newValue.copy(selection = TextRange(newValue.text.length))
                    }
                }, placeholder = {
                    Text(
                        text = stringResource(R.string.label_zero_cents),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                }, leadingIcon = {
                    Text(
                        text = stringResource(id = R.string.currency_symbol),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = dynamicAccentColor
                        ),
                        modifier = Modifier.padding(start = 12.dp, end = 2.dp)
                    )
                }, trailingIcon = {
                    if (state.amountText.isNotEmpty()) {
                        IconButton(onClick = { onAmountChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear_value),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }, textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold, fontSize = 32.sp, color = dynamicAccentColor
                ), singleLine = true, keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                ), shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = dynamicAccentColor.copy(alpha = 0.6f),
                    unfocusedBorderColor = DarkCardBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ), modifier = Modifier.fillMaxWidth()
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

            // 4. Campo Categoria (Seletor inline ancorado diretamente no fluxo)
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = state.type != null,
                        label = { Text(stringResource(R.string.label_category)) },
                        placeholder = {
                            Text(
                                if (state.type == null) stringResource(R.string.message_select_type_first)
                                else stringResource(R.string.Select_a_category)
                            )
                        },
                        leadingIcon = if (selectedCategory != null) {
                            {
                                val catColor = try {
                                    Color(selectedCategory.colorHex.toColorInt())
                                } catch (e: Exception) {
                                    MaterialTheme.colorScheme.primary
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                            }
                        } else null,
                        trailingIcon = {
                            if (state.type != null) {
                                IconButton(onClick = {
                                    isCategoryDropdownExpanded = !isCategoryDropdownExpanded
                                }) {
                                    Icon(
                                        imageVector = if (isCategoryDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentIndigo,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            disabledBorderColor = DarkCardBorder,
                            disabledContainerColor = DarkSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.3f
                            ),
                            disabledTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.type != null) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    isCategoryDropdownExpanded = !isCategoryDropdownExpanded
                                }
                        )
                    }
                }

                // Lista de categorias expansível ancorada diretamente abaixo do campo
                AnimatedVisibility(
                    visible = isCategoryDropdownExpanded && state.type != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .heightIn(max = 240.dp)
                            .drawWithContent {
                                drawContent()
                                if (categoryListScrollState.maxValue > 0) {
                                    val totalHeight = size.height
                                    val progress =
                                        categoryListScrollState.value.toFloat() / categoryListScrollState.maxValue
                                    val scrollbarHeight =
                                        (totalHeight * (totalHeight / (totalHeight + categoryListScrollState.maxValue))).coerceAtLeast(
                                            36f
                                        )
                                    val scrollbarTop = progress * (totalHeight - scrollbarHeight)
                                    drawRoundRect(
                                        color = AccentIndigo.copy(alpha = 0.6f),
                                        topLeft = Offset(size.width - 6.dp.toPx(), scrollbarTop),
                                        size = Size(4.dp.toPx(), scrollbarHeight),
                                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                    )
                                }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(categoryListScrollState)
                                .padding(vertical = 4.dp)
                        ) {
                            if (filteredCategories.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.text_no_categories_registered),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                )
                            } else {
                                filteredCategories.forEach { category ->
                                    val isSelected = category.id == state.selectedCategoryId
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onCategorySelected(category.id)
                                                isCategoryDropdownExpanded = false
                                            }
                                            .background(
                                                if (isSelected) AccentIndigo.copy(alpha = 0.15f)
                                                else Color.Transparent
                                            )
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        val catColor = try {
                                            Color(category.colorHex.toColorInt())
                                        } catch (e: Exception) {
                                            MaterialTheme.colorScheme.primary
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = category.name,
                                            color = if (isSelected) AccentIndigo else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 15.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = AccentIndigo,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Data da Transação (Compacta, alinhada e elegante)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                    .clickable { datePickerDialog.show() }
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.cd_select_transaction_date),
                        tint = AccentIndigo,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = formattedDateText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 6. Botão Sutil do Accordion: "Mais opções (Observação, Tags) ▾"
            val hasExtraData = state.note.isNotBlank() || state.tags.isNotEmpty()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showMoreOptions = !showMoreOptions }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (showMoreOptions) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (showMoreOptions || hasExtraData) AccentIndigo else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.7f
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.text_more_options_accordion),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = if (showMoreOptions || hasExtraData) AccentIndigo else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.7f
                        )
                    )
                }

                if (!showMoreOptions && hasExtraData) {
                    val summaryText = buildString {
                        if (state.note.isNotBlank()) append(stringResource(R.string.text_note))
                        if (state.tags.isNotEmpty()) {
                            if (isNotEmpty()) append(" • ")
                            append(
                                "${state.tags.size} ${
                                    if (state.tags.size == 1) stringResource(R.string.text_singular_tag) else stringResource(
                                        R.string.message_plural_tag
                                    )
                                }"
                            )
                        }
                    }
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = AccentIndigo,
                        modifier = Modifier
                            .background(
                                AccentIndigo.copy(alpha = 0.15f), RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // 7. Seção Expansível Unificada (Accordion)
            AnimatedVisibility(
                visible = showMoreOptions,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Campo Observação
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = onNoteChanged,
                        label = { Text(stringResource(R.string.text_observation)) },
                        placeholder = { Text(stringResource(R.string.text_add_details)) },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentIndigo,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        trailingIcon = {
                            if (state.note.isNotBlank()) {
                                IconButton(onClick = { onNoteChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.clear_observation),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Campo Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.newTagInput,
                            onValueChange = onNewTagInputChanged,
                            placeholder = {
                                Text(
                                    if (state.tags.size < 2) stringResource(R.string.new_tag)
                                    else stringResource(R.string.text_tag_limit_reached)
                                )
                            },
                            singleLine = true,
                            enabled = state.tags.size < 2,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                onAddTag(state.newTagInput)
                                focusManager.clearFocus()
                            }),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentIndigo,
                                unfocusedBorderColor = DarkCardBorder,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                onAddTag(state.newTagInput)
                                focusManager.clearFocus()
                            }, enabled = state.tags.size < 2 && state.newTagInput.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.text_add_tag),
                                tint = if (state.tags.size < 2 && state.newTagInput.isNotBlank()) AccentIndigo
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }

                    // Chips das tags selecionadas
                    if (state.tags.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            state.tags.forEach { tag ->
                                InputChip(
                                    selected = true,
                                    onClick = { onRemoveTag(tag) },
                                    label = { Text(stringResource(R.string.text_name_tag, tag)) },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.cd_remove_tag),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = AccentIndigo.copy(alpha = 0.2f),
                                        selectedLabelColor = AccentIndigo,
                                        selectedTrailingIconColor = AccentIndigo
                                    )
                                )
                            }
                        }
                    }

                    // Sugestões de tags do banco
                    val suggestedTags = remember(availableTags, state.tags) {
                        availableTags.map { it.name }.filterNot { tag ->
                            state.tags.any {
                                it.equals(
                                    tag, ignoreCase = true
                                )
                            }
                        }.take(5)
                    }
                    if (suggestedTags.isNotEmpty() && state.tags.size < 2) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            suggestedTags.forEach { suggestion ->
                                SuggestionChip(
                                    onClick = { onAddTag(suggestion) },
                                    label = { Text("#$suggestion") })
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                        color = Color.White, modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check, contentDescription = null
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
}
