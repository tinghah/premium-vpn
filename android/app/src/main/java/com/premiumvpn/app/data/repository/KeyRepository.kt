package com.premiumvpn.app.data.repository

import com.premiumvpn.app.data.local.KeyDao
import com.premiumvpn.app.data.local.KeyEntity
import com.premiumvpn.app.data.remote.OutlineApiService
import com.premiumvpn.app.domain.model.KeyUsageStats
import com.premiumvpn.app.util.KeyParser
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyRepository @Inject constructor(
    private val keyDao: KeyDao,
    private val okHttpClient: OkHttpClient
) {

    fun getAllKeys(): Flow<List<KeyEntity>> = keyDao.getAllKeys()

    suspend fun addKey(ssUrl: String, name: String? = null): Result<KeyEntity> {
        return try {
            val parsed = KeyParser.parse(ssUrl)
                ?: return Result.failure(IllegalArgumentException("Invalid ss:// URL"))

            val key = KeyEntity(
                name = name ?: parsed.name,
                password = parsed.password,
                host = parsed.host,
                port = parsed.port,
                method = parsed.method,
                accessKeyUrl = ssUrl,
                serverApiSecret = null
            )

            keyDao.insertKey(key)
            Result.success(key)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteKey(id: String) = keyDao.deleteKey(id)

    suspend fun setActiveKey(id: String) {
        keyDao.clearActiveKey()
        keyDao.setActiveKey(id)
    }

    suspend fun getActiveKey(): KeyEntity? = keyDao.getActiveKey()

    suspend fun getKeyById(id: String): KeyEntity? = keyDao.getKeyById(id)

    suspend fun refreshKeyStats(id: String): Result<KeyUsageStats> {
        return try {
            val key = keyDao.getKeyById(id) ?: return Result.failure(Exception("Key not found"))

            // Create API service for this key's server
            val serverUrl = "https://${key.host}:${key.port}/"
            val api = OutlineApiService.create(serverUrl, okHttpClient)

            // Try to get metrics, default to 0 if API fails (cached view)
            val metrics = try {
                api.getMetrics()
            } catch (e: Exception) {
                null
            }

            val keyMetrics = metrics?.accessKeys?.find {
                it.accessKeyId.toString() == key.id
            }

            val usage = KeyUsageStats(
                keyId = id,
                bytesUsed = keyMetrics?.dataTransferred?.bytes ?: key.bytesUsed,
                tunnelTimeSeconds = keyMetrics?.tunnelTime?.seconds?.toLong() ?: 0,
                lastTrafficSeen = keyMetrics?.connection?.lastTrafficSeen?.toLong() ?: 0,
                peakDevices = keyMetrics?.connection?.peakDeviceCount?.data ?: 0,
                dataLimitBytes = key.dataLimitBytes
            )

            keyDao.updateKeyStats(id, usage.bytesUsed, System.currentTimeMillis())
            Result.success(usage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
