package com.example.celltowertrackingforbus.features.onlineRemoteTracking

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.celltowertrackingforbus.features.onlineRemoteTracking.screens.StopsAvailableStatus

class OnlineTrackingViewModel : ViewModel() {
    private val _stopsAvailableStatus = mutableStateOf<StopsAvailableStatus>(StopsAvailableStatus.StopsAvailable)
    val stopsAvailableStatus: State<StopsAvailableStatus> = _stopsAvailableStatus

    // Whether to show the warning popup dialog
    private val _showWarningPopup = mutableStateOf(true)
    val showWarningPopup: State<Boolean> = _showWarningPopup

    // Whether crowd sourcing is unavailable (shows warning text in cards)
    private val _isCrowdSourcingUnavailable = mutableStateOf(true)
    val isCrowdSourcingUnavailable: State<Boolean> = _isCrowdSourcingUnavailable

    fun setStopsAvailableStatus(status: StopsAvailableStatus) {
        _stopsAvailableStatus.value = status
    }

    fun dismissWarningPopup() {
        _showWarningPopup.value = false
    }

    fun setCrowdSourcingUnavailable(unavailable: Boolean) {
        _isCrowdSourcingUnavailable.value = unavailable
        if (unavailable) {
            _showWarningPopup.value = true
        }
    }
}