package com.ucb.smartpark.features.parking.data.repository

import com.ucb.smartpark.features.parking.data.datasource.ParkingRemoteDataSource
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.repository.IParkingRepository
import kotlinx.coroutines.flow.Flow

class ParkingRepository(
    private val remote: ParkingRemoteDataSource
) : IParkingRepository {

    override fun observeSlots(): Flow<List<ParkingSlot>> = remote.observeSlots()

    override suspend fun setSlotOccupied(slotId: Int, occupied: Boolean) {
        remote.setSlotOccupied(slotId, occupied)
    }
}
