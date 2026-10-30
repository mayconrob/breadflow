package com.mayconrob.breadflow.domain.model

data class Tag(
    val id: Long = 0,
    val name: String,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
