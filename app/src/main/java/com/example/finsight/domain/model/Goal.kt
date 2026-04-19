package com.example.finsight.domain.model

enum class GoalType(val label: String) {
    SAVINGS("Savings Goal"),
    BUDGET_LIMIT("Budget Limit"),
    NO_SPEND("No-Spend Challenge"),
    DEBT_PAYOFF("Debt Payoff")
}

data class Goal(
    val id: Long = 0L,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val type: GoalType = GoalType.SAVINGS,
    val deadline: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val streakDays: Int = 0,
    val lastCheckinDate: Long? = null,
    val category: Category? = null,
    val color: Long = 0xFF6C63FF
) {
    val progressPercent: Float
        get() = if (targetAmount > 0) (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)

    val isOverdue: Boolean
        get() = deadline != null && System.currentTimeMillis() > deadline && !isCompleted
}
