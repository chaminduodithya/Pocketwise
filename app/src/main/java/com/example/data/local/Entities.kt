package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,
    EXPENSE
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryName: String,
    val paymentMethod: String,
    val timestamp: Long,
    val receiptImageUri: String? = null,
    val notes: String = ""
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val targetAmount: Double,
    val allocatedAmount: Double = 0.0,
    val storeUrl: String = "",
    val storeLabel: String = "",
    val imageUri: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isPurchased: Boolean = false
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val iconKey: String,
    val colorHex: Long,
    val isDefault: Boolean = false
)
