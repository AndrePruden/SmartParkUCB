package com.ucb.smartpark.features.parking.data.datasource

import android.util.Log
import com.google.firebase.database.*
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import com.ucb.smartpark.features.parking.domain.vo.LotId
import com.ucb.smartpark.features.parking.domain.vo.SlotId
import com.ucb.smartpark.features.parking.domain.vo.SlotStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ParkingRemoteDataSource(
    private val db: FirebaseDatabase
) {
    // Usamos .value para obtener el String real para Firebase
    private fun lotRef(lotId: LotId) = db.getReference("parking").child(lotId.value)
    private fun slotsRef(lotId: LotId) = lotRef(lotId).child("slots")
    private fun updatedAtRef(lotId: LotId) = lotRef(lotId).child("updatedAt")
    private fun auditRef(lotId: LotId) = lotRef(lotId).child("audit")

    fun observeSlots(lotId: LotId): Flow<List<ParkingSlot>> = callbackFlow {
        val ref = slotsRef(lotId)
        try { ref.keepSynced(true) } catch (_: Exception) {}

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = if (!snapshot.exists()) {
                    // Generar 32 slots vacíos con VOs
                    (1..32).map {
                        ParkingSlot(
                            id = SlotId(it),
                            status = SlotStatus.Free
                        )
                    }
                } else {
                    snapshot.children.mapNotNull { child ->
                        val idInt = child.key?.toIntOrNull() ?: return@mapNotNull null

                        val occupiedInt: Int? = child.getValue(Int::class.java)
                            ?: child.getValue(Long::class.java)?.toInt()
                            ?: child.getValue(String::class.java)?.toIntOrNull()
                            ?: (if (child.getValue(Boolean::class.java) == true) 1 else 0)

                        ParkingSlot(
                            id = SlotId(idInt),
                            status = SlotStatus(occupiedInt == 1)
                        )
                    }.sortedBy { it.id.value }
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun setSlotOccupied(lotId: LotId, slotId: SlotId, status: SlotStatus) {
        val now = System.currentTimeMillis()

        val occupiedInt = if (status.value) 1 else 0

        slotsRef(lotId).child(slotId.value.toString()).setValue(occupiedInt).await()
        updatedAtRef(lotId).setValue(now).await()

        val audit = mapOf(
            "slotId" to slotId.value,
            "occupied" to occupiedInt,
            "timestamp" to now
        )
        auditRef(lotId).push().setValue(audit).await()
    }
}