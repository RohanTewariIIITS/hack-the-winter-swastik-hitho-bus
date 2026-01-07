package com.example.celltowertrackingforbus.features.onlineRemoteTracking.screens

sealed class StopsAvailableStatus(
    var isWarned: Boolean,
    val title:String,
    val message:String
) {

    companion object {}

    object NoStopsAvailable : StopsAvailableStatus(
        isWarned = true,
        title = "No Stops Available",
        message = "Stops for this route are not available currently in the database.As you complete the trip you will contribute the stop database"
    )
//Vo logic jismein identified nearyby stops ko db mein add karna hai  vo app mein to nahi likhna padega instead of backend.
    object StopsAvailable : StopsAvailableStatus(
        isWarned = true,
        title = "Error",
        message = "An error occurred while fetching stops. Please try again later."
    )
}