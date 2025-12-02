package com.ucb.smartpark.features.parking.domain.model

import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus

data class ParkingSlot(
    val id: SlotId,
    val status: SlotStatus
)