package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.LicenseKey
import com.example.data.repository.LicenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LicenseUiState(
    val filter: String = "ALL", // "ALL", "ACTIVE", "EXPIRED", "REVOKED", "BOUND"
    val searchQuery: String = "",
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val selectedLicenseForBinding: LicenseKey? = null,
    val showIssueKeyDialog: Boolean = false,
    val showBindAccountDialog: Boolean = false,
    val notificationMessage: String? = null
)

class LicenseViewModel(
    private val repository: LicenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LicenseUiState())
    val uiState: StateFlow<LicenseUiState> = _uiState.asStateFlow()

    val allLicenses: StateFlow<List<LicenseKey>> = repository.allLicenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredLicenses: StateFlow<List<LicenseKey>> = combine(
        allLicenses,
        _uiState
    ) { licenses, state ->
        licenses.filter { key ->
            val matchesFilter = when (state.filter) {
                "ACTIVE" -> key.status == "ACTIVE" && key.isActivated
                "EXPIRED" -> key.status == "EXPIRED" || (key.expiryTimestamp != -1L && key.expiryTimestamp <= System.currentTimeMillis())
                "REVOKED" -> key.status == "REVOKED"
                "BOUND" -> key.isUsed && !key.boundAccountNumber.isNullOrBlank()
                else -> true
            }

            val query = state.searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                    key.keyCode.lowercase().contains(query) ||
                    key.userName.lowercase().contains(query) ||
                    key.eaName.lowercase().contains(query) ||
                    (key.boundAccountNumber?.lowercase()?.contains(query) == true)

            matchesFilter && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            refreshCounts()
        }
    }

    private suspend fun refreshCounts() {
        val total = repository.getLicenseCount()
        val active = repository.getActiveLicenseCount()
        _uiState.update { it.copy(totalCount = total, activeCount = active) }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openIssueKeyDialog(show: Boolean) {
        _uiState.update { it.copy(showIssueKeyDialog = show) }
    }

    fun openBindAccountDialog(license: LicenseKey?) {
        _uiState.update {
            it.copy(
                showBindAccountDialog = license != null,
                selectedLicenseForBinding = license
            )
        }
    }

    fun clearNotification() {
        _uiState.update { it.copy(notificationMessage = null) }
    }

    fun issueNewLicenseKey(
        userName: String,
        eaName: String,
        planName: String,
        durationDays: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val expiry = if (durationDays == -1) -1L else now + (durationDays.toLong() * 24 * 60 * 60 * 1000)
            val newCode = repository.generateRandomKeyCode()

            val newLicense = LicenseKey(
                keyCode = newCode,
                userName = userName.ifBlank { "Client Trader" },
                eaName = eaName,
                eaId = when {
                    eaName.contains("Vortex") -> 101L
                    eaName.contains("Scalper") -> 102L
                    else -> 103L
                },
                planName = planName,
                durationDays = durationDays,
                createdTimestamp = now,
                expiryTimestamp = expiry,
                status = "ACTIVE",
                isActivated = true,
                isUsed = false,
                boundAccountNumber = null,
                notes = notes
            )
            repository.insertLicense(newLicense)
            refreshCounts()
            _uiState.update {
                it.copy(
                    showIssueKeyDialog = false,
                    notificationMessage = "License Key $newCode generated successfully!"
                )
            }
        }
    }

    fun bindAccount(licenseId: Long, accountNumber: String) {
        viewModelScope.launch {
            val trimmedAcc = accountNumber.trim()
            if (trimmedAcc.isNotBlank()) {
                repository.bindAccount(licenseId, isUsed = true, account = trimmedAcc)
                refreshCounts()
                _uiState.update {
                    it.copy(
                        showBindAccountDialog = false,
                        selectedLicenseForBinding = null,
                        notificationMessage = "MetaTrader Account #$trimmedAcc bound to license!"
                    )
                }
            }
        }
    }

    fun toggleActivation(license: LicenseKey) {
        viewModelScope.launch {
            val newActive = !license.isActivated
            val newStatus = if (newActive) "ACTIVE" else "REVOKED"
            repository.updateActivationStatus(license.id, newActive, newStatus)
            refreshCounts()
            _uiState.update {
                it.copy(notificationMessage = "License status changed to $newStatus")
            }
        }
    }

    fun extendExpiry(license: LicenseKey, daysToAdd: Int) {
        viewModelScope.launch {
            val baseTime = if (license.expiryTimestamp > System.currentTimeMillis()) {
                license.expiryTimestamp
            } else {
                System.currentTimeMillis()
            }
            val newExpiry = baseTime + (daysToAdd.toLong() * 24 * 60 * 60 * 1000)
            repository.updateExpiryDate(license.id, newExpiry)
            refreshCounts()
            _uiState.update {
                it.copy(notificationMessage = "License extended by $daysToAdd days")
            }
        }
    }

    fun deleteLicense(license: LicenseKey) {
        viewModelScope.launch {
            repository.deleteLicense(license)
            refreshCounts()
            _uiState.update {
                it.copy(notificationMessage = "License ${license.keyCode} deleted")
            }
        }
    }

    fun activateByEnteredCode(keyCode: String, accountNum: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val key = repository.getLicenseByCode(keyCode)
            if (key == null) {
                onResult(false, "License key not found in EA Vortex database.")
                return@launch
            }
            if (key.status != "ACTIVE" || !key.isActivated) {
                onResult(false, "This license key is marked as ${key.status}.")
                return@launch
            }
            if (key.expiryTimestamp != -1L && key.expiryTimestamp <= System.currentTimeMillis()) {
                onResult(false, "This license key has expired.")
                return@launch
            }
            if (accountNum.isNotBlank()) {
                repository.bindAccount(key.id, isUsed = true, account = accountNum.trim())
            }
            refreshCounts()
            onResult(true, "License activated for ${key.eaName} (${key.planName})!")
        }
    }
}

class LicenseViewModelFactory(private val repository: LicenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LicenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LicenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
