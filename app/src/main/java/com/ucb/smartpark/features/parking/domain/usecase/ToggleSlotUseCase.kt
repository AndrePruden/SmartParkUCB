package com.ucb.smartpark.features.parking.domain.usecase

import com.ucb.smartpark.features.parking.domain.repository.IParkingRepository
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus

    class ToggleSlotUseCase(
    private val repo: IParkingRepository
) {
    suspend operator fun invoke(lotId: LotId, slotId: SlotId, status: SlotStatus) {
        repo.setSlotOccupied(lotId, slotId, status)
    }
}
