package com.premiumvpn.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyDao {

    @Query("SELECT * FROM access_keys ORDER BY createdAt DESC")
    fun getAllKeys(): Flow<List<KeyEntity>>

    @Query("SELECT * FROM access_keys WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveKey(): KeyEntity?

    @Query("SELECT * FROM access_keys WHERE id = :id")
    suspend fun getKeyById(id: String): KeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: KeyEntity)

    @Update
    suspend fun updateKey(key: KeyEntity)

    @Query("UPDATE access_keys SET isActive = 0")
    suspend fun clearActiveKey()

    @Query("UPDATE access_keys SET isActive = 1 WHERE id = :id")
    suspend fun setActiveKey(id: String)

    @Query("DELETE FROM access_keys WHERE id = :id")
    suspend fun deleteKey(id: String)

    @Query("UPDATE access_keys SET bytesUsed = :bytesUsed, lastConnectedAt = :timestamp WHERE id = :id")
    suspend fun updateKeyStats(id: String, bytesUsed: Long, timestamp: Long)
}
