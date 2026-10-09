package com.bob.whopaidit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val currency: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val memberCount: Int = 0,
    val members: String = "",
    val totalExpense: String = "",
    val status: String = "Active",
    val isSettled: Boolean = false,
    val createdAt: Long = 0L,
)
