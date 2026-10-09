package com.bob.whopaidit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String = "",
    val tripId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val category: String = "General",
    val merchant: String = "",
    val dateTime: String = "",
    val location: String = "",
    val notes: String = "",
    val paidBy: String = "You",
    val splitMode: String = "Equal",
    val splitParticipants: String = "",
    val subtitle: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
