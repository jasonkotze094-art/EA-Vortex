package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LicenseDao {

    // Insertion & Updates
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLicenseKey(key: LicenseKey): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLicenseKeys(keys: List<LicenseKey>): List<Long>

    @Update
    suspend fun updateLicenseKey(key: LicenseKey)

    // Deletion
    @Delete
    suspend fun deleteLicenseKey(key: LicenseKey)

    @Query("DELETE FROM license_keys WHERE id = :id")
    suspend fun deleteLicenseKeyById(id: Long)

    @Query("DELETE FROM license_keys WHERE keyCode = :keyCode")
    suspend fun deleteLicenseKeyByCode(keyCode: String)

    // Reactive Queries by Date and Status
    @Query("SELECT * FROM license_keys ORDER BY createdTimestamp DESC")
    fun getAllLicenseKeys(): Flow<List<LicenseKey>>

    @Query("SELECT * FROM license_keys WHERE id = :id LIMIT 1")
    suspend fun getLicenseKeyById(id: Long): LicenseKey?

    @Query("SELECT * FROM license_keys WHERE keyCode = :keyCode LIMIT 1")
    suspend fun getLicenseKeyByCode(keyCode: String): LicenseKey?

    @Query("SELECT * FROM license_keys WHERE status = :status ORDER BY createdTimestamp DESC")
    fun getLicenseKeysByStatus(status: String): Flow<List<LicenseKey>>

    @Query("SELECT * FROM license_keys WHERE status = 'ACTIVE' AND isActivated = 1 AND (expiryTimestamp = -1 OR expiryTimestamp > :currentTime) ORDER BY createdTimestamp DESC")
    fun getActiveNonExpiredKeys(currentTime: Long): Flow<List<LicenseKey>>

    @Query("SELECT * FROM license_keys WHERE expiryTimestamp != -1 AND expiryTimestamp <= :currentTime ORDER BY expiryTimestamp DESC")
    fun getExpiredLicenseKeys(currentTime: Long): Flow<List<LicenseKey>>

    // Status & Expiry Modifications
    @Query("UPDATE license_keys SET status = :status, isActivated = :isActivated WHERE id = :id")
    suspend fun updateActivationStatus(id: Long, isActivated: Boolean, status: String)

    @Query("UPDATE license_keys SET status = :status, isActivated = CASE WHEN :status = 'ACTIVE' THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun updateKeyStatus(id: Long, status: String)

    @Query("UPDATE license_keys SET expiryTimestamp = :expiryTimestamp, status = 'ACTIVE', isActivated = 1 WHERE id = :id")
    suspend fun updateExpiryDate(id: Long, expiryTimestamp: Long)

    @Query("UPDATE license_keys SET isUsed = :isUsed, boundAccountNumber = :account WHERE id = :id")
    suspend fun bindAccount(id: Long, isUsed: Boolean, account: String)

    // Aggregations
    @Query("SELECT COUNT(*) FROM license_keys")
    suspend fun getLicenseCount(): Int

    @Query("SELECT COUNT(*) FROM license_keys WHERE status = 'ACTIVE' AND isActivated = 1")
    suspend fun getActiveLicenseCount(): Int
}
