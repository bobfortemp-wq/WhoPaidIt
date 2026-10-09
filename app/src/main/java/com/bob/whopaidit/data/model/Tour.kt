package com.bob.whopaidit.data.model

data class Tour(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "INR",
    val memberIds: List<String> = emptyList(),
    val familyGroups: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
)
