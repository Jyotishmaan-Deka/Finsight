package com.example.finsight.data.local.dao

import androidx.room.*
import com.example.finsight.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getTransactionsByDateRangeSync(startDate: Long, endDate: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE description LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpenses(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND date >= :startDate AND date <= :endDate")
    suspend fun getIncomeInRange(startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate")
    suspend fun getExpenseInRange(startDate: Long, endDate: Long): Double?

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate GROUP BY category ORDER BY total DESC")
    suspend fun getCategoryExpensesInRange(startDate: Long, endDate: Long): List<CategoryTotal>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate AND type = 'EXPENSE' ORDER BY date DESC")
    suspend fun getExpensesInRange(startDate: Long, endDate: Long): List<TransactionEntity>
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
