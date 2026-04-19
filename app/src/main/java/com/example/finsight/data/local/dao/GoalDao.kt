package com.example.finsight.data.local.dao

import androidx.room.*
import com.example.finsight.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    @Query("UPDATE goals SET currentAmount = :amount WHERE id = :id")
    suspend fun updateGoalProgress(id: Long, amount: Double)

    @Query("UPDATE goals SET streakDays = :streak, lastCheckinDate = :date WHERE id = :id")
    suspend fun updateStreak(id: Long, streak: Int, date: Long)

    @Query("UPDATE goals SET isCompleted = :completed WHERE id = :id")
    suspend fun setGoalCompleted(id: Long, completed: Boolean)
}
