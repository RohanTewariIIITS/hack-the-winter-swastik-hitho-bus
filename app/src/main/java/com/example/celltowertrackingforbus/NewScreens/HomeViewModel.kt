package com.example.celltowertrackingforbus.NewScreens

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val fromLocation: String = "",
    val toLocation: String = "",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val showDatePicker: Boolean = false
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun updateFromLocation(from: String) {
        _uiState.value = _uiState.value.copy(fromLocation = from)
    }

    fun updateToLocation(to: String) {
        _uiState.value = _uiState.value.copy(toLocation = to)
    }

    fun swapLocations() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            fromLocation = currentState.toLocation,
            toLocation = currentState.fromLocation
        )
    }

    fun updateSelectedDate(dateMillis: Long) {
        _uiState.value = _uiState.value.copy(selectedDateMillis = dateMillis)
    }

    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }

    fun hideDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }

    /**
     * Checks if the current route is valid (Haldwani to Delhi or Haldwani to Anand Vihar)
     * Case insensitive comparison
     */
    fun isValidRoute(): Boolean {
        val from = _uiState.value.fromLocation.trim().lowercase()
        val to = _uiState.value.toLocation.trim().lowercase()

        return from == "haldwani" && (to == "delhi" || to == "anand vihar")
    }

    /**
     * Checks if both from and to fields have some input
     */
    fun hasInput(): Boolean {
        return _uiState.value.fromLocation.isNotBlank() && _uiState.value.toLocation.isNotBlank()
    }
}

