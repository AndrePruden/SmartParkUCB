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
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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

    private lateinit var viewModel: ParkingViewModel

    @Test
    fun `init should show Maintenance state when config returns 3 (All Closed)`() = runTest {
        // Arrange
        // Config devolviendo 3 (Cerrado)
        coEvery { configRepo.fetchParkingStatus() } returns 3

        // Mockeamos observe para que no explote, aunque no debería llamarse si está cerrado
        coEvery { observeParkingUseCase(any()) } returns flowOf(emptyList())

        // Act (Al instanciar el ViewModel se ejecuta el init)
        viewModel = ParkingViewModel(observeParkingUseCase, toggleSlotUseCase, configRepo)

        // Assert
        viewModel.state.test {
            // El primer estado puede ser Loading o directamente Maintenance dependiendo de la velocidad
            val item = awaitItem()

            // Si el primero fue Loading, esperamos el siguiente
            val finalState = if (item is ParkingViewModel.UiState.Loading) awaitItem() else item

            assertTrue(finalState is ParkingViewModel.UiState.Maintenance)
            assertEquals("Este parqueo se encuentra cerrado por mantenimiento.", (finalState as ParkingViewModel.UiState.Maintenance).message)
        }

        // Verificamos que NO se llamó a observar parqueo porque estaba cerrado
        coVerify(exactly = 0) { observeParkingUseCase(any()) }
    }

    @Test
    fun `init should show Success state when config returns 0 (Open)`() = runTest {
        // Arrange
        val lotId = LotId("Tupuraya 1")
        val slots = listOf(ParkingSlot(SlotId(1), SlotStatus.Free))

        coEvery { configRepo.fetchParkingStatus() } returns 0 // Abierto
        coEvery { observeParkingUseCase(lotId) } returns flowOf(slots)

        // Act
        viewModel = ParkingViewModel(observeParkingUseCase, toggleSlotUseCase, configRepo)

        // Assert
        viewModel.state.test {
            // Saltamos el loading inicial si aparece
            val first = awaitItem()
            val result = if (first is ParkingViewModel.UiState.Loading) awaitItem() else first

            assertTrue(result is ParkingViewModel.UiState.Success)
            assertEquals(slots, (result as ParkingViewModel.UiState.Success).slots)
        }
    }

    @Test
    fun `onLotSelected should switch lot and check config again`() = runTest {
        // Arrange
        val lot1 = LotId("Tupuraya 1")
        val lot2 = LotId("Tupuraya 2")

        coEvery { configRepo.fetchParkingStatus() } returns 0 // Siempre abierto
        coEvery { observeParkingUseCase(any()) } returns flowOf(emptyList())

        viewModel = ParkingViewModel(observeParkingUseCase, toggleSlotUseCase, configRepo)

        // Act
        viewModel.onLotSelected(lot2)

        // Assert
        viewModel.selectedLot.test {
            assertEquals(lot2, awaitItem())
        }

        // Verificar que se llamó al repo con el nuevo ID
        coVerify { observeParkingUseCase(lot2) }
    }

    @Test
    fun `onSlotClicked should call toggle use case only if state is Success`() = runTest {
        // Arrange
        val lotId = LotId("Tupuraya 1")
        val slot = ParkingSlot(SlotId(1), SlotStatus.Free)

        coEvery { configRepo.fetchParkingStatus() } returns 0
        coEvery { observeParkingUseCase(lotId) } returns flowOf(listOf(slot))
        coEvery { toggleSlotUseCase(any(), any(), any()) } returns Unit

        viewModel = ParkingViewModel(observeParkingUseCase, toggleSlotUseCase, configRepo)

        // Esperar a que llegue a Success para poder hacer click
        viewModel.state.test {
            val state = awaitItem()
            if (state is ParkingViewModel.UiState.Loading) awaitItem() // Esperar Success

            // Act
            viewModel.onSlotClicked(slot)
            cancelAndIgnoreRemainingEvents()
        }

        // Assert
        // Verificamos que invierte el estado (Free -> Occupied)
        coVerify { toggleSlotUseCase(lotId, slot.id, SlotStatus.Occupied) }
    }
}