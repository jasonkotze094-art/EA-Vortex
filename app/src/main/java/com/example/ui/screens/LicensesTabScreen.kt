package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LicenseKey
import com.example.ui.theme.TradeGreen
import com.example.ui.theme.TradeRed
import com.example.ui.theme.VortexAccent
import com.example.ui.theme.VortexBg
import com.example.ui.theme.VortexBorder
import com.example.ui.theme.VortexPrimary
import com.example.ui.theme.VortexPrimaryVariant
import com.example.ui.theme.VortexSecondary
import com.example.ui.theme.VortexSurfaceCard
import com.example.ui.theme.WarningGold
import com.example.viewmodel.LicenseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LicensesTabScreen(
    viewModel: LicenseViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val licenseList by viewModel.filteredLicenses.collectAsStateWithLifecycle()
    val allLicenses by viewModel.allLicenses.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.notificationMessage) {
        uiState.notificationMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearNotification()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VortexBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "License Manager",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "EA Vortex & MetaTrader License Key Hub",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.openIssueKeyDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = VortexPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("issue_new_key_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Issue Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Stats Cards Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val boundCount = allLicenses.count { it.isUsed && !it.boundAccountNumber.isNullOrBlank() }
                    StatCard(
                        title = "TOTAL KEYS",
                        count = uiState.totalCount.toString(),
                        subtitle = "Database Records",
                        color = VortexPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "ACTIVE",
                        count = uiState.activeCount.toString(),
                        subtitle = "Authorized",
                        color = TradeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "MT4 BOUND",
                        count = boundCount.toString(),
                        subtitle = "Connected",
                        color = VortexSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Prominent "My Active License" Card
            item {
                val primaryLicense = allLicenses.firstOrNull { it.status == "ACTIVE" && it.isActivated }
                    ?: allLicenses.firstOrNull()

                primaryLicense?.let { myKey ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("my_active_license_card"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF221640)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, VortexPrimary)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = VortexAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CURRENT ACTIVE LICENSE",
                                        color = Color(0xFFD8B4FE),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                StatusBadge(status = myKey.status, isActivated = myKey.isActivated)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = myKey.keyCode,
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("License Key", myKey.keyCode))
                                        Toast.makeText(context, "License Key copied!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp).testTag("copy_my_key_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Key",
                                        tint = VortexPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${myKey.eaName} • ${myKey.planName}",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (myKey.expiryTimestamp == -1L) "Lifetime Access" else "Expires ${formatDate(myKey.expiryTimestamp)}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = if (myKey.boundAccountNumber != null) TradeGreen else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (myKey.boundAccountNumber != null) "MT4: #${myKey.boundAccountNumber}" else "Not Bound to MetaTrader",
                                        color = if (myKey.boundAccountNumber != null) TradeGreen else Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.openBindAccountDialog(myKey) },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, VortexBorder),
                                    modifier = Modifier.height(34.dp).testTag("bind_my_account_button")
                                ) {
                                    Text("Bind MT4/5", fontSize = 11.sp, color = VortexSecondary)
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar & Filters
            item {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by code, user, EA, MT4...", color = Color(0xFF64748B)) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = VortexPrimary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_licenses_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VortexPrimary,
                        unfocusedBorderColor = VortexBorder,
                        focusedContainerColor = VortexSurfaceCard,
                        unfocusedContainerColor = VortexSurfaceCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                val filterOptions = listOf("ALL", "ACTIVE", "EXPIRED", "REVOKED", "BOUND")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filterOptions.forEach { filter ->
                        val isSelected = uiState.filter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VortexPrimaryVariant,
                                selectedLabelColor = Color.White,
                                containerColor = VortexSurfaceCard,
                                labelColor = Color(0xFF94A3B8)
                            ),
                            border = BorderStroke(1.dp, if (isSelected) VortexPrimary else VortexBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("filter_chip_$filter")
                        )
                    }
                }
            }

            // License Keys List
            if (licenseList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No licenses matching filter", color = Color(0xFF94A3B8), fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(licenseList, key = { it.id }) { license ->
                    LicenseItemCard(
                        license = license,
                        onBindClick = { viewModel.openBindAccountDialog(license) },
                        onToggleActive = { viewModel.toggleActivation(license) },
                        onExtendExpiry = { viewModel.extendExpiry(license, 30) },
                        onDelete = { viewModel.deleteLicense(license) },
                        onCopyCode = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("License Key", license.keyCode))
                            Toast.makeText(context, "Copied ${license.keyCode}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Issue New Key Dialog
        if (uiState.showIssueKeyDialog) {
            IssueKeyDialog(
                onDismiss = { viewModel.openIssueKeyDialog(false) },
                onConfirm = { user, ea, plan, duration, notes ->
                    viewModel.issueNewLicenseKey(user, ea, plan, duration, notes)
                }
            )
        }

        // Bind Account Dialog
        if (uiState.showBindAccountDialog && uiState.selectedLicenseForBinding != null) {
            val key = uiState.selectedLicenseForBinding!!
            BindAccountDialog(
                licenseKey = key,
                onDismiss = { viewModel.openBindAccountDialog(null) },
                onConfirm = { accountNum ->
                    viewModel.bindAccount(key.id, accountNum)
                }
            )
        }
    }
}

