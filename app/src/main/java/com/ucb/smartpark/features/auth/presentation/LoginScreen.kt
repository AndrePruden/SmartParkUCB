package com.ucb.smartpark.features.auth.presentation

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts // 👈 Importar este
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.ucb.smartpark.R
import com.ucb.smartpark.ui.theme.UcbYellow
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                viewModel.onGoogleSignInSuccess(account)
            } catch (e: ApiException) {
                Log.w("LoginScreen", "Google sign in failed", e)
                viewModel.onGoogleSignInError(e.message)
            }
        } else {
            Log.w("LoginScreen", "Google sign in cancelled by user. Result code: ${result.resultCode}")
            viewModel.onGoogleSignInError("Inicio de sesión cancelado")
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.loginEvent.collectLatest {
            onLoginSuccess()
        }
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.launchGoogleSignIn.collectLatest {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.smart_park_logo),
                contentDescription = "Logo de Smart Park",
                modifier = Modifier.width(250.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            val errorMessage = state.error
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = viewModel::onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = UcbYellow)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    // TODO: añadir el icono de Google aquí
                    Text("CONTINUAR CON GOOGLE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = viewModel::onGuestLoginClick,
                enabled = !state.isLoading
            ) {
                Text(
                    text = "Entrar sin Iniciar Sesión",
                    color = Color.Gray, // Color discreto
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}