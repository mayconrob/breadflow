package com.mayconrob.breadflow.domain.repository

import com.mayconrob.breadflow.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface ITagRepository {
    val all: Flow<List<Tag>>
}