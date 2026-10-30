package com.mayconrob.breadflow.domain.model

import com.mayconrob.breadflow.domain.enums.TransactionType
import java.math.BigDecimal

data class Transaction(
    val id: Long = 0,
    val description: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val categoryId: Long,
    val note: String? = null,
    val dateTimestamp: Long,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val tags: List<Tag> = emptyList()
)
