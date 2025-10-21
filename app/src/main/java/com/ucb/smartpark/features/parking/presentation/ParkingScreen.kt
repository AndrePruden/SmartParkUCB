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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot

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
            // Garantiza 32 slots (1..32)
            val map = s.slots.associateBy { it.id }
            val all32: List<ParkingSlot> = (1..32).map { id ->
                map[id] ?: ParkingSlot(id = id, isOccupied = false)
            }

            // Columnas: 1..8 | 9..16 | 17..24 | 25..32
            val col1 = all32.slice(0..7)
            val col2 = all32.slice(8..15)
            val col3 = all32.slice(16..23)
            val col4 = all32.slice(24..31)

            val libres = all32.count { !it.isOccupied }
            val ocupados = all32.size - libres

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Resumen superior
                Spacer(Modifier.height(30.dp))

                // TABLA: anclada arriba y con ALTO envuelto (no llena toda la pantalla)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.Top), // <- clave
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    ColumnSlots(
                        slots = col1,
                        onClick = { vm.onSlotClicked(it) },
                        modifier = Modifier.weight(1f, fill = false) // no obliga alto
                    )

                    // CALLE 1
                    Spacer(Modifier.width(36.dp))

                    ColumnSlots(
                        slots = col2,
                        onClick = { vm.onSlotClicked(it) },
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Gap interno
                    Spacer(Modifier.width(16.dp))

                    ColumnSlots(
                        slots = col3,
                        onClick = { vm.onSlotClicked(it) },
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // CALLE 2
                    Spacer(Modifier.width(36.dp))

                    ColumnSlots(
                        slots = col4,
                        onClick = { vm.onSlotClicked(it) },
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // Contenido inmediatamente debajo de la tabla
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Libres: $libres   |   Ocupados: $ocupados",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(12.dp))

                // 👉 Aquí ya puedes agregar más secciones/widgets sin que la tabla empuje hacia abajo
                // Ejemplo:
                // Button(onClick = { /* ... */ }) { Text("Reservar") }
            }
        }
    }
}

@Composable
private fun ColumnSlots(
    slots: List<ParkingSlot>,
    onClick: (ParkingSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier, // <- sin fillMaxHeight
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        slots.forEach { slot ->
            CarSlotCompact(
                id = slot.id,
                occupied = slot.isOccupied,
                onClick = { onClick(slot) }
            )
        }
    }
}

/** Slot compacto: ~22dp de alto, solo número, rojo/plomo. */
@Composable
private fun CarSlotCompact(
    id: Int,
    occupied: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .background(if (occupied) Color(0xFFD32F2F) else Color(0xFF9E9E9E)) // plomo si libre
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = id.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}
