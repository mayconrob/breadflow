package com.mayconrob.breadflow.ui.viewmodel

import com.mayconrob.breadflow.domain.model.Category
import java.math.BigDecimal

data class OrcamentoCategoriaUiState(
    val categoria: Category,
    val totalGasto: BigDecimal = BigDecimal.ZERO,
    val porcentagemConsumida: BigDecimal = BigDecimal.ZERO
)
