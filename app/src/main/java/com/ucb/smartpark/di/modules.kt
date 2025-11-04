package com.ucb.smartpark.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ucb.smartpark.features.auth.data.repository.AuthRepository
import com.ucb.smartpark.features.auth.domain.repository.IAuthRepository
import com.ucb.smartpark.features.auth.domain.usecase.LoginUseCase
import com.ucb.smartpark.features.auth.presentation.LoginViewModel
import com.ucb.smartpark.features.parking.data.datasource.ParkingRemoteDataSource
import com.ucb.smartpark.features.parking.data.repository.ParkingRepository
import com.ucb.smartpark.features.parking.domain.repository.IParkingRepository
import com.ucb.smartpark.features.parking.domain.usecase.ObserveParkingUseCase
import com.ucb.smartpark.features.parking.domain.usecase.ToggleSlotUseCase
import com.ucb.smartpark.features.parking.presentation.ParkingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // ---------- Auth ----------
    // El repositorio ahora necesita FirebaseAuth
    single<IAuthRepository> { AuthRepository(get()) } // 👈 Modificado
    factory { LoginUseCase(get()) }
    viewModel { LoginViewModel(get()) }

    // ---------- Firebase ----------
    // Instancia única para toda la app
    single {
        com.google.firebase.database.FirebaseDatabase.getInstance(
            "https://smartpark-bf660-default-rtdb.firebaseio.com/"
        )
    }
    // 👇 AÑADIR Instancia de FirebaseAuth
    single { FirebaseAuth.getInstance() }

    // ---------- Parking ----------
    single { ParkingRemoteDataSource(get()) }
    single<IParkingRepository> { ParkingRepository(get()) }
    factory { ObserveParkingUseCase(get()) }
    factory { ToggleSlotUseCase(get()) }
    viewModel { ParkingViewModel(get(), get()) }
}
