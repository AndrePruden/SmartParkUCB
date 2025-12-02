package com.ucb.smartpark.features.parking.domain.usecase

import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.repository.IParkingRepository
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ParkingUseCasesTest {

    private val repository: IParkingRepository = mockk()

    @Test
    fun `ObserveParkingUseCase should call repository observeSlots`() {
        // Arrange
        val useCase = ObserveParkingUseCase(repository)
        val lotId = LotId("TestLot")
        every { repository.observeSlots(lotId) } returns flowOf(emptyList())

        // Act
        useCase(lotId)

        // Assert
        verify(exactly = 1) { repository.observeSlots(lotId) }
    }

    @Test
    fun `ToggleSlotUseCase should call repository setSlotOccupied`() = runTest {
        // Arrange
        val useCase = ToggleSlotUseCase(repository)
        val lotId = LotId("TestLot")
        val slotId = SlotId(1)
        val status = SlotStatus.Occupied

        coEvery { repository.setSlotOccupied(lotId, slotId, status) } returns Unit

        // Act
        useCase(lotId, slotId, status)

        // Assert
        coVerify(exactly = 1) { repository.setSlotOccupied(lotId, slotId, status) }
    }

    @Test
    fun `EnsureLotInitializedUseCase should call repository ensureLotInitialized`() = runTest {
        // Arrange
        val useCase = EnsureLotInitializedUseCase(repository)
        val lotId = LotId("NewLot")

        coEvery { repository.ensureLotInitialized(lotId, 32) } returns Unit

        // Act
        useCase(lotId)

        // Assert
        coVerify(exactly = 1) { repository.ensureLotInitialized(lotId, 32) }
    }
}