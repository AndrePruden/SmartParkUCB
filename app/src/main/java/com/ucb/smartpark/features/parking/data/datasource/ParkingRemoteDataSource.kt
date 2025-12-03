package com.ucb.smartpark.features.parking.data.datasource

import com.google.firebase.database.*
import com.ucb.smartpark.features.parking.data.mapper.toDomain // 👈 Importante
import com.ucb.smartpark.features.parking.data.model.ParkingSlotEntity // 👈 Importante
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
                    (1..32).map {
                        ParkingSlotEntity(it, false).toDomain()
                    }
                } else {
                    snapshot.children.mapNotNull { child ->
                        val idInt = child.key?.toIntOrNull() ?: return@mapNotNull null

                        // Lógica "sucia" de parsing de Firebase se queda aquí para crear la Entidad
                        val occupiedInt: Int? = child.getValue(Int::class.java)
                            ?: child.getValue(Long::class.java)?.toInt()
                            ?: child.getValue(String::class.java)?.toIntOrNull()
                            ?: (if (child.getValue(Boolean::class.java) == true) 1 else 0)

                        val isOccupied = (occupiedInt == 1)

                        // 1. Creamos el DTO/Entity (Datos puros)
                        ParkingSlotEntity(id = idInt, isOccupied = isOccupied)

                    }.map { entity ->
                        // 2. Mappeamos a Dominio usando la función que creamos
                        entity.toDomain()
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

    // ... el resto de funciones (setSlotOccupied) ...
    // En teoría setSlotOccupied también debería recibir un dominio y convertirlo a primitivos
    // pero como pasas IDs y Status sueltos, está pasable.
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