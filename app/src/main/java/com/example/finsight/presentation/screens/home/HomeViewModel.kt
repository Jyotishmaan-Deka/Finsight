package com.example.finsight.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finsight.data.repository.GoalRepository
import com.example.finsight.data.repository.TransactionRepository
import com.example.finsight.domain.model.Category
import com.example.finsight.domain.model.Goal
import com.example.finsight.domain.model.Transaction
import com.example.finsight.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val categoryBreakdown: List<Pair<Category, Double>> = emptyList(),
    val activeGoals: List<Goal> = emptyList(),
    val thisMonthIncome: Double = 0.0,
    val thisMonthExpense: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val goalRepo: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                transactionRepo.getTotalIncome(),
                transactionRepo.getTotalExpenses(),
                transactionRepo.getRecentTransactions(5),
                goalRepo.getActiveGoals()
            ) { income, expenses, recent, goals ->
                val totalIncome = income ?: 0.0
                val totalExpenses = expenses ?: 0.0

                // This month stats
                val monthStart = DateUtils.getStartOfMonth()
                val monthEnd = System.currentTimeMillis()
                val monthIncome = transactionRepo.getIncomeInRange(monthStart, monthEnd)
                val monthExpense = transactionRepo.getExpenseInRange(monthStart, monthEnd)

                // Category breakdown for current month
                val categoryTotals = transactionRepo.getCategoryExpensesInRange(monthStart, monthEnd)
                val breakdown = categoryTotals.mapNotNull { ct ->
                    runCatching { Category.valueOf(ct.category) }.getOrNull()?.let { it to ct.total }
                }.sortedByDescending { it.second }

                HomeUiState(
                    totalBalance = totalIncome - totalExpenses,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    recentTransactions = recent,
                    categoryBreakdown = breakdown,
                    activeGoals = goals.take(2),
                    thisMonthIncome = monthIncome,
                    thisMonthExpense = monthExpense,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
