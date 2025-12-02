package com.ucb.smartpark.features.notifications.data.repository

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await

class RemoteConfigRepository(
    private val remoteConfig: FirebaseRemoteConfig
) {
    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 30
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(mapOf("parking_status" to 0))
    }

    suspend fun fetchParkingStatus(): Int {
        return try {
            remoteConfig.fetchAndActivate().await()
            remoteConfig.getLong("parking_status").toInt()
        } catch (e: Exception) {
            remoteConfig.getLong("parking_status").toInt()
        }
    }
}