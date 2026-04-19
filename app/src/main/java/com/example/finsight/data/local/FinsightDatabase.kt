package com.example.finsight.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.finsight.data.local.dao.GoalDao
import com.example.finsight.data.local.dao.TransactionDao
import com.example.finsight.data.local.entity.GoalEntity
import com.example.finsight.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, GoalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FinsightDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao
}
