package com.example.finsight.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finsight.domain.model.Category
import com.example.finsight.domain.model.Goal
import com.example.finsight.domain.model.GoalType

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val type: String,
    val deadline: Long?,
    val createdAt: Long,
    val isCompleted: Boolean,
    val streakDays: Int,
    val lastCheckinDate: Long?,
    val category: String?,
    val color: Long
) {
    fun toDomain() = Goal(
        id = id,
        title = title,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        type = GoalType.valueOf(type),
        deadline = deadline,
        createdAt = createdAt,
        isCompleted = isCompleted,
        streakDays = streakDays,
        lastCheckinDate = lastCheckinDate,
        category = category?.let { Category.valueOf(it) },
        color = color
    )

    companion object {
        fun fromDomain(g: Goal) = GoalEntity(
            id = g.id,
            title = g.title,
            targetAmount = g.targetAmount,
            currentAmount = g.currentAmount,
            type = g.type.name,
            deadline = g.deadline,
            createdAt = g.createdAt,
            isCompleted = g.isCompleted,
            streakDays = g.streakDays,
            lastCheckinDate = g.lastCheckinDate,
            category = g.category?.name,
            color = g.color
        )
    }
}
