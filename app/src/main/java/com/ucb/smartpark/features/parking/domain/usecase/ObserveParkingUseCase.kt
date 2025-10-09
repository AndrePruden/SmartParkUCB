package com.ucb.smartpark.features.parking.domain.usecase

import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.repository.IParkingRepository
import kotlinx.coroutines.flow.Flow

class ObserveParkingUseCase(
    private val repo: IParkingRepository
) {
    operator fun invoke(): Flow<List<ParkingSlot>> = repo.observeSlots()
}
