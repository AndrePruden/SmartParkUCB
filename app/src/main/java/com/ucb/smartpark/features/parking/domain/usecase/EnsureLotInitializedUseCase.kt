package com.ucb.smartpark.features.parking.domain.usecase

import com.ucb.smartpark.features.parking.domain.repository.IParkingRepository
import com.ucb.smartpark.features.parking.domain.vo.LotId

class EnsureLotInitializedUseCase(
    private val repo: IParkingRepository
) {
    suspend operator fun invoke(lotId: LotId, slotsCount: Int = 32) {
        repo.ensureLotInitialized(lotId, slotsCount)
    }
}
