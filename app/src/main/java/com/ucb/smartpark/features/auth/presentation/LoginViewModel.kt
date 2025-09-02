package com.ucb.smartpark.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucb.smartpark.features.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    // _state es privado y mutable, solo el ViewModel puede cambiarlo.
    private val _state = MutableStateFlow(LoginState())
    // state es público e inmutable, la UI solo puede leerlo.
    val state = _state.asStateFlow()

    private val _loginEvent = MutableSharedFlow<Unit>()
    val loginEvent = _loginEvent.asSharedFlow()


    fun onEmailChange(email: String) {
        // Actualiza el estado con el nuevo email y limpia cualquier error previo.
        _state.update { currentState ->
            currentState.copy(email = email, error = null)
        }
    }

    fun onContrasenaChange(contrasena: String) {
        // Actualiza el estado con la nueva contraseña y limpia cualquier error previo.
        _state.update { currentState ->
            currentState.copy(contrasena = contrasena, error = null)
        }
    }

    fun onToggleContrasenaVisibility() {
        _state.update { currentState ->
            currentState.copy(contrasenaVisible = !currentState.contrasenaVisible)
        }
    }

    fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            loginUseCase(state.value.email, state.value.contrasena)
                .onSuccess {
                    _loginEvent.emit(Unit)
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message) }
                }
            _state.update { it.copy(isLoading = false) }
        }
    }
}