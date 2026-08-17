package com.premiumvpn.app.data.remote

import com.premiumvpn.app.data.remote.dto.AccessKeysResponse
import com.premiumvpn.app.data.remote.dto.ServerInfo
import com.premiumvpn.app.data.remote.dto.ServerMetrics
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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

    companion object {
        /**
         * Create an OutlineApiService for a specific server.
         * The baseUrl must include the secret path, e.g.:
         * "https://myserver.com:1234/SECRET_PATH/"
         */
        fun create(baseUrl: String, client: OkHttpClient): OutlineApiService {
            val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            return Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OutlineApiService::class.java)
        }
    }
}
