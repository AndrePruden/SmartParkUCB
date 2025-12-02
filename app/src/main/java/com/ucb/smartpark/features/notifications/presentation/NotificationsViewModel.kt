package com.ucb.smartpark.features.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.smartpark.features.notifications.data.repository.RemoteConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val configRepo: RemoteConfigRepository
) : ViewModel() {

    private val _statusMessage = MutableStateFlow("Cargando notificaciones...")
    val statusMessage = _statusMessage.asStateFlow()

    init {
        fetchStatus()
    }

    fun fetchStatus() {
        viewModelScope.launch {
            val status = configRepo.fetchParkingStatus()
            _statusMessage.value = when (status) {
                1 -> "⚠️ AVISO: El parqueo 'Tupuraya 1' se encuentra cerrado por mantenimiento."
                2 -> "⚠️ AVISO: El parqueo 'Tupuraya 2' se encuentra cerrado por mantenimiento."
                3 -> "⛔ ALERTA: Ambos parqueos están cerrados temporalmente."
                else -> "✅ Todo normal. Los parqueos están operando habitualmente."
            }
        }
    }
}