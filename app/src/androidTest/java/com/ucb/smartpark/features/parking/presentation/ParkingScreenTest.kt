package com.ucb.smartpark.features.parking.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
        // Arrange: Preparamos datos falsos (2 slots para no llenar la pantalla)
        val slot1 = ParkingSlot(SlotId(1), SlotStatus.Free)
        val slot2 = ParkingSlot(SlotId(5), SlotStatus.Occupied) // Un ocupado

        // Simulamos que el usuario seleccionó "Tupuraya 1"
        every { mockViewModel.selectedLot } returns MutableStateFlow(LotId("Tupuraya 1"))

        // Simulamos estado de ÉXITO con la lista de slots
        every { mockViewModel.state } returns MutableStateFlow(
            ParkingViewModel.UiState.Success(listOf(slot1, slot2))
        )

        // Act
        composeTestRule.setContent {
            ParkingScreen(vm = mockViewModel)
        }

        // Assert
        // 1. Verificamos que se vea el nombre del parqueo seleccionado
        composeTestRule.onNodeWithText("Tupuraya 1").assertIsDisplayed()

        // 2. Verificamos que aparezcan los números de los slots
        // Nota: En tu UI usas CarSlotCompact que muestra el ID como texto
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
        composeTestRule.onNodeWithText("5").assertIsDisplayed()

        // 3. Verificamos los textos de resumen
        // Como mandamos 2 slots (uno libre, uno ocupado) + 30 generados vacíos en tu lógica de UI...
        // Espera, tu UI genera los 32 slots forzados si no vienen en la lista.
        // Mejor verifiquemos solo que los elementos existen.
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

        // Act: Hacemos click en el slot número 10
        composeTestRule.onNodeWithText("10").performClick()

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