package com.bob.whopaidit.util

import com.bob.whopaidit.data.model.Expense
import com.bob.whopaidit.data.model.SettlementPayment
import com.bob.whopaidit.data.model.TravelStage
import kotlin.math.abs
import kotlin.math.min

object SettlementCalculator {

    fun calculateNetBalances(
        participantNames: Map<String, String>,
        expenses: List<Expense>,
        stages: List<TravelStage>,
    ): Map<String, Double> {
        val netBalances = participantNames.keys.associateWith { 0.0 }.toMutableMap()

        // Process general shared expenses
        expenses.forEach { expense ->
            if (expense.splitUserIds.isNotEmpty() && (expense.amount > 0)) {
                val perPersonShare = expense.amount / expense.splitUserIds.size
                netBalances[expense.paidByUserId] = (netBalances[expense.paidByUserId] ?: 0.0) + expense.amount
                expense.splitUserIds.forEach { userId ->
                    netBalances[userId] = (netBalances[userId] ?: 0.0) - perPersonShare
                }
            }
        }

        // Process stage-exact travel expenses
        stages.forEach { stage ->
            val totalCost = stage.totalStageCost
            if (stage.passengerUserIds.isNotEmpty() && (totalCost > 0)) {
                val perPassengerShare = totalCost / stage.passengerUserIds.size
                netBalances[stage.driverUserId] = (netBalances[stage.driverUserId] ?: 0.0) + totalCost
                stage.passengerUserIds.forEach { userId ->
                    netBalances[userId] = (netBalances[userId] ?: 0.0) - perPassengerShare
                }
            }
        }

        return netBalances
    }

    fun calculateMinimalSettlements(
        participantNames: Map<String, String>,
        netBalances: Map<String, Double>,
    ): List<SettlementPayment> {
        val debtors = mutableListOf<Pair<String, Double>>()
        val creditors = mutableListOf<Pair<String, Double>>()

        netBalances.forEach { (userId, balance) ->
            if (balance < -0.01) {
                debtors.add(Pair(userId, abs(balance)))
            } else if (balance > 0.01) {
                creditors.add(Pair(userId, balance))
            }
        }

        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val payments = mutableListOf<SettlementPayment>()
        var debtorIdx = 0
        var creditorIdx = 0

        while (debtorIdx < debtors.size && creditorIdx < creditors.size) {
            val (debtorId, debtAmount) = debtors[debtorIdx]
            val (creditorId, creditAmount) = creditors[creditorIdx]

            val settledAmount = min(debtAmount, creditAmount)
            val debtorName = participantNames[debtorId] ?: debtorId
            val creditorName = participantNames[creditorId] ?: creditorId

            payments.add(
                SettlementPayment(
                    fromUserId = debtorId,
                    fromUserName = debtorName,
                    toUserId = creditorId,
                    toUserName = creditorName,
                    amount = settledAmount,
                ),
            )

            val remainingDebt = debtAmount - settledAmount
            val remainingCredit = creditAmount - settledAmount

            if (remainingDebt < 0.01) {
                debtorIdx++
            } else {
                debtors[debtorIdx] = Pair(debtorId, remainingDebt)
            }

            if (remainingCredit < 0.01) {
                creditorIdx++
            } else {
                creditors[creditorIdx] = Pair(creditorId, remainingCredit)
            }
        }

        return payments
    }
}
