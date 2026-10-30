package com.mayconrob.breadflow.ui.viewmodel

import com.mayconrob.breadflow.domain.model.TransactionWithCategory
import com.mayconrob.breadflow.domain.model.FinancialSummary
import com.mayconrob.breadflow.common.Formatters

import java.util.Calendar

data class PainelFinanceiroUiState(
    val resumo: FinancialSummary = FinancialSummary(),
    val consumosOrcamento: List<OrcamentoCategoriaUiState> = emptyList(),
    val ultimasTransacoes: List<TransactionWithCategory> = emptyList(),
    val currentMonthLabel: String = Formatters.getCurrentMonthName(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val loading: Boolean = false,
    val isValuesVisible: Boolean = false,
)
