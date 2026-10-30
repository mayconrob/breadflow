package com.mayconrob.breadflow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mayconrob.breadflow.R
import com.mayconrob.breadflow.domain.enums.TransactionType
import com.mayconrob.breadflow.domain.model.Category
import com.mayconrob.breadflow.domain.model.TransactionWithCategory
import com.mayconrob.breadflow.ui.theme.ExpenseRed
import com.mayconrob.breadflow.ui.theme.IncomeGreen
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    categories: List<Category>,
    transactionToEdit: TransactionWithCategory? = null,
    onDismiss: () -> Unit,
    onConfirm: (description: String, amount: BigDecimal, type: TransactionType, categoryId: Long) -> Unit
) {
    val isEditing = transactionToEdit != null
    val textOtherMale = stringResource(R.string.text_other_male)
    val textOtherFemale = stringResource(R.string.text_other_female)

    var description by remember {
        mutableStateOf(
            transactionToEdit?.transaction?.description ?: ""
        )
    }
    var amountText by remember {
        mutableStateOf(
            transactionToEdit?.transaction?.amount?.toPlainString() ?: ""
        )
    }
    var selectedType by remember {
        mutableStateOf<TransactionType?>(
            transactionToEdit?.transaction?.type
        )
    }

    val filteredCategories = if (selectedType != null) {
        categories
            .filter { it.type == selectedType }
            .sortedWith(
                compareBy { category ->
                    val nameLower = category.name.trim().lowercase()
                    if (nameLower.startsWith(textOtherMale) ||
                        nameLower.startsWith(textOtherFemale)
                    ) 1 else 0
                }
            )
    } else emptyList()
    var selectedCategory by remember(selectedType, categories) {
        mutableStateOf(
            if (isEditing && selectedType == transactionToEdit?.transaction?.type) {
                categories.find { id -> id.id == transactionToEdit?.transaction?.categoryId }
            } else {
                null
            }
        )
    }

    var dropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar Transação" else "Nova Transação",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            selectedType = TransactionType.EXIT
                            selectedCategory = null
                        }
                    ) {
                        RadioButton(
                            selected = selectedType == TransactionType.EXIT,
                            onClick = {
                                selectedType = TransactionType.EXIT
                                selectedCategory = null
                            }
                        )
                        Text("Saída", color = ExpenseRed)
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            selectedType = TransactionType.ENTRY
                            selectedCategory = null
                        }
                    ) {
                        RadioButton(
                            selected = selectedType == TransactionType.ENTRY,
                            onClick = {
                                selectedType = TransactionType.ENTRY
                                selectedCategory = null
                            }
                        )
                        Text("Entrada", color = IncomeGreen)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição (ex: Almoço, Salário)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Valor R$
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor (R$) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Seletor de Categoria ancorado inline diretamente no fluxo
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (selectedType == null) "Selecione o tipo primeiro" else (selectedCategory?.name
                                ?: "Selecione"),
                            onValueChange = {},
                            readOnly = true,
                            enabled = selectedType != null,
                            label = { Text("Categoria *") },
                            trailingIcon = {
                                if (selectedType != null) {
                                    IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                                        Icon(
                                            imageVector = if (dropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (selectedType != null) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { dropdownExpanded = !dropdownExpanded }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = dropdownExpanded && selectedType != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .heightIn(max = 200.dp)
                                .drawWithContent {
                                    drawContent()
                                    if (scrollState.maxValue > 0) {
                                        val totalHeight = size.height
                                        val progress =
                                            scrollState.value.toFloat() / scrollState.maxValue
                                        val scrollbarHeight =
                                            (totalHeight * (totalHeight / (totalHeight + scrollState.maxValue))).coerceAtLeast(
                                                30f
                                            )
                                        val scrollbarTop = progress * (totalHeight - scrollbarHeight)
                                        drawRoundRect(
                                            color = Color.Gray.copy(alpha = 0.7f),
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
                                    .verticalScroll(scrollState)
                                    .padding(vertical = 4.dp)
                            ) {
                                // Opção limpar / Selecione
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategory = null
                                            dropdownExpanded = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        "Selecione",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }

                                if (filteredCategories.isEmpty()) {
                                    Text(
                                        text = "Nenhuma categoria de ${if (selectedType == TransactionType.ENTRY) "Entrada" else "Saída"}",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 10.dp
                                        )
                                    )
                                } else {
                                    filteredCategories.forEach { cat ->
                                        val isSelected = cat.id == selectedCategory?.id
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedCategory = cat
                                                    dropdownExpanded = false
                                                }
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Text(
                                                text = cat.name,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
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

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err,
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanAmountText = amountText.replace(',', '.').trim()
                    val amount = try {
                        if (cleanAmountText.isNotEmpty()) BigDecimal(cleanAmountText) else null
                    } catch (e: Exception) {
                        null
                    }
                    val type = selectedType
                    val category = selectedCategory
                    val finalDescription = description.trim()

                    if (type == null) {
                        errorMessage = "Selecione o tipo da transação (Entrada ou Saída)"
                    } else if (cleanAmountText.isEmpty() || amount == null) {
                        errorMessage = "Informe o valor da transação"
                    } else if (amount <= BigDecimal.ZERO) {
                        errorMessage = "O valor deve ser maior que zero"
                    } else if (category == null) {
                        errorMessage = "Selecione uma categoria válida"
                    } else {
                        onConfirm(finalDescription, amount, type, category.id)
                        onDismiss()
                    }
                }
            ) {
                Text(if (isEditing) "Salvar Alterações" else "Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
