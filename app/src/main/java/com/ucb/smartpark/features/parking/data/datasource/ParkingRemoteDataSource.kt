package com.ucb.smartpark.features.parking.data.datasource

import android.util.Log
import com.google.firebase.database.*
import com.ucb.smartpark.features.parking.domain.model.ParkingSlot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ParkingRemoteDataSource(
    private val db: FirebaseDatabase
) {
    private val rootRef = db.getReference("parking")
    private val slotsRef = rootRef.child("slots")
    private val updatedAtRef = rootRef.child("updatedAt")
    private val auditRef = rootRef.child("audit")

    init {
        // Mantener sincronizado offline/online (ayuda a evitar vacíos por latencia)
        try {
            slotsRef.keepSynced(true)
        } catch (_: Exception) { /* ok en modo test */ }
    }

    fun observeSlots(): Flow<List<ParkingSlot>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("PARKING", "onDataChange() path=/parking/slots children=${snapshot.childrenCount} exists=${snapshot.exists()}")

                val list = if (!snapshot.exists() || !snapshot.children.iterator().hasNext()) {
                    // Fallback: 10 puestos libres para no dejar la UI en blanco
                    (1..10).map { ParkingSlot(id = it, isOccupied = false) }
                } else {
                    snapshot.children.mapNotNull { child ->
                        val key = child.key
                        val id = key?.toIntOrNull()
                        if (id == null) {
                            Log.w("PARKING", "child.key no es Int: key=$key")
                            return@mapNotNull null
                        }

                        // Lectura tolerante a tipos: Int, Long, String, Boolean
                        val occupiedInt: Int? =
                            child.getValue(Int::class.java)
                                ?: child.getValue(Long::class.java)?.toInt()
                                ?: child.getValue(String::class.java)?.toIntOrNull()
                                ?: (if (child.getValue(Boolean::class.java) == true) 1 else 0)

                        val occupied = occupiedInt == 1
                        Log.d("PARKING", "slot id=$id raw=${child.value} (${child.value?.javaClass?.simpleName}) -> occ=$occupied")
                        ParkingSlot(id = id, isOccupied = occupied)
                    }.sortedBy { it.id }
                }

                Log.d("PARKING", "emit slots=${list.size} -> ${list.joinToString { "${it.id}:${if (it.isOccupied) 1 else 0}" }}")
                trySend(list).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PARKING", "onCancelled: ${error.toException()}")
                close(error.toException())
            }
        }

        slotsRef.addValueEventListener(listener)
        awaitClose { slotsRef.removeEventListener(listener) }
    }

    suspend fun setSlotOccupied(slotId: Int, occupied: Boolean) {
        val now = System.currentTimeMillis()
        slotsRef.child(slotId.toString()).setValue(if (occupied) 1 else 0).await()
        updatedAtRef.setValue(now).await()

        val audit = mapOf(
            "slotId" to slotId,
            "occupied" to if (occupied) 1 else 0,
            "timestamp" to now
        )
        auditRef.push().setValue(audit).await()
    }
}
