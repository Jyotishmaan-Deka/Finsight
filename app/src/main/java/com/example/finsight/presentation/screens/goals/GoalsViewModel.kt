package com.example.finsight.presentation.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finsight.data.repository.GoalRepository
import com.example.finsight.data.repository.TransactionRepository
import com.example.finsight.domain.model.Goal
import com.example.finsight.domain.model.GoalType
import com.example.finsight.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class GoalsUiState(
    val allGoals: List<Goal> = emptyList(),
    val activeGoals: List<Goal> = emptyList(),
    val completedGoals: List<Goal> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepo: GoalRepository,
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = goalRepo.getAllGoals()
        .map { goals ->
            GoalsUiState(
                allGoals = goals,
                activeGoals = goals.filter { !it.isCompleted },
                completedGoals = goals.filter { it.isCompleted },
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalsUiState())

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch { goalRepo.deleteGoal(goal) }
    }

    fun checkinStreak(goal: Goal) {
        viewModelScope.launch {
            val today = System.currentTimeMillis()
            val lastCheckin = goal.lastCheckinDate

            val newStreak = if (lastCheckin != null && DateUtils.isSameDay(lastCheckin, today)) {
                // Already checked in today
                goal.streakDays
            } else if (lastCheckin != null) {
                val yesterday = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                }.timeInMillis
                if (DateUtils.isSameDay(lastCheckin, yesterday)) {
                    // Consecutive day
                    goal.streakDays + 1
                } else {
                    // Streak broken
                    1
                }
            } else {
                1
            }

            goalRepo.updateStreak(goal.id, newStreak, today)
        }
    }

    fun addProgress(goal: Goal, amount: Double) {
        viewModelScope.launch {
            val newAmount = goal.currentAmount + amount
            goalRepo.updateGoalProgress(goal.id, newAmount)
            if (newAmount >= goal.targetAmount) {
                goalRepo.setGoalCompleted(goal.id, true)
            }
        }
    }

    fun autoSyncSavingsGoal(goal: Goal) {
        viewModelScope.launch {
            if (goal.type == GoalType.SAVINGS) {
                val monthStart = DateUtils.getStartOfMonth()
                val now = System.currentTimeMillis()
                val income = transactionRepo.getIncomeInRange(monthStart, now)
                val expense = transactionRepo.getExpenseInRange(monthStart, now)
                val savings = (income - expense).coerceAtLeast(0.0)
                goalRepo.updateGoalProgress(goal.id, savings)
                if (savings >= goal.targetAmount) {
                    goalRepo.setGoalCompleted(goal.id, true)
                }
            }
        }
    }
}
