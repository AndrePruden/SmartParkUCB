package com.ucb.smartpark.features.parking.domain.usecase

import com.ucb.smartpark.features.parking.domain.repository.IParkingRepository

class ToggleSlotUseCase(
    private val repo: IParkingRepository
) {
    suspend operator fun invoke(slotId: Int, occupied: Boolean) {
        repo.setSlotOccupied(slotId, occupied)
    }
}
