package com.ucb.smartpark.features.parking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

/**
 * Dibuja 10 puestos: 5 a la izquierda y 5 a la derecha, como el esquema.
 * Blanco = libre, Rojo = ocupado. Click alterna estado (modo demo).
 */
@Composable
fun ParkingScreen(
    vm: ParkingViewModel = koinViewModel()
) {
    val state by vm.state.collectAsState()

    when (val s = state) {
        is ParkingViewModel.UiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Cargando parqueo…")
            }
        }

        is ParkingViewModel.UiState.Error -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text(s.message) }

        is ParkingViewModel.UiState.Success -> {
            if (s.slots.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin datos de slots. Mostrando vista vacía.")
                }
                return
            }
            val left = s.slots.filter { it.id in 1..5 }
            val right = s.slots.filter { it.id in 6..10 }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Columna izquierda (slots 1..5)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    left.forEach { slot ->
                        CarSlot(
                            occupied = slot.isOccupied,
                            onClick = { vm.onSlotClicked(slot) }
                        )
                    }
                }

                // “calle” central
                Spacer(Modifier.width(24.dp))

                // Columna derecha (slots 6..10)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    right.forEach { slot ->
                        CarSlot(
                            occupied = slot.isOccupied,
                            onClick = { vm.onSlotClicked(slot) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarSlot(
    occupied: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .background(if (occupied) Color(0xFFD32F2F) else Color.White)
                .padding(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (occupied) "Ocupado" else "Libre",
                style = MaterialTheme.typography.bodyMedium,
                color = if (occupied) Color.White else Color.Black
            )
        }
    }
}
