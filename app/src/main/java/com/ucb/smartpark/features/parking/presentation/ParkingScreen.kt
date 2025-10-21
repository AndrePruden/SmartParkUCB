package com.ucb.smartpark.features.parking.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import org.koin.androidx.compose.koinViewModel

@Composable
fun ParkingScreen(
    vm: ParkingViewModel = koinViewModel()
) {
    val state by vm.state.collectAsState()
    val selectedLot by vm.selectedLot.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Selector de Parqueo
        LotSelector(
            lots = vm.lots,
            selected = selectedLot,
            onSelect = vm::onLotSelected
        )

        Spacer(Modifier.height(12.dp))

        when (val s = state) {
            is ParkingViewModel.UiState.Loading -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(Modifier.width(12.dp))
                    Text("Cargando $selectedLot…")
                }
            }

            is ParkingViewModel.UiState.Error -> Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart
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

                // TABLA: anclada arriba y con alto envuelto
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.Top),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    ColumnSlots(
                        slots = col1,
                        onClick = { vm.onSlotClicked(it) },
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(Modifier.width(36.dp)) // CALLE 1

                    ColumnSlots(
                        slots = col2,
                        onClick = { vm.onSlotClicked(it) },
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(Modifier.width(16.dp)) // gap interno

                    ColumnSlots(
                        slots = col3,
                        onClick = { vm.onSlotClicked(it) },
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(Modifier.width(36.dp)) // CALLE 2

                    ColumnSlots(
                        slots = col4,
                        onClick = { vm.onSlotClicked(it) },
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Libres: $libres   |   Ocupados: $ocupados",
                    style = MaterialTheme.typography.labelLarge
                )

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
                id = slot.id,
                occupied = slot.isOccupied,
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
                .background(if (occupied) Color(0xFFD32F2F) else Color(0xFF9E9E9E))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LotSelector(
    lots: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .widthIn(min = 180.dp),
            readOnly = true,
            value = selected,
            onValueChange = {},
            label = { Text("Parqueo") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            lots.forEach { lot ->
                DropdownMenuItem(
                    text = { Text(lot) },
                    onClick = {
                        expanded = false
                        onSelect(lot)
                    }
                )
            }
        }
    }
}
