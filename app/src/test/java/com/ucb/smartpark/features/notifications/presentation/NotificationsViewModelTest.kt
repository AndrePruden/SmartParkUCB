package com.ucb.smartpark.features.notifications.presentation

import app.cash.turbine.test
import com.ucb.smartpark.MainDispatcherRule
import com.ucb.smartpark.features.notifications.data.repository.RemoteConfigRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class NotificationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val configRepo: RemoteConfigRepository = mockk()
    private lateinit var viewModel: NotificationsViewModel

    @Test
    fun `fetchStatus should show Normal message when status is 0`() = runTest {
        // Arrange
        // 💡 AGREGAMOS DELAY para poder ver el estado "Cargando"
        coEvery { configRepo.fetchParkingStatus() } coAnswers {
            delay(100)
            0
        }

        // Act
        viewModel = NotificationsViewModel(configRepo)

        // Assert
        viewModel.statusMessage.test {
            // 1. Ahora sí atrapamos el estado inicial
            assertEquals("Cargando notificaciones...", awaitItem())

            // 2. Esperamos a que pase el delay y llegue el éxito
            assertEquals("✅ Todo normal. Los parqueos están operando habitualmente.", awaitItem())
        }
    }

    @Test
    fun `fetchStatus should show Warning Tupuraya 1 when status is 1`() = runTest {
        // Arrange
        coEvery { configRepo.fetchParkingStatus() } coAnswers {
            delay(100)
            1
        }

        // Act
        viewModel = NotificationsViewModel(configRepo)

        // Assert
        viewModel.statusMessage.test {
            assertEquals("Cargando notificaciones...", awaitItem())
            assertEquals("⚠️ AVISO: El parqueo 'Tupuraya 1' se encuentra cerrado por mantenimiento.", awaitItem())
        }
    }

    @Test
    fun `fetchStatus should show Warning Tupuraya 2 when status is 2`() = runTest {
        // Arrange
        coEvery { configRepo.fetchParkingStatus() } coAnswers {
            delay(100)
            2
        }

        // Act
        viewModel = NotificationsViewModel(configRepo)

        // Assert
        viewModel.statusMessage.test {
            assertEquals("Cargando notificaciones...", awaitItem())
            assertEquals("⚠️ AVISO: El parqueo 'Tupuraya 2' se encuentra cerrado por mantenimiento.", awaitItem())
        }
    }

    @Test
    fun `fetchStatus should show Alert All Closed when status is 3`() = runTest {
        // Arrange
        coEvery { configRepo.fetchParkingStatus() } coAnswers {
            delay(100)
            3
        }

        // Act
        viewModel = NotificationsViewModel(configRepo)

        // Assert
        viewModel.statusMessage.test {
            assertEquals("Cargando notificaciones...", awaitItem())
            assertEquals("⛔ ALERTA: Ambos parqueos están cerrados temporalmente.", awaitItem())
        }
    }

    @Test
    fun `manual fetchStatus should update message`() = runTest {
        // Arrange
        // Simulamos dos llamadas: la primera devuelve 0, la segunda 3
        var callCount = 0
        coEvery { configRepo.fetchParkingStatus() } coAnswers {
            delay(100) // Delay para que se note el cambio
            if (callCount == 0) {
                callCount++
                0
            } else {
                3
            }
        }

        viewModel = NotificationsViewModel(configRepo)

        viewModel.statusMessage.test {
            // Flujo inicial (init)
            assertEquals("Cargando notificaciones...", awaitItem())
            assertEquals("✅ Todo normal. Los parqueos están operando habitualmente.", awaitItem())

            // Act: Forzamos actualización manual
            viewModel.fetchStatus()

            // Flujo de actualización (debe aparecer el mensaje final de alerta)
            // Nota: Dependiendo de cómo funcione StateFlow, podría no emitir "Cargando" de nuevo si es muy rápido,
            // pero el delay ayuda a asegurar que el valor cambie.
            assertEquals("⛔ ALERTA: Ambos parqueos están cerrados temporalmente.", awaitItem())
        }
    }
}