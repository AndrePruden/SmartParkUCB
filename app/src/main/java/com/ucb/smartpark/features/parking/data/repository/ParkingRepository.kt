package com.ucb.smartpark.features.parking.data.repository

import com.ucb.smartpark.features.parking.data.datasource.ParkingRemoteDataSource
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.repository.IParkingRepository
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import kotlinx.coroutines.flow.Flow

class ParkingRepository(
    private val remote: ParkingRemoteDataSource
) : IParkingRepository {

    override fun observeSlots(lotId: LotId): Flow<List<ParkingSlot>> =
        remote.observeSlots(lotId)

    override suspend fun setSlotOccupied(lotId: LotId, slotId: SlotId, status: SlotStatus) {
        remote.setSlotOccupied(lotId, slotId, status)
    }

    override suspend fun ensureLotInitialized(lotId: LotId, slotsCount: Int) {
    }
}