package com.ucb.smartpark.features.parking.presentation

import app.cash.turbine.test
import com.ucb.smartpark.MainDispatcherRule
import com.ucb.smartpark.features.notifications.data.repository.RemoteConfigRepository
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.usecase.ObserveParkingUseCase
import com.ucb.smartpark.features.parking.domain.usecase.ToggleSlotUseCase
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ParkingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeParkingUseCase: ObserveParkingUseCase = mockk()
    private val toggleSlotUseCase: ToggleSlotUseCase = mockk()
    private val configRepo: RemoteConfigRepository = mockk()

    // Dispatcher para tests
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: ParkingViewModel

    @Test
    fun `init should show Maintenance state when config returns 3 (All Closed)`() = runTest {
        // Arrange
        // 1. fetch estático (con delay para simular carga)
        coEvery { configRepo.fetchParkingStatus() } coAnswers {
            delay(100)
            3
        }
        // 2. 🟢 NUEVO: Mockeamos el observer de tiempo real también
        every { configRepo.observeConfigUpdates() } returns flowOf(3)

        // Mock del caso de uso
        coEvery { observeParkingUseCase(any()) } returns flowOf(emptyList())

        // Act
        viewModel = ParkingViewModel(
            observeParkingUseCase,
            toggleSlotUseCase,
            configRepo,
            testDispatcher
        )

        // Assert
        viewModel.state.test {
            // 1. Loading
            val firstState = awaitItem()
            println("Estado 1 recibido: $firstState")
            assertTrue(firstState is ParkingViewModel.UiState.Loading)

            // 2. Estado Final
            val finalState = awaitItem()
            println("Estado 2 recibido: $finalState") // 👈 Esto nos dirá la verdad en la consola

            // Verificamos tipo
            assertTrue("Esperaba Maintenance pero llegó: $finalState", finalState is ParkingViewModel.UiState.Maintenance)

            // Verificamos mensaje (Solo si pasó lo anterior)
            val msg = (finalState as ParkingViewModel.UiState.Maintenance).message
            assertEquals("Este parqueo se encuentra cerrado por mantenimiento.", msg)
        }
    }

    @Test
    fun `init should show Success state when config returns 0 (Open)`() = runTest {
        // Arrange
        val lotId = LotId("Tupuraya 1")
        val slots = listOf(ParkingSlot(SlotId(1), SlotStatus.Free))

        // 1. Fetch normal
        coEvery { configRepo.fetchParkingStatus() } coAnswers {
            delay(100)
            0
        }
        // 2. 🟢 NUEVO: Observer en tiempo real
        every { configRepo.observeConfigUpdates() } returns flowOf(0)

        coEvery { observeParkingUseCase(lotId) } returns flowOf(slots)

        // Act
        viewModel = ParkingViewModel(observeParkingUseCase, toggleSlotUseCase, configRepo, testDispatcher)

        // Assert
        viewModel.state.test {
            assertTrue(awaitItem() is ParkingViewModel.UiState.Loading)

            val result = awaitItem()
            assertTrue(result is ParkingViewModel.UiState.Success)
            assertEquals(slots, (result as ParkingViewModel.UiState.Success).slots)
        }
    }

    @Test
    fun `onLotSelected should switch lot and check config again`() = runTest {
        // Arrange
        val lot2 = LotId("Tupuraya 2")

        coEvery { configRepo.fetchParkingStatus() } coAnswers { delay(50); 0 }
        // 🟢 NUEVO: Observer
        every { configRepo.observeConfigUpdates() } returns flowOf(0)

        coEvery { observeParkingUseCase(any()) } returns flowOf(emptyList())

        viewModel = ParkingViewModel(observeParkingUseCase, toggleSlotUseCase, configRepo, testDispatcher)

        // Act
        viewModel.onLotSelected(lot2)

        // Assert
        viewModel.selectedLot.test {
            assertEquals(lot2, awaitItem())
        }
    }

    @Test
    fun `onSlotClicked should call toggle use case only if state is Success`() = runTest {
        // Arrange
        val lotId = LotId("Tupuraya 1")
        val slot = ParkingSlot(SlotId(1), SlotStatus.Free)

        coEvery { configRepo.fetchParkingStatus() } coAnswers { delay(50); 0 }
        // 🟢 NUEVO: Observer
        every { configRepo.observeConfigUpdates() } returns flowOf(0)

        coEvery { observeParkingUseCase(lotId) } returns flowOf(listOf(slot))
        coEvery { toggleSlotUseCase(any(), any(), any()) } returns Unit

        viewModel = ParkingViewModel(observeParkingUseCase, toggleSlotUseCase, configRepo, testDispatcher)

        // Esperar a que llegue a Success
        viewModel.state.test {
            awaitItem() // Loading
            awaitItem() // Success

            // Act
            viewModel.onSlotClicked(slot)
        }

        // Assert
        coVerify { toggleSlotUseCase(lotId, slot.id, SlotStatus.Occupied) }
    }
}