package com.example.data.repository

import com.example.data.local.LicenseDao
import com.example.data.local.LicenseKey
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import java.util.UUID

class LicenseRepository(private val licenseDao: LicenseDao) {

    val allLicenses: Flow<List<LicenseKey>> = licenseDao.getAllLicenseKeys()

    fun getLicensesByStatus(status: String): Flow<List<LicenseKey>> =
        licenseDao.getLicenseKeysByStatus(status)

    fun getActiveNonExpired(currentTime: Long): Flow<List<LicenseKey>> =
        licenseDao.getActiveNonExpiredKeys(currentTime)

    fun getExpiredLicenses(currentTime: Long): Flow<List<LicenseKey>> =
        licenseDao.getExpiredLicenseKeys(currentTime)

    suspend fun getLicenseById(id: Long): LicenseKey? =
        licenseDao.getLicenseKeyById(id)

    suspend fun getLicenseByCode(keyCode: String): LicenseKey? =
        licenseDao.getLicenseKeyByCode(keyCode.trim())

    suspend fun insertLicense(key: LicenseKey): Long =
        licenseDao.insertLicenseKey(key)

    suspend fun updateLicense(key: LicenseKey) =
        licenseDao.updateLicenseKey(key)

    suspend fun deleteLicense(key: LicenseKey) =
        licenseDao.deleteLicenseKey(key)

    suspend fun deleteLicenseById(id: Long) =
        licenseDao.deleteLicenseKeyById(id)

    suspend fun updateActivationStatus(id: Long, isActivated: Boolean, status: String) =
        licenseDao.updateActivationStatus(id, isActivated, status)

    suspend fun updateKeyStatus(id: Long, status: String) =
        licenseDao.updateKeyStatus(id, status)

    suspend fun updateExpiryDate(id: Long, expiryTimestamp: Long) =
        licenseDao.updateExpiryDate(id, expiryTimestamp)

    suspend fun bindAccount(id: Long, isUsed: Boolean, account: String) =
        licenseDao.bindAccount(id, isUsed, account)

    suspend fun getLicenseCount(): Int =
        licenseDao.getLicenseCount()

    suspend fun getActiveLicenseCount(): Int =
        licenseDao.getActiveLicenseCount()

    /**
     * Helper to seed initial sample license keys if the database is empty.
     */
    suspend fun seedInitialDataIfNeeded() {
        if (licenseDao.getLicenseCount() == 0) {
            val now = System.currentTimeMillis()
            val dayMs = 24L * 60 * 60 * 1000

            val initialKeys = listOf(
                LicenseKey(
                    keyCode = "136-75F-CBC-490",
                    userName = "Trader Alex (You)",
                    eaName = "EA Vortex v4.2 Pro",
                    eaId = 101L,
                    planName = "1 Year Pro",
                    durationDays = 365,
                    createdTimestamp = now - (30L * dayMs),
                    expiryTimestamp = now + (335L * dayMs),
                    status = "ACTIVE",
                    isActivated = true,
                    isUsed = true,
                    boundAccountNumber = "20938411",
                    notes = "Official EA Vortex license issued by Precision Algo. Bound to MetaTrader 4 live broker account."
                ),
                LicenseKey(
                    keyCode = "892-41A-E89-204",
                    userName = "Kabelo Mokoena",
                    eaName = "EA Vortex v4.2 Pro",
                    eaId = 101L,
                    planName = "Lifetime VIP",
                    durationDays = -1,
                    createdTimestamp = now - (90L * dayMs),
                    expiryTimestamp = -1L,
                    status = "ACTIVE",
                    isActivated = true,
                    isUsed = true,
                    boundAccountNumber = "88419203",
                    notes = "Lifetime unlimited VIP access for MT5 Prop Firm accounts."
                ),
                LicenseKey(
                    keyCode = "451-99C-DA1-718",
                    userName = "Sarah Jenkins",
                    eaName = "EA Vortex Scalper",
                    eaId = 102L,
                    planName = "6 Months",
                    durationDays = 180,
                    createdTimestamp = now - (15L * dayMs),
                    expiryTimestamp = now + (165L * dayMs),
                    status = "ACTIVE",
                    isActivated = true,
                    isUsed = false,
                    boundAccountNumber = null,
                    notes = "Available for activation on client MetaTrader platform."
                ),
                LicenseKey(
                    keyCode = "703-12B-AA4-559",
                    userName = "Marcus Vance",
                    eaName = "EA Vortex Swing Pro",
                    eaId = 103L,
                    planName = "1 Month Trial",
                    durationDays = 30,
                    createdTimestamp = now - (45L * dayMs),
                    expiryTimestamp = now - (15L * dayMs),
                    status = "EXPIRED",
                    isActivated = false,
                    isUsed = true,
                    boundAccountNumber = "10492817",
                    notes = "Expired on trial completion. Renewal pending."
                ),
                LicenseKey(
                    keyCode = "990-88F-REV-012",
                    userName = "Dev Test Bot",
                    eaName = "EA Vortex v4.2 Pro",
                    eaId = 101L,
                    planName = "6 Months",
                    durationDays = 180,
                    createdTimestamp = now - (60L * dayMs),
                    expiryTimestamp = now + (120L * dayMs),
                    status = "REVOKED",
                    isActivated = false,
                    isUsed = true,
                    boundAccountNumber = "99402134",
                    notes = "Revoked due to duplicate account sharing breach."
                )
            )
            licenseDao.insertLicenseKeys(initialKeys)
        }
    }

    /**
     * Generate formatted license key like "136-75F-CBC-490"
     */
    fun generateRandomKeyCode(): String {
        val chars = "0123456789ABCDEF"
        fun randomGroup(len: Int) = (1..len).map { chars.random() }.joinToString("")
        return "${randomGroup(3)}-${randomGroup(3)}-${randomGroup(3)}-${randomGroup(3)}"
    }
}
