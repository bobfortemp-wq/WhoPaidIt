package com.bob.whopaidit.data.model

data class Expense(
    val id: String = "",
    val tourId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val paidByUserId: String = "",
    val paidByUserName: String = "",
    val splitUserIds: List<String> = emptyList(),
    val category: String = "General",
    val date: Long = System.currentTimeMillis(),
)
