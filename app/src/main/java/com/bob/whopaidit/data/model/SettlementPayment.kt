package com.bob.whopaidit.data.model

data class SettlementPayment(
    val fromUserId: String,
    val fromUserName: String,
    val toUserId: String,
    val toUserName: String,
    val amount: Double,
)
