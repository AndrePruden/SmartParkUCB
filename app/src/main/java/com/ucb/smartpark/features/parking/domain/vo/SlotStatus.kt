package com.ucb.smartpark.features.parking.domain.vo

@JvmInline
value class SlotStatus(val value: Boolean) {
    companion object {
        val Occupied = SlotStatus(true)
        val Free = SlotStatus(false)
    }
}