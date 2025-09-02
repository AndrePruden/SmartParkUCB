package com.ucb.smartpark.features.auth.presentation

// Esta clase representa todos los estados posibles de nuestra pantalla de login.
data class LoginState(
    val email: String = "",
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)