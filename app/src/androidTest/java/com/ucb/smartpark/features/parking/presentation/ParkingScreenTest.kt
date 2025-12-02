package com.ucb.smartpark.features.parking.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class ParkingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mockeamos el ViewModel
    private val mockViewModel = mockk<ParkingViewModel>(relaxed = true)

    @Test
    fun parkingScreen_showsSlots_whenStateIsSuccess() {
        // Arrange
        val slot1 = ParkingSlot(SlotId(1), SlotStatus.Free)
        val slot2 = ParkingSlot(SlotId(5), SlotStatus.Occupied)

        every { mockViewModel.selectedLot } returns MutableStateFlow(LotId("Tupuraya 1"))
        every { mockViewModel.state } returns MutableStateFlow(
            ParkingViewModel.UiState.Success(listOf(slot1, slot2))
        )

        // Act
        composeTestRule.setContent {
            ParkingScreen(vm = mockViewModel)
        }

        // Assert
        // 1. Verificamos el título del parqueo
        composeTestRule.onNodeWithText("Tupuraya 1").assertIsDisplayed()

        // 2. Verificamos los slots por su ETIQUETA ÚNICA (Tag)
        composeTestRule.onNodeWithTag("slot_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slot_5").assertIsDisplayed()

        // (Eliminamos la verificación de hijos internos porque Compose fusiona las semánticas en elementos clickeables)
    }

    @Test
    fun parkingScreen_callsToggleSlot_onClick() {
        // Arrange
        val slot1 = ParkingSlot(SlotId(10), SlotStatus.Free)
        every { mockViewModel.selectedLot } returns MutableStateFlow(LotId("TestLot"))
        every { mockViewModel.state } returns MutableStateFlow(
            ParkingViewModel.UiState.Success(listOf(slot1))
        )

        composeTestRule.setContent {
            ParkingScreen(vm = mockViewModel)
        }

        // Act: Hacemos click usando el TAG (Más seguro que usar texto "10")
        composeTestRule.onNodeWithTag("slot_10").performClick()

        // Assert: Verificamos que se llamó a la función del ViewModel
        verify { mockViewModel.onSlotClicked(any()) }
    }

    @Test
    fun parkingScreen_showsMaintenance_whenStateIsMaintenance() {
        // Arrange
        val msg = "Cerrado por mantenimiento"
        every { mockViewModel.selectedLot } returns MutableStateFlow(LotId("TestLot"))
        every { mockViewModel.state } returns MutableStateFlow(
            ParkingViewModel.UiState.Maintenance(msg)
        )

        // Act
        composeTestRule.setContent {
            ParkingScreen(vm = mockViewModel)
        }

        // Assert
        composeTestRule.onNodeWithText(msg).assertIsDisplayed()
    }
}