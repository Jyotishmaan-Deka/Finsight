package com.example.finsight.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.finsight.domain.model.Category
import com.example.finsight.domain.model.Transaction
import com.example.finsight.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amount: Double,
    val type: String,
    val category: String,
    val description: String,
    val date: Long,
    val note: String
) {
    fun toDomain() = Transaction(
        id = id,
        amount = amount,
        type = TransactionType.valueOf(type),
        category = Category.valueOf(category),
        description = description,
        date = date,
        note = note
    )

    companion object {
        fun fromDomain(t: Transaction) = TransactionEntity(
            id = t.id,
            amount = t.amount,
            type = t.type.name,
            category = t.category.name,
            description = t.description,
            date = t.date,
            note = t.note
        )
    }
}
