package com.ucb.smartpark.features.parking.domain.repository

import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import kotlinx.coroutines.flow.Flow

interface IParkingRepository {

    fun observeSlots(lotId: String): Flow<List<ParkingSlot>>

    /** Actualiza el estado de un slot dentro de un parqueo y registra auditoría. */
    suspend fun setSlotOccupied(lotId: String, slotId: Int, occupied: Boolean)

    suspend fun ensureLotInitialized(lotId: String, slotsCount: Int = 32)
}
