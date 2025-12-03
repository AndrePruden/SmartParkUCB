package com.ucb.smartpark.features.parking.data.mapper

import com.ucb.smartpark.features.parking.data.model.ParkingSlotEntity
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus

// Extension function para convertir de Entidad (Data) a Modelo (Domain)
fun ParkingSlotEntity.toDomain(): ParkingSlot {
    return ParkingSlot(
        id = SlotId(this.id),
        status = if (this.isOccupied) SlotStatus.Occupied else SlotStatus.Free
    )
}

// Opcional: Si necesitaras enviar datos A Firebase, harías el inverso:
// fun ParkingSlot.toEntity(): ParkingSlotEntity { ... }