package com.ucb.smartpark.features.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ucb.smartpark.features.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _loginEvent = MutableSharedFlow<Unit>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val _launchGoogleSignIn = MutableSharedFlow<Unit>()
    val launchGoogleSignIn = _launchGoogleSignIn.asSharedFlow()

    /**
     * La UI llama a esto cuando el usuario presiona "Continuar con Google"
     */
    fun onSignInClick() {
        viewModelScope.launch {
            _launchGoogleSignIn.emit(Unit)
        }
    }

    /**
     * La UI llama a esto con el resultado exitoso del popup de Google.
     */
    fun onGoogleSignInSuccess(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            loginUseCase(account) // El UseCase valida el @ucb.edu.bo
                .onSuccess {
                    // Éxito, navegar a la siguiente pantalla
                    _loginEvent.emit(Unit)
                }
                .onFailure { error ->
                    // Error (ej: no es @ucb.edu.bo, o error de Firebase)
                    _state.update { it.copy(error = error.message) }
                }

            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onGoogleSignInError(message: String?) {
        _state.update {
            it.copy(error = message ?: "Ocurrió un error desconocido")
        }
    }
}