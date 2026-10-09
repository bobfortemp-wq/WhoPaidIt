package com.bob.whopaidit.viewModel

import androidx.lifecycle.ViewModel
import com.bob.whopaidit.data.model.Expense
import com.bob.whopaidit.data.model.SettlementPayment
import com.bob.whopaidit.data.model.TravelStage
import com.bob.whopaidit.data.repository.AuthRepository
import com.bob.whopaidit.data.repository.ExpenseRepository
import com.bob.whopaidit.util.SettlementCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _stages = MutableStateFlow<List<TravelStage>>(emptyList())
    val stages: StateFlow<List<TravelStage>> = _stages.asStateFlow()

    private val _netBalances = MutableStateFlow<Map<String, Double>>(emptyMap())
    val netBalances: StateFlow<Map<String, Double>> = _netBalances.asStateFlow()

    private val _settlements = MutableStateFlow<List<SettlementPayment>>(emptyList())
    val settlements: StateFlow<List<SettlementPayment>> = _settlements.asStateFlow()

    fun addExpense(
        title: String,
        amount: Double,
        category: String = "General",
        splitUserIds: List<String> = listOf("user_you"),
    ) {
        val currentUserId = authRepository.getCurrentUserId() ?: "user_you"
        val newExpense = Expense(
            id = System.currentTimeMillis().toString(),
            title = title,
            amount = amount,
            paidByUserId = currentUserId,
            paidByUserName = "You",
            splitUserIds = splitUserIds,
            category = category,
        )
        _expenses.value = listOf(newExpense) + _expenses.value
        recalculateBalancesAndSettlements()
    }

    fun addTravelStage(
        title: String,
        distanceKm: Double,
        ratePerKm: Double = 10.0,
        tollsAndParkingAmount: Double = 0.0,
        passengerUserIds: List<String> = listOf("user_you"),
    ) {
        val currentUserId = authRepository.getCurrentUserId() ?: "user_you"
        val newStage = TravelStage(
            id = System.currentTimeMillis().toString(),
            title = title,
            driverUserId = currentUserId,
            driverUserName = "You",
            distanceKm = distanceKm,
            ratePerKm = ratePerKm,
            tollsAndParkingAmount = tollsAndParkingAmount,
            passengerUserIds = passengerUserIds,
        )
        _stages.value = listOf(newStage) + _stages.value
        recalculateBalancesAndSettlements()
    }

    private fun recalculateBalancesAndSettlements() {
        val participantNames = mapOf(
            "user_you" to "You",
        )

        val balances = SettlementCalculator.calculateNetBalances(
            participantNames = participantNames,
            expenses = _expenses.value,
            stages = _stages.value,
        )

        val minimalSettlements = SettlementCalculator.calculateMinimalSettlements(
            participantNames = participantNames,
            netBalances = balances,
        )

        _netBalances.value = balances
        _settlements.value = minimalSettlements
    }
}
