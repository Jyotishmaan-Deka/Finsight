package com.example.finsight.domain.model

import androidx.compose.ui.graphics.Color

enum class TransactionType(val label: String) {
    INCOME("Income"),
    EXPENSE("Expense")
}

enum class Category(
    val displayName: String,
    val emoji: String,
    val colorHex: Long
) {
    // Expense categories
    FOOD("Food & Dining", "🍔", 0xFFFF7043),
    SHOPPING("Shopping", "🛍️", 0xFFAB47BC),
    TRANSPORT("Transportation", "🚗", 0xFF42A5F5),
    ENTERTAINMENT("Entertainment", "🎬", 0xFFEF5350),
    HEALTH("Health & Fitness", "💊", 0xFF66BB6A),
    BILLS("Bills & Utilities", "💡", 0xFFFFCA28),
    EDUCATION("Education", "📚", 0xFFFF8A65),
    TRAVEL("Travel", "✈️", 0xFF29B6F6),
    SUBSCRIPTIONS("Subscriptions", "📱", 0xFF7E57C2),
    PERSONAL("Personal Care", "✨", 0xFFEC407A),
    GROCERIES("Groceries", "🛒", 0xFF8BC34A),

    // Income categories
    SALARY("Salary", "💼", 0xFF26A69A),
    FREELANCE("Freelance", "💻", 0xFF00BCD4),
    INVESTMENT("Investment", "📈", 0xFF5C6BC0),
    GIFT("Gift", "🎁", 0xFFFF80AB),
    BONUS("Bonus", "🏆", 0xFFFFD54F),

    OTHER("Other", "📦", 0xFF78909C);

    fun color() = Color(colorHex)

    companion object {
        fun expenseCategories() = listOf(
            FOOD, SHOPPING, TRANSPORT, ENTERTAINMENT, HEALTH,
            BILLS, EDUCATION, TRAVEL, SUBSCRIPTIONS, PERSONAL, GROCERIES, OTHER
        )

        fun incomeCategories() = listOf(
            SALARY, FREELANCE, INVESTMENT, GIFT, BONUS, OTHER
        )
    }
}

data class Transaction(
    val id: Long = 0L,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)
