package com.ucb.smartpark.features.parking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.smartpark.features.notifications.data.repository.RemoteConfigRepository
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.usecase.ObserveParkingUseCase
import com.ucb.smartpark.features.parking.domain.usecase.ToggleSlotUseCase
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
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
    private val toggleSlot: ToggleSlotUseCase,
    private val configRepo: RemoteConfigRepository
) : ViewModel() {

    val lots: List<LotId> = listOf(LotId("Tupuraya 1"), LotId("Tupuraya 2"))

    private val _selectedLot = MutableStateFlow(lots.first())
    val selectedLot: StateFlow<LotId> = _selectedLot.asStateFlow()

    sealed class UiState {
        object Loading : UiState()
        data class Success(val slots: List<ParkingSlot>) : UiState()
        data class Error(val message: String) : UiState()
        data class Maintenance(val message: String) : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var observeJob: Job? = null

    init {
        onLotSelected(_selectedLot.value)
    }

    fun onLotSelected(lotId: LotId) {
        // Actualizamos el lote seleccionado inmediatamente
        _selectedLot.value = lotId

        observeJob?.cancel()
        observeJob = viewModelScope.launch(Dispatchers.IO) {
            _state.value = UiState.Loading

            // 1. Verificar Remote Config antes de observar datos
            val status = configRepo.fetchParkingStatus()

            // Lógica de bloqueo según el valor de Remote Config
            val isClosed = when {
                status == 3 -> true // Ambos cerrados
                status == 1 && lotId.value == "Tupuraya 1" -> true
                status == 2 && lotId.value == "Tupuraya 2" -> true
                else -> false
            }

            if (isClosed) {
                _state.value = UiState.Maintenance("Este parqueo se encuentra cerrado por mantenimiento.")
                return@launch
            }

            // 2. Si está abierto, observamos los slots normalmente
            observeParking(lotId)
                .catch { e -> _state.value = UiState.Error(e.message ?: "Error") }
                .collect { list -> _state.value = UiState.Success(list) }
        }
    }

    fun onSlotClicked(slot: ParkingSlot) {
        // Evitamos clicks si el estado no es Success (ej. si está en Mantenimiento)
        if (_state.value !is UiState.Success) return

        viewModelScope.launch(Dispatchers.IO) {
            val lotId = _selectedLot.value
            val newStatus = SlotStatus(!slot.status.value)
            toggleSlot(lotId, slot.id, newStatus)
        }
    }
}