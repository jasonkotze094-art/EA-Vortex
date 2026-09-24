package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "license_keys",
    indices = [
        Index(value = ["keyCode"], unique = true),
        Index(value = ["status"]),
        Index(value = ["expiryTimestamp"])
    ]
)
data class LicenseKey(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyCode: String,                                     // Unique License Key ID (e.g. "136-75F-CBC-490")
    val userName: String,                                    // Authorized user / client name
    val eaName: String,                                      // EA trading robot name
    val eaId: Long,                                          // Linked Expert Advisor ID
    val planName: String,                                    // e.g. "6 months", "1 year", "Lifetime"
    val durationDays: Int,                                   // Duration in days (-1 for Lifetime)
    val createdTimestamp: Long = System.currentTimeMillis(), // Creation date timestamp
    val expiryTimestamp: Long,                               // Expiry date timestamp (-1 for Lifetime)
    val status: String = "ACTIVE",                           // Activation status: "ACTIVE", "EXPIRED", "REVOKED"
    val isActivated: Boolean = true,                         // Boolean activation status flag
    val isUsed: Boolean = false,                             // Activation state (Not Yet Used vs Bound to MT4)
    val boundAccountNumber: String? = null,                  // MetaTrader 4/5 account number
    val notes: String = ""                                   // Administrative notes
) {
    // Convenience property accessors
    val uniqueKeyId: String get() = keyCode
    val creationDate: Long get() = createdTimestamp
    val expiryDate: Long get() = expiryTimestamp
    val activationStatus: String get() = status
}
