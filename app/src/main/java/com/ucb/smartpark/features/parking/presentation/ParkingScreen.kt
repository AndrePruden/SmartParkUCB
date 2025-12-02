package com.ucb.smartpark.features.parking.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.platform.testTag

// --- PALETA DE COLORES ---
private val ParkingGreen = Color(0xFF43A047)   // Verde (Libre)
private val ParkingRed = Color(0xFFE53935)     // Rojo (Ocupado)
private val PavementColor = Color(0xFF455A64)  // Gris Asfalto (Blue Grey 700)
private val ParkingLineColor = Color(0xFFCFD8DC) // Color para líneas divisorias (opcional)

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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selector de Parqueo
        LotSelector(
            lots = vm.lots,
            selected = selectedLot,
            onSelect = vm::onLotSelected
        )

        Spacer(Modifier.height(24.dp))

        when (val s = state) {
            is ParkingViewModel.UiState.Loading -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = ParkingGreen)
                    Spacer(Modifier.width(12.dp))
                    Text("Cargando ${selectedLot.value}…")
                }
            }

            is ParkingViewModel.UiState.Error -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { Text(s.message, color = MaterialTheme.colorScheme.error) }

            is ParkingViewModel.UiState.Maintenance -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Cerrado",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = s.message,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is ParkingViewModel.UiState.Success -> {
                val map = s.slots.associateBy { it.id }

                val all32: List<ParkingSlot> = (1..32).map { idInt ->
                    val idVo = SlotId(idInt)
                    map[idVo] ?: ParkingSlot(id = idVo, status = SlotStatus.Free)
                }

                val col1 = all32.slice(0..7)
                val col2 = all32.slice(8..15)
                val col3 = all32.slice(16..23)
                val col4 = all32.slice(24..31)

                val libres = all32.count { !it.status.value }
                val ocupados = all32.size - libres

                // --- PLATAFORMA DE ASFALTO (CONTAINER) ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)) // Bordes redondeados del pavimento
                        .background(PavementColor)       // <--- AQUÍ ESTÁ EL GRIS ASFALTO
                        .padding(12.dp)                  // "Acera" interna
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Columna 1
                        ColumnSlots(
                            slots = col1,
                            onClick = { vm.onSlotClicked(it) },
                            modifier = Modifier.weight(1f)
                        )

                        // CALLE 1 (Se ve del color del asfalto)
                        Spacer(Modifier.width(32.dp))

                        // ISLA CENTRAL
                        ColumnSlots(
                            slots = col2,
                            onClick = { vm.onSlotClicked(it) },
                            modifier = Modifier.weight(1f)
                        )
                        // Separación central pequeña
                        Spacer(Modifier.width(4.dp))
                        ColumnSlots(
                            slots = col3,
                            onClick = { vm.onSlotClicked(it) },
                            modifier = Modifier.weight(1f)
                        )

                        // CALLE 2
                        Spacer(Modifier.width(32.dp))

                        // Columna 4
                        ColumnSlots(
                            slots = col4,
                            onClick = { vm.onSlotClicked(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // --- RESUMEN ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusCard(
                        label = "Libres",
                        count = libres,
                        color = ParkingGreen
                    )
                    StatusCard(
                        label = "Ocupados",
                        count = ocupados,
                        color = ParkingRed
                    )
                }
                Spacer(Modifier.height(16.dp))
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
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        slots.forEach { slot ->
            CarSlotCompact(
                id = slot.id.value,
                occupied = slot.status.value,
                onClick = { onClick(slot) }
            )
        }
    }
}

@Composable
private fun CarSlotCompact(
    id: Int,
    occupied: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (occupied) ParkingRed else ParkingGreen

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clickable { onClick() }
            .testTag("slot_$id"), // 👈 ¡AGREGA ESTA LÍNEA! (Ej: "slot_1", "slot_5")
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 4.dp,
        color = backgroundColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = id.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StatusCard(
    label: String,
    count: Int,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(130.dp)
            .height(55.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
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
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            readOnly = true,
            value = selected.value,
            onValueChange = {},
            label = { Text("Parqueo Seleccionado") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            lots.forEach { lot ->
                DropdownMenuItem(
                    text = { Text(lot.value) },
                    onClick = { expanded = false; onSelect(lot) }
                )
            }
        }
    }
}