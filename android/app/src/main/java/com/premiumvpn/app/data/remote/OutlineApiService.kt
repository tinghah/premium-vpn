package com.premiumvpn.app.data.remote

import com.premiumvpn.app.data.remote.dto.AccessKeysResponse
import com.premiumvpn.app.data.remote.dto.ServerInfo
import com.premiumvpn.app.data.remote.dto.ServerMetrics
import retrofit2.http.GET
import retrofit2.http.Query

interface OutlineApiService {

    @GET("server")
    suspend fun getServerInfo(): ServerInfo

    @GET("access-keys")
    suspend fun getAccessKeys(): AccessKeysResponse

    @GET("experimental/server/metrics")
    suspend fun getMetrics(
        @Query("since") since: String? = null
    ): ServerMetrics
}
