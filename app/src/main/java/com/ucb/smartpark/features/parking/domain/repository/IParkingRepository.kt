package com.ucb.smartpark.features.parking.domain.repository

import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import kotlinx.coroutines.flow.Flow

interface IParkingRepository {
    /** Observa todos los slots en tiempo real. */
    fun observeSlots(): Flow<List<ParkingSlot>>

    /** Actualiza el estado de un slot y registra auditoría. */
    suspend fun setSlotOccupied(slotId: Int, occupied: Boolean)
}
