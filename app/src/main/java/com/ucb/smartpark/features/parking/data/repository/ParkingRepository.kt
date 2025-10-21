package com.ucb.smartpark.features.parking.data.repository

import com.ucb.smartpark.features.parking.data.datasource.ParkingRemoteDataSource
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.repository.IParkingRepository
import kotlinx.coroutines.flow.Flow

class ParkingRepository(
    private val remote: ParkingRemoteDataSource
) : IParkingRepository {

    override fun observeSlots(lotId: String): Flow<List<ParkingSlot>> =
        remote.observeSlots(lotId)

    override suspend fun setSlotOccupied(lotId: String, slotId: Int, occupied: Boolean) {
        remote.setSlotOccupied(lotId, slotId, occupied)
    }

    override suspend fun ensureLotInitialized(lotId: String, slotsCount: Int) {
        TODO("Not yet implemented")
    }
}
