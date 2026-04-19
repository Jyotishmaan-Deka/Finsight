package com.example.finsight.presentation.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finsight.data.repository.TransactionRepository
import com.example.finsight.domain.model.Transaction
import com.example.finsight.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TransactionFilter { ALL, INCOME, EXPENSE }

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val filter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)

    val uiState: StateFlow<TransactionsUiState> = combine(
        repository.getAllTransactions(),
        _filter,
        _searchQuery
    ) { allTransactions, filter, query ->
        _isLoading.value = false
        val filtered = allTransactions
            .filter { tx ->
                when (filter) {
                    TransactionFilter.ALL -> true
                    TransactionFilter.INCOME -> tx.type == TransactionType.INCOME
                    TransactionFilter.EXPENSE -> tx.type == TransactionType.EXPENSE
                }
            }
            .filter { tx ->
                if (query.isBlank()) true
                else tx.description.contains(query, ignoreCase = true) ||
                        tx.note.contains(query, ignoreCase = true) ||
                        tx.category.displayName.contains(query, ignoreCase = true)
            }
        TransactionsUiState(
            transactions = allTransactions,
            filteredTransactions = filtered,
            filter = filter,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TransactionsUiState()
    )

    fun setFilter(filter: TransactionFilter) { _filter.value = filter }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
