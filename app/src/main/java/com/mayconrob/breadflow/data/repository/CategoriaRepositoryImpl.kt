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

import com.mayconrob.breadflow.data.local.CategoryDao
import com.mayconrob.breadflow.data.mapper.toDomain
import com.mayconrob.breadflow.data.mapper.toEntity
import com.mayconrob.breadflow.domain.model.Category
import com.mayconrob.breadflow.domain.repository.ICategoriaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoriaRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : ICategoriaRepository {

    override val all: Flow<List<Category>> = categoryDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun insert(category: Category): Long {
        return categoryDao.insert(category.toEntity())
    }

    override suspend fun update(category: Category) {
        categoryDao.update(category.toEntity())
    }

    override suspend fun delete(category: Category) {
        categoryDao.delete(category.toEntity())
    }
}
