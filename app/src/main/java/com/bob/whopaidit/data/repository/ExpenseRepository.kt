package com.bob.whopaidit.data.repository

import com.bob.whopaidit.data.model.Expense
import com.bob.whopaidit.data.model.Tour
import com.bob.whopaidit.data.model.TravelStage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) {

    suspend fun createTour(tour: Tour): String = withContext(Dispatchers.IO) {
        val docRef = if (tour.id.isBlank()) {
            firestore.collection("tours").document()
        } else {
            firestore.collection("tours").document(tour.id)
        }
        val tourWithId = tour.copy(id = docRef.id)
        docRef.set(tourWithId).await()
        docRef.id
    }

    fun getUserTours(userId: String): Flow<List<Tour>> = callbackFlow {
        val subscription = firestore.collection("tours")
            .whereArrayContains("memberIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tours = snapshot?.documents?.mapNotNull { it.toObject(Tour::class.java) } ?: emptyList()
                trySend(tours)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addExpense(expense: Expense): String = withContext(Dispatchers.IO) {
        val docRef = firestore.collection("tours")
            .document(expense.tourId)
            .collection("expenses")
            .document()

        val expenseWithId = expense.copy(id = docRef.id)
        docRef.set(expenseWithId).await()
        docRef.id
    }

    fun getTourExpenses(tourId: String): Flow<List<Expense>> = callbackFlow {
        val subscription = firestore.collection("tours")
            .document(tourId)
            .collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { it.toObject(Expense::class.java) } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addTravelStage(stage: TravelStage): String = withContext(Dispatchers.IO) {
        val docRef = firestore.collection("tours")
            .document(stage.tourId)
            .collection("stages")
            .document()

        val stageWithId = stage.copy(id = docRef.id)
        docRef.set(stageWithId).await()
        docRef.id
    }

    fun getTourTravelStages(tourId: String): Flow<List<TravelStage>> = callbackFlow {
        val subscription = firestore.collection("tours")
            .document(tourId)
            .collection("stages")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val stages = snapshot?.documents?.mapNotNull { it.toObject(TravelStage::class.java) } ?: emptyList()
                trySend(stages)
            }
        awaitClose { subscription.remove() }
    }
}
