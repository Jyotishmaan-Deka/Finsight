package com.example.finsight.utils

import com.example.finsight.data.repository.GoalRepository
import com.example.finsight.data.repository.TransactionRepository
import com.example.finsight.domain.model.Category
import com.example.finsight.domain.model.Goal
import com.example.finsight.domain.model.GoalType
import com.example.finsight.domain.model.Transaction
import com.example.finsight.domain.model.TransactionType
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDataUtil @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository
) {

    suspend fun seedIfEmpty() {
        val existing = transactionRepository.getTransactionsByDateRangeSync(0L, Long.MAX_VALUE)
        if (existing.isNotEmpty()) return
        val transactions = generateSampleTransactions()
        transactions.forEach { transactionRepository.insertTransaction(it) }

        val goals = generateSampleGoals()
        goals.forEach { goalRepository.insertGoal(it) }
    }

    private fun generateSampleTransactions(): List<Transaction> {
        val cal = Calendar.getInstance()
        val transactions = mutableListOf<Transaction>()

        // This month - income
        transactions += Transaction(
            amount = 75000.0, type = TransactionType.INCOME,
            category = Category.SALARY, description = "Monthly Salary",
            date = daysAgo(cal, 2), note = "October salary credit"
        )
        transactions += Transaction(
            amount = 12000.0, type = TransactionType.INCOME,
            category = Category.FREELANCE, description = "Freelance Project",
            date = daysAgo(cal, 5), note = "UI design project"
        )

        // This month - expenses
        transactions += Transaction(
            amount = 2400.0, type = TransactionType.EXPENSE,
            category = Category.FOOD, description = "Zomato Order",
            date = daysAgo(cal, 1), note = "Dinner for two"
        )
        transactions += Transaction(
            amount = 850.0, type = TransactionType.EXPENSE,
            category = Category.TRANSPORT, description = "Ola Ride",
            date = daysAgo(cal, 1)
        )
        transactions += Transaction(
            amount = 3500.0, type = TransactionType.EXPENSE,
            category = Category.SHOPPING, description = "Amazon Shopping",
            date = daysAgo(cal, 3), note = "Books and stationary"
        )
        transactions += Transaction(
            amount = 15000.0, type = TransactionType.EXPENSE,
            category = Category.BILLS, description = "Rent",
            date = daysAgo(cal, 4), note = "Monthly rent"
        )
        transactions += Transaction(
            amount = 499.0, type = TransactionType.EXPENSE,
            category = Category.SUBSCRIPTIONS, description = "Netflix",
            date = daysAgo(cal, 6)
        )
        transactions += Transaction(
            amount = 799.0, type = TransactionType.EXPENSE,
            category = Category.SUBSCRIPTIONS, description = "Spotify Premium",
            date = daysAgo(cal, 6)
        )
        transactions += Transaction(
            amount = 4200.0, type = TransactionType.EXPENSE,
            category = Category.GROCERIES, description = "BigBasket Order",
            date = daysAgo(cal, 7), note = "Monthly groceries"
        )
        transactions += Transaction(
            amount = 1800.0, type = TransactionType.EXPENSE,
            category = Category.HEALTH, description = "Gym Membership",
            date = daysAgo(cal, 8)
        )
        transactions += Transaction(
            amount = 650.0, type = TransactionType.EXPENSE,
            category = Category.FOOD, description = "Starbucks Coffee",
            date = daysAgo(cal, 9)
        )
        transactions += Transaction(
            amount = 2500.0, type = TransactionType.EXPENSE,
            category = Category.ENTERTAINMENT, description = "Movie + Dinner",
            date = daysAgo(cal, 10), note = "Date night"
        )
        transactions += Transaction(
            amount = 12000.0, type = TransactionType.EXPENSE,
            category = Category.EDUCATION, description = "Online Course",
            date = daysAgo(cal, 12), note = "Udemy - Kotlin Compose"
        )

        // Last month transactions
        transactions += Transaction(
            amount = 75000.0, type = TransactionType.INCOME,
            category = Category.SALARY, description = "Monthly Salary",
            date = daysAgo(cal, 32)
        )
        transactions += Transaction(
            amount = 5000.0, type = TransactionType.INCOME,
            category = Category.BONUS, description = "Performance Bonus",
            date = daysAgo(cal, 35)
        )
        transactions += Transaction(
            amount = 15000.0, type = TransactionType.EXPENSE,
            category = Category.BILLS, description = "Rent",
            date = daysAgo(cal, 33)
        )
        transactions += Transaction(
            amount = 5200.0, type = TransactionType.EXPENSE,
            category = Category.GROCERIES, description = "Monthly Groceries",
            date = daysAgo(cal, 34)
        )
        transactions += Transaction(
            amount = 8000.0, type = TransactionType.EXPENSE,
            category = Category.SHOPPING, description = "Flipkart Sale",
            date = daysAgo(cal, 36), note = "Big Billion Days"
        )
        transactions += Transaction(
            amount = 3200.0, type = TransactionType.EXPENSE,
            category = Category.FOOD, description = "Restaurant & Cafes",
            date = daysAgo(cal, 38)
        )
        transactions += Transaction(
            amount = 2200.0, type = TransactionType.EXPENSE,
            category = Category.TRANSPORT, description = "Fuel & Cab Rides",
            date = daysAgo(cal, 40)
        )
        transactions += Transaction(
            amount = 1500.0, type = TransactionType.EXPENSE,
            category = Category.ENTERTAINMENT, description = "Weekend Trip",
            date = daysAgo(cal, 42)
        )

        transactions += Transaction(
            amount = 75000.0, type = TransactionType.INCOME,
            category = Category.SALARY, description = "Monthly Salary",
            date = daysAgo(cal, 62)
        )
        transactions += Transaction(
            amount = 8000.0, type = TransactionType.INCOME,
            category = Category.FREELANCE, description = "Consulting Work",
            date = daysAgo(cal, 65)
        )
        transactions += Transaction(
            amount = 15000.0, type = TransactionType.EXPENSE,
            category = Category.BILLS, description = "Rent",
            date = daysAgo(cal, 63)
        )
        transactions += Transaction(
            amount = 4800.0, type = TransactionType.EXPENSE,
            category = Category.GROCERIES, description = "Monthly Groceries",
            date = daysAgo(cal, 64)
        )
        transactions += Transaction(
            amount = 6000.0, type = TransactionType.EXPENSE,
            category = Category.SHOPPING, description = "Clothing & Accessories",
            date = daysAgo(cal, 66)
        )
        transactions += Transaction(
            amount = 3500.0, type = TransactionType.EXPENSE,
            category = Category.FOOD, description = "Dining & Food Delivery",
            date = daysAgo(cal, 68)
        )

        return transactions
    }

    private fun generateSampleGoals(): List<Goal> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 6)
        val sixMonthsLater = cal.timeInMillis

        cal.add(Calendar.MONTH, -5)
        cal.add(Calendar.MONTH, 3)
        val threeMonthsLater = cal.timeInMillis

        return listOf(
            Goal(
                title = "Emergency Fund",
                targetAmount = 200000.0,
                currentAmount = 45000.0,
                type = GoalType.SAVINGS,
                deadline = sixMonthsLater,
                color = 0xFF6C63FF
            ),
            Goal(
                title = "No-Spend November",
                targetAmount = 0.0,
                currentAmount = 0.0,
                type = GoalType.NO_SPEND,
                streakDays = 5,
                lastCheckinDate = daysAgo(Calendar.getInstance(), 0),
                color = 0xFFFF9F43
            ),
            Goal(
                title = "New Laptop Fund",
                targetAmount = 80000.0,
                currentAmount = 22000.0,
                type = GoalType.SAVINGS,
                deadline = threeMonthsLater,
                color = 0xFF00BCD4
            )
        )
    }

    private fun daysAgo(cal: Calendar, days: Int): Long {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -days)
        c.set(Calendar.HOUR_OF_DAY, (8..20).random())
        c.set(Calendar.MINUTE, (0..59).random())
        return c.timeInMillis
    }
}
