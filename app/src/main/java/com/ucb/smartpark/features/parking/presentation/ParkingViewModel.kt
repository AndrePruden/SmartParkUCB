package com.ucb.smartpark.features.parking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.usecase.ObserveParkingUseCase
import com.ucb.smartpark.features.parking.domain.usecase.ToggleSlotUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ParkingViewModel(
    private val observeParking: ObserveParkingUseCase,
    private val toggleSlot: ToggleSlotUseCase
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val slots: List<ParkingSlot>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        observe()
    }

    private fun observe() {
        viewModelScope.launch(Dispatchers.IO) {
            observeParking()
                .onStart { _state.value = UiState.Loading }
                .catch { e -> _state.value = UiState.Error(e.message ?: "Error") }
                .collect { list -> _state.value = UiState.Success(list) }
        }
    }

    /** Para modo demo: tocar un slot alterna su estado. */
    fun onSlotClicked(slot: ParkingSlot) {
        viewModelScope.launch(Dispatchers.IO) {
            toggleSlot(slot.id, !slot.isOccupied)
        }
    }
}
