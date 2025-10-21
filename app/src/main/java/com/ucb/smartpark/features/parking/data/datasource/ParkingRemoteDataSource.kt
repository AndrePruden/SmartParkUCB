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
    private fun lotRef(lotId: String) = db.getReference("parking").child(lotId)
    private fun slotsRef(lotId: String) = lotRef(lotId).child("slots")
    private fun updatedAtRef(lotId: String) = lotRef(lotId).child("updatedAt")
    private fun auditRef(lotId: String) = lotRef(lotId).child("audit")

    fun observeSlots(lotId: String): Flow<List<ParkingSlot>> = callbackFlow {
        val ref = slotsRef(lotId)

        try { ref.keepSynced(true) } catch (_: Exception) {}

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("PARKING", "[$lotId] onDataChange slots children=${snapshot.childrenCount} exists=${snapshot.exists()}")

                val list = if (!snapshot.exists()) {

                    (1..32).map { ParkingSlot(id = it, isOccupied = false) }
                } else {
                    snapshot.children.mapNotNull { child ->
                        val id = child.key?.toIntOrNull()
                        if (id == null) {
                            Log.w("PARKING", "[$lotId] child.key no es Int: key=${child.key}")
                            return@mapNotNull null
                        }


                        val occupiedInt: Int? =
                            child.getValue(Int::class.java)
                                ?: child.getValue(Long::class.java)?.toInt()
                                ?: child.getValue(String::class.java)?.toIntOrNull()
                                ?: (if (child.getValue(Boolean::class.java) == true) 1 else 0)

                        ParkingSlot(id = id, isOccupied = (occupiedInt == 1))
                    }.sortedBy { it.id }
                }

                trySend(list).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PARKING", "[$lotId] onCancelled: ${error.toException()}")
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun setSlotOccupied(lotId: String, slotId: Int, occupied: Boolean) {
        val now = System.currentTimeMillis()
        slotsRef(lotId).child(slotId.toString()).setValue(if (occupied) 1 else 0).await()
        updatedAtRef(lotId).setValue(now).await()

        val audit = mapOf(
            "slotId" to slotId,
            "occupied" to if (occupied) 1 else 0,
            "timestamp" to now
        )
        auditRef(lotId).push().setValue(audit).await()
    }
}
