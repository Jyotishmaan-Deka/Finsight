package com.example.finsight.data.repository

import com.example.finsight.data.local.dao.CategoryTotal
import com.example.finsight.data.local.dao.TransactionDao
import com.example.finsight.data.local.entity.TransactionEntity
import com.example.finsight.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> =
        dao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    fun getTransactionsByType(type: String): Flow<List<Transaction>> =
        dao.getTransactionsByType(type).map { list -> list.map { it.toDomain() } }

    fun searchTransactions(query: String): Flow<List<Transaction>> =
        dao.searchTransactions(query).map { list -> list.map { it.toDomain() } }

    fun getRecentTransactions(limit: Int = 5): Flow<List<Transaction>> =
        dao.getRecentTransactions(limit).map { list -> list.map { it.toDomain() } }

    fun getTotalIncome(): Flow<Double?> = dao.getTotalIncome()
    fun getTotalExpenses(): Flow<Double?> = dao.getTotalExpenses()

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        dao.getTransactionsByDateRange(startDate, endDate).map { list -> list.map { it.toDomain() } }

    suspend fun getTransactionsByDateRangeSync(startDate: Long, endDate: Long): List<Transaction> =
        dao.getTransactionsByDateRangeSync(startDate, endDate).map { it.toDomain() }

    suspend fun getIncomeInRange(startDate: Long, endDate: Long): Double =
        dao.getIncomeInRange(startDate, endDate) ?: 0.0

    suspend fun getExpenseInRange(startDate: Long, endDate: Long): Double =
        dao.getExpenseInRange(startDate, endDate) ?: 0.0

    suspend fun getCategoryExpensesInRange(startDate: Long, endDate: Long): List<CategoryTotal> =
        dao.getCategoryExpensesInRange(startDate, endDate)

    suspend fun getExpensesInRange(startDate: Long, endDate: Long): List<Transaction> =
        dao.getExpensesInRange(startDate, endDate).map { it.toDomain() }

    suspend fun getTransactionById(id: Long): Transaction? =
        dao.getTransactionById(id)?.toDomain()

    suspend fun insertTransaction(transaction: Transaction): Long =
        dao.insertTransaction(TransactionEntity.fromDomain(transaction))

    suspend fun updateTransaction(transaction: Transaction) =
        dao.updateTransaction(TransactionEntity.fromDomain(transaction))

    suspend fun deleteTransaction(transaction: Transaction) =
        dao.deleteTransaction(TransactionEntity.fromDomain(transaction))

    suspend fun deleteTransactionById(id: Long) =
        dao.deleteTransactionById(id)
}
