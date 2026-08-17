package com.premiumvpn.app.data.remote

import com.premiumvpn.app.data.remote.dto.AccessKeyDto
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val keys: List<AccessKeyDto>
)

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
