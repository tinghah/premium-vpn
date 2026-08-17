package com.premiumvpn.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ServerInfo(
    @SerializedName("name") val name: String,
    @SerializedName("serverId") val serverId: String,
    @SerializedName("metricsEnabled") val metricsEnabled: Boolean,
    @SerializedName("createdTimestampMs") val createdTimestampMs: Long,
    @SerializedName("version") val version: String,
    @SerializedName("portForNewAccessKeys") val portForNewAccessKeys: Int,
    @SerializedName("hostnameForAccessKeys") val hostnameForAccessKeys: String?,
    @SerializedName("accessKeyDataLimit") val accessKeyDataLimit: DataLimitDto?
)

data class DataLimitDto(
    @SerializedName("bytes") val bytes: Long
)

data class AccessKeysResponse(
    @SerializedName("accessKeys") val accessKeys: List<AccessKeyDto>
)

data class AccessKeyDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("port") val port: Int?,
    @SerializedName("method") val method: String?,
    @SerializedName("accessUrl") val accessUrl: String?,
    @SerializedName("limit") val limit: DataLimitDto?
)

data class ServerMetrics(
    @SerializedName("server") val server: ServerStatsDto,
    @SerializedName("accessKeys") val accessKeys: List<AccessKeyMetricsDto>
)

data class ServerStatsDto(
    @SerializedName("tunnelTime") val tunnelTime: DurationDto,
    @SerializedName("dataTransferred") val dataTransferred: BytesDto,
    @SerializedName("bandwidth") val bandwidth: BandwidthDto?,
    @SerializedName("locations") val locations: List<LocationDto>?
)

data class DurationDto(
    @SerializedName("seconds") val seconds: Double
)

data class BytesDto(
    @SerializedName("bytes") val bytes: Long
)

data class BandwidthDto(
    @SerializedName("current") val current: BandwidthDataDto?,
    @SerializedName("peak") val peak: BandwidthDataDto?
)

data class BandwidthDataDto(
    @SerializedName("data") val data: BytesDto?,
    @SerializedName("timestamp") val timestamp: Long?
)

data class LocationDto(
    @SerializedName("location") val location: String?,
    @SerializedName("asn") val asn: Int?,
    @SerializedName("asOrg") val asOrg: String?,
    @SerializedName("tunnelTime") val tunnelTime: DurationDto,
    @SerializedName("dataTransferred") val dataTransferred: BytesDto
)

data class AccessKeyMetricsDto(
    @SerializedName("accessKeyId") val accessKeyId: Int,
    @SerializedName("tunnelTime") val tunnelTime: DurationDto,
    @SerializedName("dataTransferred") val dataTransferred: BytesDto,
    @SerializedName("connection") val connection: ConnectionDto?
)

data class ConnectionDto(
    @SerializedName("lastTrafficSeen") val lastTrafficSeen: Double?,
    @SerializedName("peakDeviceCount") val peakDeviceCount: PeakDeviceDto?
)

data class PeakDeviceDto(
    @SerializedName("data") val data: Int?,
    @SerializedName("timestamp") val timestamp: Long?
)
