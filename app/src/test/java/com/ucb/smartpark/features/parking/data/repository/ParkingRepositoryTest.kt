package com.ucb.smartpark.features.parking.data.repository

import com.ucb.smartpark.features.parking.data.datasource.ParkingRemoteDataSource
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ParkingRepositoryTest {

    // Mockeamos el DataSource (la clase concreta)
    private val remoteDataSource: ParkingRemoteDataSource = mockk()
    private val repository = ParkingRepository(remoteDataSource)

    @Test
    fun `observeSlots should return flow from remote data source`() = runTest {
        // Arrange
        val lotId = LotId("Tupuraya 1")
        val expectedSlots = listOf(
            ParkingSlot(SlotId(1), SlotStatus.Free),
            ParkingSlot(SlotId(2), SlotStatus.Occupied)
        )
        // Simulamos que el remote devuelve un Flow con esos datos
        every { remoteDataSource.observeSlots(lotId) } returns flowOf(expectedSlots)

        // Act
        val resultFlow = repository.observeSlots(lotId)
        val resultList = resultFlow.first() // Obtenemos el primer valor

        // Assert
        assertEquals(expectedSlots, resultList)
        // Verificamos que se llamó al método del remote
        io.mockk.verify(exactly = 1) { remoteDataSource.observeSlots(lotId) }
    }

    @Test
    fun `setSlotOccupied should call remote data source`() = runTest {
        // Arrange
        val lotId = LotId("Tupuraya 1")
        val slotId = SlotId(5)
        val status = SlotStatus.Occupied

        // Simulamos que el método suspendido termina bien (returns Unit)
        coEvery { remoteDataSource.setSlotOccupied(lotId, slotId, status) } returns Unit

        // Act
        repository.setSlotOccupied(lotId, slotId, status)

        // Assert
        coVerify(exactly = 1) { remoteDataSource.setSlotOccupied(lotId, slotId, status) }
    }
}