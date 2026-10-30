package com.mayconrob.breadflow.data.repository

import com.mayconrob.breadflow.data.local.TagDao
import com.mayconrob.breadflow.data.mapper.toDomain
import com.mayconrob.breadflow.domain.model.Tag
import com.mayconrob.breadflow.domain.repository.ITagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao
) : ITagRepository {
    override val all: Flow<List<Tag>> = tagDao.getAll().map { list ->
        list.map { it.toDomain() }
    }

}