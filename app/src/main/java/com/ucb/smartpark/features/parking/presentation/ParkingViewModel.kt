package com.ucb.smartpark.features.parking.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.smartpark.features.notifications.data.repository.RemoteConfigRepository
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.usecase.ObserveParkingUseCase
import com.ucb.smartpark.features.parking.domain.usecase.ToggleSlotUseCase
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ParkingViewModel(
    private val observeParking: ObserveParkingUseCase,
    private val toggleSlot: ToggleSlotUseCase,
    private val configRepo: RemoteConfigRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    val lots: List<LotId> = listOf(LotId("Tupuraya 1"), LotId("Tupuraya 2"))

    private val _selectedLot = MutableStateFlow(lots.first())
    val selectedLot: StateFlow<LotId> = _selectedLot.asStateFlow()

    // Evento para el Snackbar
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

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
        // 1. Carga inicial
        onLotSelected(_selectedLot.value)
        // 2. Escucha activa de cambios
        startListeningToConfigChanges()
    }

    private fun startListeningToConfigChanges() {
        viewModelScope.launch {
            configRepo.observeConfigUpdates().collectLatest { newStatus ->
                // ¡Alerta visual!
                _uiMessage.emit("⚠️ Aviso: El estado de mantenimiento ha cambiado.")

                // Re-evaluar el estado actual con el nuevo valor
                val currentLot = _selectedLot.value
                val isClosed = checkIsClosed(newStatus, currentLot)

                if (isClosed) {
                    // Si se cerró, cortamos la conexión a DB y mostramos mantenimiento
                    observeJob?.cancel()
                    _state.value = UiState.Maintenance("Este parqueo se encuentra cerrado por mantenimiento.")
                } else {
                    // Si se abrió y estábamos en mantenimiento, recargamos
                    if (_state.value is UiState.Maintenance) {
                        onLotSelected(currentLot)
                    }
                }
            }
        }
    }

    // Lógica auxiliar para verificar si está cerrado
    private fun checkIsClosed(status: Int, lotId: LotId): Boolean {
        return when {
            status == 3 -> true
            status == 1 && lotId.value == "Tupuraya 1" -> true
            status == 2 && lotId.value == "Tupuraya 2" -> true
            else -> false
        }
    }

    fun onLotSelected(lotId: LotId) {
        _selectedLot.value = lotId
        observeJob?.cancel()

        observeJob = viewModelScope.launch(Dispatchers.IO) {
            _state.value = UiState.Loading

            // Verificación inicial (Fetch forzado gracias al intervalo 0)
            val status = configRepo.fetchParkingStatus()

            if (checkIsClosed(status, lotId)) {
                _state.value = UiState.Maintenance("Este parqueo se encuentra cerrado por mantenimiento.")
                return@launch
            }

            // Si está abierto, observamos Slots
            observeParking(lotId)
                .catch { e -> _state.value = UiState.Error(e.message ?: "Error") }
                .collect { list -> _state.value = UiState.Success(list) }
        }
    }

    fun onSlotClicked(slot: ParkingSlot) {
        if (_state.value !is UiState.Success) return
        viewModelScope.launch(Dispatchers.IO) {
            toggleSlot(_selectedLot.value, slot.id, SlotStatus(!slot.status.value))
        }
    }
}