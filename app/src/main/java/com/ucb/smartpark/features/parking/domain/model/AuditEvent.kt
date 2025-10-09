package com.ucb.smartpark.features.parking.domain.model

data class AuditEvent(
    val slotId: Int,
    val occupied: Boolean,
    val timestamp: Long
)
