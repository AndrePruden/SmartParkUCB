package com.ucb.smartpark.features.notifications.data.repository

import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RemoteConfigRepository(
    private val remoteConfig: FirebaseRemoteConfig
) {
    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf("parking_status" to 0))
    }

    suspend fun fetchParkingStatus(): Int {
        return try {
            // Fuerza la descarga y activación inmediata
            remoteConfig.fetchAndActivate().await()
            remoteConfig.getLong("parking_status").toInt()
        } catch (e: Exception) {
            // Si falla (ej. sin internet), usa el valor en caché o default
            remoteConfig.getLong("parking_status").toInt()
        }
    }

    fun observeConfigUpdates(): Flow<Int> = callbackFlow {
        val listener = object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                if (configUpdate.updatedKeys.contains("parking_status")) {
                    remoteConfig.activate().addOnCompleteListener {
                        val newValue = remoteConfig.getLong("parking_status").toInt()
                        trySend(newValue)
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                // Manejo de error silencioso
            }
        }

        val registration = remoteConfig.addOnConfigUpdateListener(listener)
        awaitClose { registration.remove() }
    }
}