@Composable
fun LicenseItemCard(
    license: LicenseKey,
    onBindClick: () -> Unit,
    onToggleActive: () -> Unit,
    onExtendExpiry: () -> Unit,
    onDelete: () -> Unit,
    onCopyCode: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("license_card_${license.id}"),
        colors = CardDefaults.cardColors(containerColor = VortexSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, VortexBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Code + Status Badge + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCopyCode() }
                ) {
                    Text(
                        text = license.keyCode,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = VortexPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(status = license.status, isActivated = license.isActivated)

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (license.isActivated) "Revoke Key" else "Activate Key") },
                                onClick = {
                                    showMenu = false
                                    onToggleActive()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.PowerSettingsNew,
                                        contentDescription = null,
                                        tint = if (license.isActivated) TradeRed else TradeGreen
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bind MetaTrader Account") },
                                onClick = {
                                    showMenu = false
                                    onBindClick()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = VortexSecondary)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Extend Expiry (+30 Days)") },
                                onClick = {
                                    showMenu = false
                                    onExtendExpiry()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = WarningGold)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Key", color = TradeRed) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = TradeRed)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // User & EA
            Text(
                text = "${license.userName} • ${license.eaName}",
                color = Color(0xFFCBD5E1),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Plan: ${license.planName}",
                    color = VortexAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (license.expiryTimestamp == -1L) "Lifetime" else "Expires: ${formatDate(license.expiryTimestamp)}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bound Account Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF130E26))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (!license.boundAccountNumber.isNullOrBlank()) "MT4/5 Account: #${license.boundAccountNumber}" else "Not Bound (Open)",
                    color = if (!license.boundAccountNumber.isNullOrBlank()) TradeGreen else Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (license.isUsed) "Activated & Bound" else "Ready to Activate",
                    color = if (license.isUsed) Color(0xFF94A3B8) else VortexPrimary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String, isActivated: Boolean) {
    val (bgColor, textColor, text) = when {
        status == "REVOKED" || !isActivated -> Triple(Color(0x33EF4444), TradeRed, "REVOKED")
        status == "EXPIRED" -> Triple(Color(0x33F59E0B), WarningGold, "EXPIRED")
        else -> Triple(Color(0x3310B981), TradeGreen, "ACTIVE")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = VortexSurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, VortexBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color(0xFF64748B),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count,
                color = color,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun IssueKeyDialog(
    onDismiss: () -> Unit,
    onConfirm: (user: String, ea: String, plan: String, duration: Int, notes: String) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var selectedEa by remember { mutableStateOf("EA Vortex v4.2 Pro") }
    var selectedPlan by remember { mutableStateOf("1 Year Pro") }
    var durationDays by remember { mutableStateOf(365) }
    var notes by remember { mutableStateOf("") }

    val eaOptions = listOf("EA Vortex v4.2 Pro", "EA Vortex Scalper", "EA Vortex Swing Pro")
    val planOptions = listOf(
        "1 Month Trial" to 30,
        "6 Months" to 180,
        "1 Year Pro" to 365,
        "Lifetime VIP" to -1
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1D1438),
        title = {
            Text("Issue EA Vortex License", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client / User Name") },
                    placeholder = { Text("e.g. Kabelo M.") },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_client_name"),
                    singleLine = true
                )

                Text("Select Expert Advisor:", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    eaOptions.forEach { ea ->
                        val isSel = selectedEa == ea
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedEa = ea },
                            label = { Text(ea.replace("EA Vortex ", ""), fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VortexPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Text("Select Plan Duration:", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    planOptions.forEach { (plan, days) ->
                        val isSel = selectedPlan == plan
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedPlan = plan
                                durationDays = days
                            },
                            label = { Text(plan, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VortexAccent,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Administrative Notes") },
                    placeholder = { Text("e.g. Prop firm account license") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        clientName.ifBlank { "Client Trader" },
                        selectedEa,
                        selectedPlan,
                        durationDays,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = VortexPrimary),
                modifier = Modifier.testTag("confirm_issue_key_button")
            ) {
                Text("Generate Key")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun BindAccountDialog(
    licenseKey: LicenseKey,
    onDismiss: () -> Unit,
    onConfirm: (accountNumber: String) -> Unit
) {
    var accountNumber by remember { mutableStateOf(licenseKey.boundAccountNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1D1438),
        title = {
            Text("Bind MetaTrader Account", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Key: ${licenseKey.keyCode}",
                    color = VortexPrimary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Enter the client MetaTrader 4 or 5 account number to authorize EA trading execution:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    placeholder = { Text("e.g. 20938411") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("mt4_account_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (accountNumber.isNotBlank()) {
                        onConfirm(accountNumber.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VortexSecondary),
                modifier = Modifier.testTag("confirm_bind_account_button")
            ) {
                Text("Bind Account", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}

fun formatDate(timestamp: Long): String {
    if (timestamp == -1L) return "Lifetime"
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    return sdf.format(Date(timestamp))
}
