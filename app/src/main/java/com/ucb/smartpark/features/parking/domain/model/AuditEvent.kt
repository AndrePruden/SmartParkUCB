package com.ucb.smartpark.features.parking.domain.model

import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import com.ucb.smartpark.features.parking.domain.vo.Timestamp

data class AuditEvent(
    val slotId: SlotId,
    val status: SlotStatus,
    val timestamp: Timestamp
)