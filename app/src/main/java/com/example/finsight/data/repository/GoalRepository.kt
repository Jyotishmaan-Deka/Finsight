package com.example.finsight.data.repository

import com.example.finsight.data.local.dao.GoalDao
import com.example.finsight.data.local.entity.GoalEntity
import com.example.finsight.domain.model.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val dao: GoalDao
) {
    fun getAllGoals(): Flow<List<Goal>> =
        dao.getAllGoals().map { list -> list.map { it.toDomain() } }

    fun getActiveGoals(): Flow<List<Goal>> =
        dao.getActiveGoals().map { list -> list.map { it.toDomain() } }

    suspend fun getGoalById(id: Long): Goal? =
        dao.getGoalById(id)?.toDomain()

    suspend fun insertGoal(goal: Goal): Long =
        dao.insertGoal(GoalEntity.fromDomain(goal))

    suspend fun updateGoal(goal: Goal) =
        dao.updateGoal(GoalEntity.fromDomain(goal))

    suspend fun deleteGoal(goal: Goal) =
        dao.deleteGoal(GoalEntity.fromDomain(goal))

    suspend fun deleteGoalById(id: Long) =
        dao.deleteGoalById(id)

    suspend fun updateGoalProgress(id: Long, amount: Double) =
        dao.updateGoalProgress(id, amount)

    suspend fun updateStreak(id: Long, streak: Int, date: Long) =
        dao.updateStreak(id, streak, date)

    suspend fun setGoalCompleted(id: Long, completed: Boolean) =
        dao.setGoalCompleted(id, completed)
}
