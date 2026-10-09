package com.bob.whopaidit.data.model

data class TravelStage(
    val id: String = "",
    val tourId: String = "",
    val title: String = "",
    val driverUserId: String = "",
    val driverUserName: String = "",
    val distanceKm: Double = 0.0,
    val ratePerKm: Double = 12.0,
    val tollsAndParkingAmount: Double = 0.0,
    val passengerUserIds: List<String> = emptyList(),
    val date: Long = System.currentTimeMillis(),
) {
    val totalStageCost: Double
        get() = (distanceKm * ratePerKm) + tollsAndParkingAmount
}
