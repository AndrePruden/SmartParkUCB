package com.ucb.smartpark.features.parking.domain.usecase

import com.ucb.smartpark.features.parking.domain.repository.IParkingRepository

class EnsureLotInitializedUseCase(
    private val repo: IParkingRepository
) {
    suspend operator fun invoke(lotId: String, slotsCount: Int = 32) {
        repo.ensureLotInitialized(lotId, slotsCount)
    }
}
