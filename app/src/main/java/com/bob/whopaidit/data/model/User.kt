package com.bob.whopaidit.data.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
