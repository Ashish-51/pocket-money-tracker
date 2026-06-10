package com.example.data

import com.google.firebase.Timestamp
import java.util.Date

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = ""
)

enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    val transactionId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val note: String = "",
    val paymentMethod: String = "Cash",
    val timestamp: Timestamp = Timestamp(Date())
)

data class Budget(
    val budgetId: String = "",
    val userId: String = "",
    val category: String = "",
    val amountLimit: Double = 0.0
)

data class RecurringTransaction(
    val recurringId: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val note: String = "",
    val paymentMethod: String = "Cash",
    val interval: String = "Monthly", // "Daily", "Weekly", "Monthly", "Yearly"
    val nextProcessingDate: Timestamp = Timestamp(Date())
)
