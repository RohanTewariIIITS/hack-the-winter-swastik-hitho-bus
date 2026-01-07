package com.example.celltowertrackingforbus.features.onlineRemoteTracking.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.celltowertrackingforbus.features.onlineRemoteTracking.OnlineTrackingViewModel

@Composable
fun OnlineTrackingScreen(
    viewModel: OnlineTrackingViewModel
) {

    val stopsStatus = viewModel.stopsAvailableStatus.value
    val showWarningPopup = viewModel.showWarningPopup.value
    val isCrowdSourcingUnavailable = viewModel.isCrowdSourcingUnavailable.value

    when(stopsStatus){
        is StopsAvailableStatus.StopsAvailable -> StopsAvailableUi(isCrowdSourcingUnavailable)
        is StopsAvailableStatus.NoStopsAvailable -> StopsUnavailableUi(isCrowdSourcingUnavailable)
    }
    
    
    WarningPopup(
        isWarned = showWarningPopup,
        warningContent = WarningContent(
            "Crowd Sourcing Unavailable",
            "Looks like Crowd Sourcing is unavailable right now for this bus.",
        ),
        onDismiss = { viewModel.dismissWarningPopup() },
    )
}


@Composable
fun StopsUnavailableUi(isWarned: Boolean) {
    val scrollState = rememberScrollState()
    Column(
        Modifier.fillMaxSize()
    )
    {
        // Header Section (Fixed)
        TripOverviewHeaderNoStops()

        // Content (Scrollable)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            TripDetailsCardNoStopsAvailable(isWarned = isWarned)

            // No Stops Available Card
            NoStopsAvailableCard(isWarned)

            // Tracking Mode Card
            TrackingModeCard(
                isWarned
            )
        }
    }
}

@Composable
fun StopsAvailableUi(isWarned: Boolean){
    val scrollState = rememberScrollState()
    Column(
        Modifier.fillMaxSize()
    )
    {
        // Header Section (Fixed)
        TripOverviewHeader()

        // Content (Scrollable)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            // Current Stop Card
            CurrentStopCard(isWarned)

            TripDetailsCardNoStopsAvailable(isWarned = isWarned)

            // Tracking Mode Card
            TrackingModeCard(
                isWarned
            )

        }
    }
}


@Preview(showBackground = true)
@Composable
fun OnlineTrackingScreenPreview() {
    OnlineTrackingScreen(
        viewModel = viewModel()
    )
}