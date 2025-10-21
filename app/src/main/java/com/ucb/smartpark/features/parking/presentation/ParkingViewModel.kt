package com.ucb.smartpark.features.parking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.usecase.ObserveParkingUseCase
import com.ucb.smartpark.features.parking.domain.usecase.ToggleSlotUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    /** Lotes disponibles en el selector de la UI. */
    val lots: List<String> = listOf("parking1", "parking2")

    /** Lote seleccionado actualmente. */
    private val _selectedLot = MutableStateFlow(lots.first())
    val selectedLot: StateFlow<String> = _selectedLot.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val slots: List<ParkingSlot>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var observeJob: Job? = null

    init {
        startObserving(_selectedLot.value)
    }

    /** Cambia el parqueo y vuelve a observar en tiempo real. */
    fun onLotSelected(lotId: String) {
        if (lotId == _selectedLot.value) return
        _selectedLot.value = lotId
        startObserving(lotId)
    }

    private fun startObserving(lotId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch(Dispatchers.IO) {
            observeParking(lotId)
                .onStart { _state.value = UiState.Loading }
                .catch { e -> _state.value = UiState.Error(e.message ?: "Error") }
                .collect { list -> _state.value = UiState.Success(list) }
        }
    }

    /** Alterna estado del slot en el lote actual. */
    fun onSlotClicked(slot: ParkingSlot) {
        viewModelScope.launch(Dispatchers.IO) {
            val lotId = _selectedLot.value
            toggleSlot(lotId, slot.id, !slot.isOccupied)
        }
    }
}
