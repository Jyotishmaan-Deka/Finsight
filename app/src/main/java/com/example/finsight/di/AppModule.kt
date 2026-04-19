package com.example.finsight.di

import android.content.Context
import androidx.room.Room
import com.example.finsight.data.local.FinsightDatabase
import com.example.finsight.data.local.dao.GoalDao
import com.example.finsight.data.local.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinsightDatabase =
        Room.databaseBuilder(
            context,
            FinsightDatabase::class.java,
            "finsight_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTransactionDao(db: FinsightDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideGoalDao(db: FinsightDatabase): GoalDao = db.goalDao()
}
