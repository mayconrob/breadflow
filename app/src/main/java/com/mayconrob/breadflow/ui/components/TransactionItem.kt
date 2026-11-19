package com.mayconrob.breadflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mayconrob.breadflow.R
import com.mayconrob.breadflow.common.Formatters
import com.mayconrob.breadflow.domain.enums.TransactionType
import com.mayconrob.breadflow.domain.model.TransactionWithCategory
import com.mayconrob.breadflow.ui.theme.DarkCardBorder
import com.mayconrob.breadflow.ui.theme.DarkSurface
import com.mayconrob.breadflow.ui.theme.ExpenseRed
import com.mayconrob.breadflow.ui.theme.IncomeGreen

@Composable
fun TransactionItem(
    item: TransactionWithCategory,
    modifier: Modifier = Modifier,
    onClick: ((TransactionWithCategory) -> Unit)? = null,
    onMoreClick: ((TransactionWithCategory) -> Unit)? = null
) {
    val context = LocalContext.current

    val isIncome = item.transaction.type == TransactionType.ENTRY
    val amountPrefix = if (isIncome) stringResource(R.string.label_positive)
    else stringResource(R.string.label_negative)

    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val formattedDate = Formatters.formatShortDate(item.transaction.dateTimestamp)

    val itemModifier = if (onClick != null) {
        modifier.clickable { onClick(item) }
    } else {
        modifier
    }

    Row(
        modifier = itemModifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {}
        ) {
            Column {
                if (item.transaction.description.isNotBlank()) {
                    Text(
                        text = item.transaction.description,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = context.getString(
                                R.string.cd_text_transaction,
                                item.transaction.description
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = amountColor
                    )
                    Text(
                        text = stringResource(R.string.transaction_date_bullet, formattedDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.semantics {
                            contentDescription =
                                context.getString(
                                    R.string.cd_transaction_date,
                                    formattedDate
                                )
                        }
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(
                    R.string.formatted_transaction_amount,
                    amountPrefix,
                    Formatters.formatCurrency(item.transaction.amount)
                ),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = amountColor
            )

            if (onMoreClick != null) {
                IconButton(
                    onClick = { onMoreClick(item) }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.cd_more_options),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
