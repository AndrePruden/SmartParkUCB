package com.ucb.smartpark.features.parking.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import org.koin.androidx.compose.koinViewModel

// Colores personalizados para el diseño de "Mapa"
val AsphaltColor = Color(0xFF263238) // Gris oscuro azulado
val ParkingLineColor = Color(0xFFECEFF1) // Blanco hueso
val GrassColor = Color(0xFF4CAF50) // Verde para bordes (opcional)

@Composable
fun ParkingScreen(
    vm: ParkingViewModel = koinViewModel()
) {
    val state by vm.state.collectAsState()
    val selectedLot by vm.selectedLot.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Selector de Parqueo
        LotSelector(
            lots = vm.lots,
            selected = selectedLot,
            onSelect = vm::onLotSelected
        )

        Spacer(Modifier.height(16.dp))

        when (val s = state) {
            is ParkingViewModel.UiState.Loading -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(Modifier.width(12.dp))
                    Text("Conectando con ${selectedLot.value}…")
                }
            }

            is ParkingViewModel.UiState.Error -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) { Text(s.message, color = MaterialTheme.colorScheme.error) }

            // Estado de Mantenimiento (Remote Config)
            is ParkingViewModel.UiState.Maintenance -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Cerrado",
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "PARQUEO CERRADO",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = s.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            is ParkingViewModel.UiState.Success -> {
                val map = s.slots.associateBy { it.id }

                val all32: List<ParkingSlot> = (1..32).map { idInt ->
                    val idVo = SlotId(idInt)
                    map[idVo] ?: ParkingSlot(id = idVo, status = SlotStatus.Free)
                }

                // Columnas: 1..8 | 9..16 | 17..24 | 25..32
                val col1 = all32.slice(0..7)
                val col2 = all32.slice(8..15)
                val col3 = all32.slice(16..23)
                val col4 = all32.slice(24..31)

                val libres = all32.count { !it.status.value }
                val ocupados = all32.size - libres

                // --- CONTENEDOR DEL MAPA DEL PARQUEO ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(12.dp),
                    color = AsphaltColor, // Fondo gris asfalto
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Bloque Izquierdo (Col 1 y 2)
                        ColumnSlots(
                            slots = col1,
                            onClick = { vm.onSlotClicked(it) },
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Calle Pequeña (Línea divisoria)
                        DrivingLane(
                            modifier = Modifier
                                .width(30.dp)
                                .height(260.dp) // Altura aproximada del bloque
                        )

                        ColumnSlots(
                            slots = col2,
                            onClick = { vm.onSlotClicked(it) },
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // CALLE PRINCIPAL (Separador grande)
                        Spacer(Modifier.width(16.dp))

                        // Bloque Derecho (Col 3 y 4)
                        ColumnSlots(
                            slots = col3,
                            onClick = { vm.onSlotClicked(it) },
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Calle Pequeña
                        DrivingLane(
                            modifier = Modifier
                                .width(30.dp)
                                .height(260.dp)
                        )

                        ColumnSlots(
                            slots = col4,
                            onClick = { vm.onSlotClicked(it) },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
                // ---------------------------------------

                Spacer(Modifier.height(16.dp))

                // Resumen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusChip(count = libres, label = "Libres", color = Color(0xFF4CAF50)) // Verde
                    StatusChip(count = ocupados, label = "Ocupados", color = Color(0xFFD32F2F)) // Rojo
                }
            }
        }
    }
}

/**
 * Dibuja una línea punteada para simular el carril de circulación
 */
@Composable
fun DrivingLane(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Dibujar línea punteada central
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(x = canvasWidth / 2, y = 0f),
                end = Offset(x = canvasWidth / 2, y = canvasHeight),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
            )
        }
    }
}

@Composable
fun StatusChip(count: Int, label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
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
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp), // Espacio entre autos
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        slots.forEach { slot ->
            CarSlotRealistic(
                id = slot.id.value,
                occupied = slot.status.value,
                onClick = { onClick(slot) }
            )
        }
    }
}

/**
 * Nuevo diseño de Slot más realista
 */
@Composable
private fun CarSlotRealistic(
    id: Int,
    occupied: Boolean,
    onClick: () -> Unit
) {
    // Si está ocupado: Rojo + Icono de Auto
    // Si está libre: Verde transparente + Borde (pintura de piso)
    val bgColor = if (occupied) Color(0xFFD32F2F) else Color(0xFF4CAF50).copy(alpha = 0.8f)
    val contentColor = Color.White

    Surface(
        modifier = Modifier
            .width(50.dp) // Ancho fijo para uniformidad
            .height(28.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        shadowElevation = if (occupied) 4.dp else 0.dp, // El auto tiene sombra, el piso no
        border = if (!occupied) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) else null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (occupied) {
                // Icono de auto
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                // Número de parqueo pintado en el piso
                Text(
                    text = id.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LotSelector(
    lots: List<LotId>,
    selected: LotId,
    onSelect: (LotId) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(), // Ocupar todo el ancho
            readOnly = true,
            value = selected.value,
            onValueChange = {},
            label = { Text("Ubicación del Parqueo") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            lots.forEach { lot ->
                DropdownMenuItem(
                    text = { Text(lot.value) },
                    onClick = {
                        expanded = false
                        onSelect(lot)
                    }
                )
            }
        }
    }
}