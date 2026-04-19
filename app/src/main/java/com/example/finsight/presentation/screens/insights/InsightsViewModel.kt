package com.example.finsight.presentation.screens.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finsight.data.repository.TransactionRepository
import com.example.finsight.domain.model.Category
import com.example.finsight.domain.model.TransactionType
import com.example.finsight.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class CategorySpend(val category: Category, val amount: Double, val percentage: Float)
data class MonthlyData(val month: String, val income: Double, val expense: Double)
data class DailyData(val day: String, val amount: Double)

data class InsightsUiState(
    val topCategories: List<CategorySpend> = emptyList(),
    val thisWeekExpense: Double = 0.0,
    val lastWeekExpense: Double = 0.0,
    val thisMonthExpense: Double = 0.0,
    val lastMonthExpense: Double = 0.0,
    val monthly6Data: List<MonthlyData> = emptyList(),
    val dailyLast7: List<DailyData> = emptyList(),
    val avgDailySpend: Double = 0.0,
    val highestCategory: Category? = null,
    val totalTransactions: Int = 0,
    val savingsRate: Float = 0f,
    val thisMonthIncome: Double = 0.0,
    val isLoading: Boolean = true
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            transactionRepo.getAllTransactions()
                .collectLatest { allTx ->
                    val now = System.currentTimeMillis()
                    val monthStart = DateUtils.getStartOfMonth()
                    val lastMonthStart = DateUtils.getStartOfLastMonth()
                    val lastMonthEnd = DateUtils.getEndOfLastMonth()
                    val weekStart = DateUtils.getStartOfWeek()
                    val lastWeekStart = DateUtils.getStartOfLastWeek()
                    val lastWeekEnd = DateUtils.getEndOfLastWeek()

                    // This month data
                    val thisMonthTx = allTx.filter { it.date >= monthStart }
                    val thisMonthIncome = thisMonthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                    val thisMonthExpense = thisMonthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                    // Last month
                    val lastMonthTx = allTx.filter { it.date in lastMonthStart..lastMonthEnd }
                    val lastMonthExpense = lastMonthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                    // This week
                    val thisWeekTx = allTx.filter { it.date >= weekStart }
                    val thisWeekExpense = thisWeekTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                    // Last week
                    val lastWeekTx = allTx.filter { it.date in lastWeekStart..lastWeekEnd }
                    val lastWeekExpense = lastWeekTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                    // Category breakdown this month
                    val categoryTotals = thisMonthTx
                        .filter { it.type == TransactionType.EXPENSE }
                        .groupBy { it.category }
                        .mapValues { (_, txs) -> txs.sumOf { it.amount } }
                        .toList()
                        .sortedByDescending { it.second }

                    val totalExpenseForPct = categoryTotals.sumOf { it.second }.let { if (it == 0.0) 1.0 else it }
                    val topCategories = categoryTotals.map { (cat, amount) ->
                        CategorySpend(cat, amount, (amount / totalExpenseForPct * 100).toFloat())
                    }

                    // Monthly 6-month data
                    val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                    val monthly6 = DateUtils.getLast6MonthsStartDates().map { (start, end) ->
                        val mTx = allTx.filter { it.date in start..end }
                        MonthlyData(
                            month = monthFormat.format(Date(start)),
                            income = mTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                            expense = mTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                        )
                    }

                    // Daily last 7 days
                    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    val (last7Start, last7End) = DateUtils.getLast7DaysRange()
                    val daily7 = (0..6).map { dayOffset ->
                        val cal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -6 + dayOffset)
                        }
                        val dayStart = DateUtils.getStartOfDay(cal.timeInMillis)
                        val dayEnd = dayStart + 86_400_000L - 1L
                        val dayExpense = allTx
                            .filter { it.date in dayStart..dayEnd && it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }
                        DailyData(dayFormat.format(cal.time), dayExpense)
                    }

                    val avgDaily = if (daily7.any { it.amount > 0 })
                        daily7.filter { it.amount > 0 }.sumOf { it.amount } / daily7.count { it.amount > 0 }
                    else 0.0

                    val savingsRate = if (thisMonthIncome > 0)
                        ((thisMonthIncome - thisMonthExpense) / thisMonthIncome * 100).toFloat().coerceIn(0f, 100f)
                    else 0f

                    _uiState.value = InsightsUiState(
                        topCategories = topCategories,
                        thisWeekExpense = thisWeekExpense,
                        lastWeekExpense = lastWeekExpense,
                        thisMonthExpense = thisMonthExpense,
                        lastMonthExpense = lastMonthExpense,
                        monthly6Data = monthly6,
                        dailyLast7 = daily7,
                        avgDailySpend = avgDaily,
                        highestCategory = topCategories.firstOrNull()?.category,
                        totalTransactions = allTx.size,
                        savingsRate = savingsRate,
                        thisMonthIncome = thisMonthIncome,
                        isLoading = false
                    )
                }
        }
    }
}
