package com.ucb.smartpark.di

import android.content.Context
import com.ucb.smartpark.MainDispatcherRule // 👈 Asegúrate de importar esto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.ucb.smartpark.features.auth.presentation.LoginViewModel
import com.ucb.smartpark.features.notifications.presentation.NotificationsViewModel
import com.ucb.smartpark.features.parking.presentation.ParkingViewModel
import io.mockk.mockk
import org.junit.After
import org.junit.Rule // 👈 Importar Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class KoinModuleTest : KoinTest {

    // 👇 AGREGA ESTA REGLA PARA SIMULAR EL HILO PRINCIPAL
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `verify all viewmodels can be injected`() {
        // 1. Mocks de las dependencias externas
        val mockedAndroidContext = mockk<Context>(relaxed = true)

        val testFirebaseModule = module {
            single { mockk<FirebaseAuth>(relaxed = true) }
            single { mockk<FirebaseDatabase>(relaxed = true) }
            single { mockk<FirebaseRemoteConfig>(relaxed = true) }
        }

        // 2. Iniciamos Koin
        startKoin {
            androidContext(mockedAndroidContext)
            allowOverride(true)
            modules(appModule, testFirebaseModule)
        }

        // 3. PRUEBA DE FUEGO

        // Auth Feature
        val loginViewModel = get<LoginViewModel>()
        assert(loginViewModel != null)

        // Parking Feature
        val parkingViewModel = get<ParkingViewModel>()
        assert(parkingViewModel != null)

        // Notifications Feature
        val notifViewModel = get<NotificationsViewModel>()
        assert(notifViewModel != null)

        println("✅ Todos los ViewModels y sus dependencias se inyectaron correctamente.")
    }
}