/*
 * BreadFlow - Gestão Financeira Pessoal
 * Copyright (C) 2026 Maycon Roberto - GitHub: @mayconrob
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.mayconrob.breadflow.data.repository

import com.mayconrob.breadflow.data.local.TagDao
import com.mayconrob.breadflow.data.local.TagEntity
import com.mayconrob.breadflow.data.local.TransactionDao
import com.mayconrob.breadflow.data.local.TransactionTagCrossRef
import com.mayconrob.breadflow.data.mapper.toDomain
import com.mayconrob.breadflow.data.mapper.toEntity
import com.mayconrob.breadflow.domain.enums.TransactionType
import com.mayconrob.breadflow.domain.model.Tag
import com.mayconrob.breadflow.domain.model.Transaction
import com.mayconrob.breadflow.domain.model.TransactionWithCategory
import com.mayconrob.breadflow.domain.repository.ITransacaoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransacaoRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val tagDao: TagDao
) : ITransacaoRepository {

    override val all: Flow<List<TransactionWithCategory>> =
        transactionDao.getAllWithCategory().map { list ->
            list.map { it.toDomain() }
        }

    override val totalIncome: Flow<BigDecimal?> = all.map { list ->
        list.filter { it.transaction.type == TransactionType.ENTRY }
            .fold(BigDecimal.ZERO) { acc, item -> acc.add(item.transaction.amount) }
    }

    override val totalExpenses: Flow<BigDecimal?> = all.map { list ->
        list.filter { it.transaction.type == TransactionType.EXIT }
            .fold(BigDecimal.ZERO) { acc, item -> acc.add(item.transaction.amount) }
    }

    override fun totalExpensesForCategory(categoryId: Long): Flow<BigDecimal?> = all.map { list ->
        list.filter { it.transaction.categoryId == categoryId && it.transaction.type == TransactionType.EXIT }
            .fold(BigDecimal.ZERO) { acc, item -> acc.add(item.transaction.amount) }
    }

    override suspend fun insert(transaction: Transaction): Long {
        val transactionId = transactionDao.insert(transaction.toEntity())
        saveTagsForTransaction(transactionId, transaction.tags.take(10))
        return transactionId
    }

    override suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction.toEntity())
        tagDao.deleteCrossRefsForTransaction(transaction.id)
        saveTagsForTransaction(transaction.id, transaction.tags.take(10))
    }

    override suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction.toEntity())
    }

    override suspend fun hasTransactionsForCategory(categoryId: Long): Boolean {
        return transactionDao.countTransactionsForCategory(categoryId) > 0
    }

    private suspend fun saveTagsForTransaction(transactionId: Long, tags: List<Tag>) {
        for (tag in tags) {
            val trimmedName = tag.name.trim()
            if (trimmedName.isBlank()) continue

            val tagId = if (tag.id > 0) {
                tag.id
            } else {
                val existingTag = tagDao.getTagByName(trimmedName)
                if (existingTag != null) {
                    existingTag.id
                } else {
                    val insertedId = tagDao.insert(
                        TagEntity(
                            name = trimmedName,
                            createdAtTimestamp = tag.createdAtTimestamp
                        )
                    )
                    if (insertedId > 0) insertedId else tagDao.getTagByName(trimmedName)?.id
                        ?: continue
                }
            }

            tagDao.insertTransactionTagCrossRef(
                TransactionTagCrossRef(transactionId = transactionId, tagId = tagId)
            )
        }
    }
}
