package com.ucb.smartpark.features.parking.domain.repository

import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import kotlinx.coroutines.flow.Flow

interface IParkingRepository {

    fun observeSlots(lotId: LotId): Flow<List<ParkingSlot>>

    suspend fun setSlotOccupied(lotId: LotId, slotId: SlotId, status: SlotStatus)

    suspend fun ensureLotInitialized(lotId: LotId, slotsCount: Int = 32)
}
