package com.mayconrob.breadflow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mayconrob.breadflow.R
import com.mayconrob.breadflow.domain.model.Tag
import com.mayconrob.breadflow.ui.theme.AccentIndigo
import com.mayconrob.breadflow.ui.theme.DarkCardBorder
import com.mayconrob.breadflow.ui.theme.DarkSurface

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionExtraOptionsSection(
    note: String,
    onNoteChanged: (String) -> Unit,
    tags: List<String>,
    newTagInput: String,
    onNewTagInputChanged: (String) -> Unit,
    availableTags: List<Tag>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showMoreOptions by remember { mutableStateOf(note.isNotBlank() || tags.isNotEmpty()) }

    LaunchedEffect(note.isNotBlank(), tags.isNotEmpty()) {
        if (note.isNotBlank() || tags.isNotEmpty()) {
            showMoreOptions = true
        }
    }

    val hasExtraData = note.isNotBlank() || tags.isNotEmpty()

    Column(modifier = modifier.fillMaxWidth()) {
        // Botão do Accordion: "Mais opções (Observação, Tags) ▾"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { showMoreOptions = !showMoreOptions }
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    if (note.isNotBlank()) append(stringResource(R.string.text_note))
                    if (tags.isNotEmpty()) {
                        if (isNotEmpty()) append(" • ")
                        append(
                            "${tags.size} ${
                                if (tags.size == 1) stringResource(R.string.text_singular_tag)
                                else stringResource(R.string.message_plural_tag)
                            }"
                        )
                    }
                }
                Text(
                    text = summaryText,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = AccentIndigo,
                    modifier = Modifier
                        .background(AccentIndigo.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // Seção Expansível Unificada
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
                    value = note,
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
                        if (note.isNotBlank()) {
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
                        value = newTagInput,
                        onValueChange = onNewTagInputChanged,
                        placeholder = {
                            Text(
                                if (tags.size < 2) stringResource(R.string.new_tag)
                                else stringResource(R.string.text_tag_limit_reached)
                            )
                        },
                        singleLine = true,
                        enabled = tags.size < 2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onAddTag(newTagInput)
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
                            onAddTag(newTagInput)
                            focusManager.clearFocus()
                        },
                        enabled = tags.size < 2 && newTagInput.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.text_add_tag),
                            tint = if (tags.size < 2 && newTagInput.isNotBlank()) AccentIndigo
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }

                // Chips das tags selecionadas
                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tags.forEach { tag ->
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
                val suggestedTags = remember(availableTags, tags) {
                    availableTags.map { it.name }.filterNot { tag ->
                        tags.any { it.equals(tag, ignoreCase = true) }
                    }.take(5)
                }

                if (suggestedTags.isNotEmpty() && tags.size < 2) {
                    Text(
                        text = if (suggestedTags.size == 1) stringResource(R.string.text_tag_available) else
                            stringResource(R.string.text_tags_available),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        suggestedTags.forEach { suggestion ->
                            SuggestionChip(
                                onClick = { onAddTag(suggestion) },
                                label = { Text("#$suggestion") }
                            )
                        }
                    }
                }
            }
        }
    }
}
