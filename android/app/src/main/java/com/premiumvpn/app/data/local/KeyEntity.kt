package com.premiumvpn.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "access_keys")
data class KeyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val password: String,
    val host: String,
    val port: Int,
    val method: String = "aes-256-gcm",
    val accessKeyUrl: String,
    val serverApiSecret: String? = null,
    val dataLimitBytes: Long? = null,
    val bytesUsed: Long = 0,
    val isActive: Boolean = false,
    val lastConnectedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
