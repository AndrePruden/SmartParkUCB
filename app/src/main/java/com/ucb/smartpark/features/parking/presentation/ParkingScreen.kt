package com.ucb.smartpark.features.parking.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource // 👈 IMPORTANTE PARA LOS TEXTOS
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ucb.smartpark.R
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import org.koin.androidx.compose.koinViewModel

// --- PALETA DE COLORES ---
private val ParkingGreen = Color(0xFF43A047)   // Verde (Libre)
private val ParkingRed = Color(0xFFE53935)     // Rojo (Ocupado)
private val PavementColor = Color(0xFF263238)  // Gris Asfalto Oscuro
private val LaneLineColor = Color.White.copy(alpha = 0.5f) // Color líneas calle

@Composable
fun ParkingScreen(
    vm: ParkingViewModel = koinViewModel()
) {
    val state by vm.state.collectAsState()
    val selectedLot by vm.selectedLot.collectAsState()

    // Estado para Snackbar y Dialogos
    val snackbarHostState = remember { SnackbarHostState() }
    var showCroquis by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // --- DIÁLOGO DE CROQUIS ---
    if (showCroquis) {
        Dialog(onDismissRequest = { showCroquis = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Uso de stringResource con parámetro (%1$s)
                        Text(
                            text = stringResource(R.string.croquis_title, selectedLot.value),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showCroquis = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close_desc)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Selección de imagen según el parqueo
                    val imageRes = if (selectedLot.value.contains("1")) R.drawable.tupuraya1 else R.drawable.tupuraya2

                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = stringResource(R.string.parking_map_desc),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    )
                }
            }
        }
    }

    // --- UI PRINCIPAL ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selector de Parqueo + Botón Info
            LotSelector(
                lots = vm.lots,
                selected = selectedLot,
                onSelect = vm::onLotSelected,
                onInfoClick = { showCroquis = true }
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
                        // stringResource con parámetro dinámico
                        Text(text = stringResource(R.string.loading_parking, selectedLot.value))
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
                            contentDescription = stringResource(R.string.closed_status),
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
                    // Preparamos los datos
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

                    // --- ZONA DE PARQUEO (ASFALTO) ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(PavementColor)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Top
                        ) {
                            // COLUMNA 1
                            ColumnSlots(slots = col1, onClick = { vm.onSlotClicked(it) }, modifier = Modifier.weight(1f))

                            // CALLE 1
                            DrivingLane(modifier = Modifier.width(32.dp).height(280.dp))

                            // ISLA CENTRAL
                            ColumnSlots(slots = col2, onClick = { vm.onSlotClicked(it) }, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(4.dp))
                            ColumnSlots(slots = col3, onClick = { vm.onSlotClicked(it) }, modifier = Modifier.weight(1f))

                            // CALLE 2
                            DrivingLane(modifier = Modifier.width(32.dp).height(280.dp))

                            // COLUMNA 4
                            ColumnSlots(slots = col4, onClick = { vm.onSlotClicked(it) }, modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- RESUMEN ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatusCard(
                            label = stringResource(R.string.free_slots),
                            count = libres,
                            color = ParkingGreen
                        )
                        StatusCard(
                            label = stringResource(R.string.occupied_slots),
                            count = ocupados,
                            color = ParkingRed
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun DrivingLane(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            drawLine(
                color = LaneLineColor,
                start = Offset(x = canvasWidth / 2, y = 0f),
                end = Offset(x = canvasWidth / 2, y = canvasHeight),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
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
            .testTag("slot_$id"),
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
    onSelect: (LotId) -> Unit,
    onInfoClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = selected.value,
                onValueChange = {},
                label = { Text(stringResource(R.string.parking_selected_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
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

        Spacer(Modifier.width(8.dp))

        FilledTonalIconButton(onClick = onInfoClick) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.view_croquis_desc)
            )
        }
    }
}