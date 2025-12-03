package com.ucb.smartpark.features.parking.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ucb.smartpark.R
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

// Colores personalizados
val AsphaltColor = Color(0xFF263238)
val ParkingLineColor = Color(0xFFECEFF1)

@Composable
fun ParkingScreen(
    vm: ParkingViewModel = koinViewModel()
) {
    val state by vm.state.collectAsState()
    val selectedLot by vm.selectedLot.collectAsState()

    // Estado para el Snackbar (Notificación negra inferior)
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para el Dialogo del Croquis
    var showCroquis by remember { mutableStateOf(false) }

    // Estado para el Scroll
    val scrollState = rememberScrollState()

    // Escuchar el evento de mensaje del ViewModel (Alerta de Mantenimiento)
    LaunchedEffect(key1 = true) {
        vm.uiMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    // --- DIALOGO FLOTANTE (CROQUIS) ---
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
                        Text(
                            text = "Croquis: ${selectedLot.value}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showCroquis = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Selección de imagen según el parqueo
                    val imageRes = if (selectedLot.value.contains("1")) {
                        R.drawable.tupuraya1
                    } else {
                        R.drawable.tupuraya2
                    }

                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Mapa",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    )
                }
            }
        }
    }

    // --- PANTALLA PRINCIPAL (Scaffold) ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState) // Habilitamos scroll vertical
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Selector de Parqueo + Botón de Info
                LotSelector(
                    lots = vm.lots,
                    selected = selectedLot,
                    onSelect = vm::onLotSelected,
                    onInfoClick = { showCroquis = true }
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

                    // --- MODO MANTENIMIENTO (CANDADO) ---
                    is ParkingViewModel.UiState.Maintenance -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp), // Altura fija para centrar contenido visualmente
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
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }

                    // --- MODO ÉXITO (MAPA REALISTA) ---
                    is ParkingViewModel.UiState.Success -> {
                        val map = s.slots.associateBy { it.id }
                        val all32 = (1..32).map { idInt ->
                            val idVo = SlotId(idInt)
                            map[idVo] ?: ParkingSlot(id = idVo, status = SlotStatus.Free)
                        }

                        // Dividir en columnas
                        val col1 = all32.slice(0..7)
                        val col2 = all32.slice(8..15)
                        val col3 = all32.slice(16..23)
                        val col4 = all32.slice(24..31)

                        val libres = all32.count { !it.status.value }
                        val ocupados = all32.size - libres

                        // Contenedor "Asfalto"
                        Surface(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            shape = RoundedCornerShape(12.dp),
                            color = AsphaltColor,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                ColumnSlots(slots = col1, onClick = { vm.onSlotClicked(it) }, modifier = Modifier.weight(1f, fill = false))
                                DrivingLane(modifier = Modifier.width(30.dp).height(260.dp))
                                ColumnSlots(slots = col2, onClick = { vm.onSlotClicked(it) }, modifier = Modifier.weight(1f, fill = false))
                                Spacer(Modifier.width(16.dp))
                                ColumnSlots(slots = col3, onClick = { vm.onSlotClicked(it) }, modifier = Modifier.weight(1f, fill = false))
                                DrivingLane(modifier = Modifier.width(30.dp).height(260.dp))
                                ColumnSlots(slots = col4, onClick = { vm.onSlotClicked(it) }, modifier = Modifier.weight(1f, fill = false))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Resumen inferior
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatusChip(count = libres, label = "Libres", color = Color(0xFF4CAF50))
                            StatusChip(count = ocupados, label = "Ocupados", color = Color(0xFFD32F2F))
                        }

                        // Espacio extra para scroll
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    )
}

// -----------------------------------------------------------------
// COMPONENTES UI AUXILIARES
// -----------------------------------------------------------------

@Composable
fun DrivingLane(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
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
            Text(text = count.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.width(8.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = color)
        }
    }
}

@Composable
private fun ColumnSlots(slots: List<ParkingSlot>, onClick: (ParkingSlot) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        slots.forEach { slot ->
            CarSlotRealistic(id = slot.id.value, occupied = slot.status.value, onClick = { onClick(slot) })
        }
    }
}

@Composable
private fun CarSlotRealistic(id: Int, occupied: Boolean, onClick: () -> Unit) {
    val bgColor = if (occupied) Color(0xFFD32F2F) else Color(0xFF4CAF50).copy(alpha = 0.8f)
    val contentColor = Color.White
    Surface(
        modifier = Modifier.width(50.dp).height(28.dp).clickable { onClick() },
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        shadowElevation = if (occupied) 4.dp else 0.dp,
        border = if (!occupied) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) else null
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (occupied) {
                Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text(text = id.toString(), style = MaterialTheme.typography.labelSmall, color = contentColor, fontWeight = FontWeight.Bold)
            }
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

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selected.value,
                onValueChange = {},
                label = { Text("Ubicación del Parqueo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                lots.forEach { lot ->
                    DropdownMenuItem(text = { Text(lot.value) }, onClick = { expanded = false; onSelect(lot) })
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        androidx.compose.material3.FilledTonalIconButton(onClick = onInfoClick) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "Ver croquis")
        }
    }
}