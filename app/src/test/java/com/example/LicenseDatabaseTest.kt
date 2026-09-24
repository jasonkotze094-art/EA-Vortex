package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.LicenseDao
import com.example.data.local.LicenseKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LicenseDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var licenseDao: LicenseDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        licenseDao = db.licenseDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveLicenseKey() = runBlocking {
        val key = LicenseKey(
            keyCode = "136-75F-CBC-490",
            userName = "Trader Alex",
            eaName = "EA Vortex v4.2 Pro",
            eaId = 101L,
            planName = "1 Year Pro",
            durationDays = 365,
            expiryTimestamp = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000),
            status = "ACTIVE",
            isActivated = true,
            isUsed = false,
            notes = "Test license"
        )

        val id = licenseDao.insertLicenseKey(key)
        val retrieved = licenseDao.getLicenseKeyById(id)

        assertNotNull(retrieved)
        assertEquals("136-75F-CBC-490", retrieved?.keyCode)
        assertEquals("Trader Alex", retrieved?.userName)
        assertEquals("ACTIVE", retrieved?.status)
        assertTrue(retrieved?.isActivated == true)
        assertEquals("136-75F-CBC-490", retrieved?.uniqueKeyId)
    }

    @Test
    fun bindAccountUpdatesCorrectly() = runBlocking {
        val key = LicenseKey(
            keyCode = "892-41A-E89-204",
            userName = "Kabelo",
            eaName = "EA Vortex Scalper",
            eaId = 102L,
            planName = "6 months",
            durationDays = 180,
            expiryTimestamp = -1L,
            status = "ACTIVE"
        )
        val id = licenseDao.insertLicenseKey(key)

        licenseDao.bindAccount(id, true, "20938411")
        val updated = licenseDao.getLicenseKeyById(id)

        assertEquals("20938411", updated?.boundAccountNumber)
        assertTrue(updated?.isUsed == true)
    }

    @Test
    fun updateStatusAndRevocation() = runBlocking {
        val key = LicenseKey(
            keyCode = "703-12B-AA4-559",
            userName = "Marcus",
            eaName = "EA Vortex Pro",
            eaId = 101L,
            planName = "1 Month",
            durationDays = 30,
            expiryTimestamp = System.currentTimeMillis() + 100000,
            status = "ACTIVE",
            isActivated = true
        )
        val id = licenseDao.insertLicenseKey(key)

        licenseDao.updateActivationStatus(id, false, "REVOKED")
        val revoked = licenseDao.getLicenseKeyById(id)

        assertEquals("REVOKED", revoked?.status)
        assertFalse(revoked?.isActivated == true)
    }
}
