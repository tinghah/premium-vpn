package com.premiumvpn.app.data.repository

import com.premiumvpn.app.data.remote.OutlineApiService
import com.premiumvpn.app.data.remote.dto.ServerInfo
import com.premiumvpn.app.data.remote.dto.ServerMetrics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val outlineApi: OutlineApiService
) {

    suspend fun getServerInfo(): Result<ServerInfo> {
        return try {
            Result.success(outlineApi.getServerInfo())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMetrics(since: String? = null): Result<ServerMetrics> {
        return try {
            Result.success(outlineApi.getMetrics(since))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
