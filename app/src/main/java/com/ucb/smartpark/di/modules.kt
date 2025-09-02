package com.ucb.smartpark.di

import com.ucb.smartpark.features.auth.data.repository.AuthRepository
import com.ucb.smartpark.features.auth.domain.repository.IAuthRepository
import com.ucb.smartpark.features.auth.domain.usecase.LoginUseCase
import com.ucb.smartpark.features.auth.presentation.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Auth Feature
    single<IAuthRepository> { AuthRepository() }
    factory { LoginUseCase(get()) }
    viewModel { LoginViewModel(get()) }
}