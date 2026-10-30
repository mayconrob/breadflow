package com.mayconrob.breadflow.ui.viewmodel

import com.mayconrob.breadflow.domain.model.Category

data class GerenciamentoCategoriasUiState(
    val categorias: List<Category> = emptyList(),
    val errorMessage: String? = null,
    val Loading: Boolean = false
)
