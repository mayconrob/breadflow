package com.mayconrob.breadflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mayconrob.breadflow.common.Formatters
import com.mayconrob.breadflow.domain.enums.TransactionType
import com.mayconrob.breadflow.domain.model.Category
import com.mayconrob.breadflow.domain.model.TransactionWithCategory
import com.mayconrob.breadflow.domain.repository.ICategoriaRepository
import com.mayconrob.breadflow.domain.repository.ITransacaoRepository
import com.mayconrob.breadflow.ui.viewmodel.enums.OrdemTransacao
import com.mayconrob.breadflow.ui.viewmodel.enums.TipoFiltroData
import com.mayconrob.breadflow.ui.viewmodel.enums.TipoFiltroTransacao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class FiltrosExtratoParams(
    val query: String,
    val filterCatIds: Set<Long>,
    val tipoTransacao: TipoFiltroTransacao,
    val ordem: OrdemTransacao,
    val tipoData: TipoFiltroData,
    val inicio: Long?,
    val fim: Long?
)

@HiltViewModel
class ExtratoTransacoesViewModel @Inject constructor(
    private val transacaoRepository: ITransacaoRepository,
    private val categoriaRepository: ICategoriaRepository
) : ViewModel() {

    private val _tipoFiltroTransacao = MutableStateFlow(TipoFiltroTransacao.TODAS)
    val tipoFiltroTransacao = _tipoFiltroTransacao.asStateFlow()

    val categories: StateFlow<List<Category>> = combine(
        categoriaRepository.all,
        _tipoFiltroTransacao
    ) { allCategories, tipoTransacao ->
        val sorted = allCategories.sortedWith(compareBy({ it.type }, { it.name }))
        when (tipoTransacao) {
            TipoFiltroTransacao.TODAS -> sorted
            TipoFiltroTransacao.ENTRADAS -> sorted.filter { it.type == TransactionType.ENTRY }
            TipoFiltroTransacao.SAIDAS -> sorted.filter { it.type == TransactionType.EXIT }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val searchQuery = MutableStateFlow("")
    private val selectedCategoryIds = MutableStateFlow<Set<Long>>(emptySet())
    private val tipoFiltroData = MutableStateFlow(TipoFiltroData.HOJE)
    private val ordemTransacao = MutableStateFlow(OrdemTransacao.MAIS_RECENTES)
    private val dataInicio = MutableStateFlow<Long?>(null)
    private val dataFim = MutableStateFlow<Long?>(null)

    private val _uiState = MutableStateFlow(ExtratoTransacoesUiState())
    val uiState: StateFlow<ExtratoTransacoesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val filtroDataFlow =
                combine(tipoFiltroData, dataInicio, dataFim) { tipo, inicio, fim ->
                    Triple(tipo, inicio, fim)
                }

            val filtrosParamsFlow = combine(
                searchQuery,
                selectedCategoryIds,
                _tipoFiltroTransacao,
                ordemTransacao,
                filtroDataFlow
            ) { query, filterCatIds, tipoTransacao, ordem, filtroData ->
                val (tipoData, inicio, fim) = filtroData
                FiltrosExtratoParams(
                    query,
                    filterCatIds,
                    tipoTransacao,
                    ordem,
                    tipoData,
                    inicio,
                    fim
                )
            }

            combine(
                transacaoRepository.all,
                filtrosParamsFlow
            ) { transactions, params ->
                val trimmedQuery = params.query.trim()
                val isTagOnlySearch = trimmedQuery.startsWith("#")
                val cleanQuery =
                    if (isTagOnlySearch) trimmedQuery.removePrefix("#") else trimmedQuery
                val isSearchActive = cleanQuery.length >= 2
                val normalizedQuery =
                    if (isSearchActive) Formatters.removeAccents(cleanQuery) else ""

                // Pre-computa ranges de data uma unica vez fora do loop
                val todayRange =
                    if (params.tipoData == TipoFiltroData.HOJE) Formatters.getTodayRange() else null
                val last7DaysRange =
                    if (params.tipoData == TipoFiltroData.ULTIMOS_7_DIAS) Formatters.getLast7DaysRange() else null
                val periodRange =
                    if (params.tipoData == TipoFiltroData.PERIODO && params.inicio != null && params.fim != null) {
                        Pair(
                            Formatters.getStartOfDay(params.inicio),
                            Formatters.getEndOfDay(params.fim)
                        )
                    } else null

                val transacoesFiltradas = transactions.filter { item ->
                    // 1. Filtro rapido por Tipo de Transacao (O(1))
                    when (params.tipoTransacao) {
                        TipoFiltroTransacao.TODAS -> Unit
                        TipoFiltroTransacao.ENTRADAS -> if (item.transaction.type != TransactionType.ENTRY) return@filter false
                        TipoFiltroTransacao.SAIDAS -> if (item.transaction.type != TransactionType.EXIT) return@filter false
                    }

                    // 2. Filtro rapido por Categorias Selecionadas (O(1))
                    if (params.filterCatIds.isNotEmpty() && item.transaction.categoryId !in params.filterCatIds) {
                        return@filter false
                    }

                    // 3. Filtro por Data (O(1) numerico com ranges pre-calculados)
                    val timestamp = item.transaction.dateTimestamp
                    val matchesDate = when (params.tipoData) {
                        TipoFiltroData.HOJE -> todayRange?.let { timestamp in it.first..it.second }
                            ?: true

                        TipoFiltroData.ULTIMOS_7_DIAS -> last7DaysRange?.let { timestamp in it.first..it.second }
                            ?: true

                        TipoFiltroData.PERIODO -> periodRange?.let { timestamp in it.first..it.second }
                            ?: true
                    }
                    if (!matchesDate) return@filter false

                    // 4. Filtro por Busca de Texto e/ou Tags (Prefixo # pesquisa exclusivamente tags)
                    if (isSearchActive) {
                        val matchesTags = item.transaction.tags.any { tag ->
                            tag.name.contains(cleanQuery, ignoreCase = true) ||
                                    Formatters.removeAccents(tag.name)
                                        .contains(normalizedQuery, ignoreCase = true)
                        }

                        if (isTagOnlySearch) {
                            if (!matchesTags) return@filter false
                        } else {
                            val matchesDescription = item.transaction.description.contains(
                                cleanQuery,
                                ignoreCase = true
                            ) ||
                                    Formatters.removeAccents(item.transaction.description)
                                        .contains(normalizedQuery, ignoreCase = true)
                            if (!matchesDescription && !matchesTags) return@filter false
                        }
                    }

                    true
                }

                // 5. Aplicação da Ordenação dos Resultados
                val transacoesOrdenadas = when (params.ordem) {
                    OrdemTransacao.MAIS_RECENTES -> transacoesFiltradas.sortedByDescending { it.transaction.dateTimestamp }
                    OrdemTransacao.MAIS_ANTIGAS -> transacoesFiltradas.sortedBy { it.transaction.dateTimestamp }
                    OrdemTransacao.MAIOR_VALOR -> transacoesFiltradas.sortedByDescending { it.transaction.amount }
                    OrdemTransacao.MENOR_VALOR -> transacoesFiltradas.sortedBy { it.transaction.amount }
                }

                ExtratoTransacoesUiState(
                    transacoes = transacoesOrdenadas,
                    buscaQuery = params.query,
                    categoriaFiltroIds = params.filterCatIds,
                    tipoFiltroData = params.tipoData,
                    tipoFiltroTransacao = params.tipoTransacao,
                    ordemTransacao = params.ordem,
                    dataInicioTimestamp = params.inicio,
                    dataFimTimestamp = params.fim,
                    loading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun onCategoryFilterChanged(categoryId: Long?) {
        if (categoryId == null) {
            selectedCategoryIds.value = emptySet()
        } else {
            val current = selectedCategoryIds.value.toMutableSet()
            if (current.contains(categoryId)) {
                current.remove(categoryId)
            } else {
                current.add(categoryId)
            }
            selectedCategoryIds.value = current
        }
    }

    fun onTipoFiltroDataChanged(tipo: TipoFiltroData) {
        tipoFiltroData.value = tipo
    }

    fun onTipoFiltroTransacaoChanged(tipo: TipoFiltroTransacao) {
        _tipoFiltroTransacao.value = tipo
        selectedCategoryIds.value = emptySet()
    }

    fun onOrdemTransacaoChanged(ordem: OrdemTransacao) {
        ordemTransacao.value = ordem
    }

    fun onPeriodoDataChanged(dataInicio: Long?, dataFim: Long?) {
        this.dataInicio.value = dataInicio
        this.dataFim.value = dataFim
        tipoFiltroData.value = TipoFiltroData.PERIODO
    }

    fun deleteTransaction(item: TransactionWithCategory) {
        viewModelScope.launch {
            transacaoRepository.delete(item.transaction)
        }
    }
}
