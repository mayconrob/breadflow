package com.mayconrob.breadflow.data.mapper

import com.mayconrob.breadflow.data.local.CategoryEntity
import com.mayconrob.breadflow.data.local.TagEntity
import com.mayconrob.breadflow.data.local.TransactionEntity
import com.mayconrob.breadflow.data.local.TransactionWithCategoryEntity
import com.mayconrob.breadflow.domain.model.Category
import com.mayconrob.breadflow.domain.model.Tag
import com.mayconrob.breadflow.domain.model.Transaction
import com.mayconrob.breadflow.domain.model.TransactionWithCategory

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        type = type,
        budgetLimit = budgetLimit,
        colorHex = colorHex
    )
}

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        type = type,
        budgetLimit = budgetLimit,
        colorHex = colorHex
    )
}

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        createdAtTimestamp = createdAtTimestamp
    )
}

fun Tag.toEntity(): TagEntity {
    return TagEntity(
        id = id,
        name = name,
        createdAtTimestamp = createdAtTimestamp
    )
}

fun TransactionEntity.toDomain(tags: List<Tag> = emptyList()): Transaction {
    return Transaction(
        id = id,
        description = description,
        amount = amount,
        type = type,
        categoryId = categoryId,
        note = note,
        dateTimestamp = dateTimestamp,
        createdAtTimestamp = createdAtTimestamp,
        tags = tags
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        description = description,
        amount = amount,
        type = type,
        categoryId = categoryId,
        note = note,
        dateTimestamp = dateTimestamp,
        createdAtTimestamp = createdAtTimestamp
    )
}

fun TransactionWithCategoryEntity.toDomain(): TransactionWithCategory {
    val domainTags = tags.map { it.toDomain() }
    return TransactionWithCategory(
        transaction = transaction.toDomain(tags = domainTags),
        category = category.toDomain()
    )
}

fun TransactionWithCategory.toEntity(): TransactionWithCategoryEntity {
    return TransactionWithCategoryEntity(
        transaction = transaction.toEntity(),
        category = category.toEntity(),
        tags = transaction.tags.map { it.toEntity() }
    )
}
