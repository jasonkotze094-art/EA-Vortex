package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CandlestickChartView
import com.example.ui.theme.TradeGreen
import com.example.ui.theme.TradeRed
import com.example.ui.theme.VortexAccent
import com.example.ui.theme.VortexBg
import com.example.ui.theme.VortexBorder
import com.example.ui.theme.VortexPrimary
import com.example.ui.theme.VortexPrimaryVariant
import com.example.ui.theme.VortexSecondary
import com.example.ui.theme.VortexSurfaceCard
import com.example.viewmodel.AnalysisViewModel
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnalysisTabScreen(
    viewModel: AnalysisViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var customUploadedImageUri by remember { mutableStateOf<String?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customUploadedImageUri = uri.toString()
            Toast.makeText(context, "Chart image attached for AI parsing", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VortexBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Header
        Text(
            text = "New Analysis",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = "Configure your setup, upload charts, and let EA Vortex find the signal.",
            color = Color(0xFF94A3B8),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // 1. SYMBOL
        Text(
            text = "1. SYMBOL",
            color = Color(0xFFE2E8F0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = uiState.selectedSymbol,
            onValueChange = { viewModel.setSymbol(it.uppercase()) },
            placeholder = { Text("E.G. EURUSD, BTCUSD, XAUUSD", color = Color(0xFF64748B)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("symbol_input"),
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

        // Quick Symbol Chips
        val popularSymbols = listOf("EURUSD", "BTCUSD", "XAUUSD", "NAS100", "GBPJPY", "US30")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            popularSymbols.forEach { sym ->
                val isSelected = uiState.selectedSymbol == sym
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSymbol(sym) },
                    label = { Text(sym, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VortexPrimaryVariant,
                        selectedLabelColor = Color.White,
                        containerColor = VortexSurfaceCard,
                        labelColor = Color(0xFFCBD5E1)
                    ),
                    border = BorderStroke(1.dp, if (isSelected) VortexPrimary else VortexBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("chip_symbol_$sym")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. TRADING MODE
        Text(
            text = "2. TRADING MODE",
            color = Color(0xFFE2E8F0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val modes = listOf("Scalping", "Day Trading", "Swing Trading")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEach { mode ->
                val isSelected = uiState.selectedTradingMode == mode
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setTradingMode(mode) }
                        .testTag("mode_${mode.lowercase().replace(" ", "_")}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF241544) else VortexSurfaceCard
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (isSelected) VortexPrimary else VortexBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = mode,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Text(
                            text = when (mode) {
                                "Scalping" -> "M1-M5"
                                "Day Trading" -> "M15-H1"
                                else -> "H4-D1"
                            },
                            color = if (isSelected) VortexAccent else Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. STRATEGY
        Text(
            text = "3. STRATEGY SELECTION",
            color = Color(0xFFE2E8F0),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val strategies = listOf(
            "Smart Money Concepts (SMC)",
            "Trend Pullback",
            "Liquidity Sweep & FVG",
            "Break & Retest Reversal"
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            strategies.forEach { strat ->
                val isSelected = uiState.selectedStrategy == strat
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setStrategy(strat) }
                        .testTag("strategy_${strat.take(8).lowercase()}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF221340) else VortexSurfaceCard
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) VortexPrimary else VortexBorder
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strat,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = VortexAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. CHART PREVIEW & TIMEFRAME
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "4. CHART READ & TIMEFRAME",
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            // Timeframe toggles
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("M5", "M15", "H1", "H4", "D1").forEach { tf ->
                    val isTfSelected = uiState.selectedTimeframe == tf
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isTfSelected) VortexPrimary else VortexSurfaceCard)
                            .clickable { viewModel.setTimeframe(tf) }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tf,
                            color = if (isTfSelected) Color.White else Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Candlestick Chart View
        CandlestickChartView(
            candles = uiState.candles,
            symbol = uiState.selectedSymbol,
            timeframe = uiState.selectedTimeframe,
            signalEntry = uiState.currentSignal?.entryPrice,
            signalSL = uiState.currentSignal?.stopLoss,
            signalTP1 = uiState.currentSignal?.takeProfit1,
            signalTP2 = uiState.currentSignal?.takeProfit2,
            modifier = Modifier.testTag("candlestick_chart_view")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Optional Chart Image Upload Button
        OutlinedButton(
            onClick = {
                photoPickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, VortexBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC084FC)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_chart_button")
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (customUploadedImageUri != null) "Chart Screenshot Attached ✓" else "Upload Custom Screenshot (Optional)",
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Analysis Execution Button
        Button(
            onClick = { viewModel.runAIAnalysis() },
            enabled = !uiState.isAnalyzing,
            colors = ButtonDefaults.buttonColors(
                containerColor = VortexPrimary,
                disabledContainerColor = Color(0xFF3B2D62)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("run_analysis_button")
        ) {
            if (uiState.isAnalyzing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("AI Parsing Multi-TF Structure...", color = Color.White, fontSize = 14.sp)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Run AI Analysis",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (uiState.isAnalyzing) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { uiState.analysisProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = VortexAccent,
                trackColor = VortexSurfaceCard
            )
        }

        // Signal Output Card
        AnimatedVisibility(
            visible = uiState.currentSignal != null,
            enter = fadeIn() + slideInVertically()
        ) {
            uiState.currentSignal?.let { signal ->
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signal_result_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1433)),
                        border = BorderStroke(
                            1.5.dp,
                            if (signal.signalType.contains("BUY")) TradeGreen else TradeRed
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Signal Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val isBuy = signal.signalType.contains("BUY")
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isBuy) Color(0x3310B981) else Color(0x33EF4444))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = signal.signalType,
                                            color = if (isBuy) TradeGreen else TradeRed,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${signal.symbol} • ${uiState.selectedTimeframe}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x339333EA))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${signal.confidence}% Confluence",
                                        color = Color(0xFFE9D5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Parameters Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ParameterBox(
                                    label = "ENTRY",
                                    value = String.format(Locale.US, "%.5f", signal.entryPrice),
                                    color = VortexSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                ParameterBox(
                                    label = "STOP LOSS",
                                    value = String.format(Locale.US, "%.5f", signal.stopLoss),
                                    color = TradeRed,
                                    modifier = Modifier.weight(1f)
                                )
                                ParameterBox(
                                    label = "TAKE PROFIT 1",
                                    value = String.format(Locale.US, "%.5f", signal.takeProfit1),
                                    color = TradeGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ParameterBox(
                                    label = "TAKE PROFIT 2",
                                    value = String.format(Locale.US, "%.5f", signal.takeProfit2),
                                    color = TradeGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                ParameterBox(
                                    label = "TAKE PROFIT 3",
                                    value = String.format(Locale.US, "%.5f", signal.takeProfit3),
                                    color = TradeGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                ParameterBox(
                                    label = "RISK : REWARD",
                                    value = signal.riskReward,
                                    color = VortexAccent,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Confluences
                            Text(
                                text = "Institutional Confluences:",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            signal.confluences.forEach { item ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = TradeGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val tradeText = """
                                            EA VORTEX SIGNAL
                                            Pair: ${signal.symbol}
                                            Type: ${signal.signalType}
                                            Entry: ${signal.entryPrice}
                                            SL: ${signal.stopLoss}
                                            TP1: ${signal.takeProfit1}
                                            TP2: ${signal.takeProfit2}
                                            RR: ${signal.riskReward}
                                        """.trimIndent()
                                        clipboard.setPrimaryClip(ClipData.newPlainText("EA Vortex Signal", tradeText))
                                        Toast.makeText(context, "Parameters copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VortexPrimaryVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("copy_parameters_button")
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Signal", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Signal sent to bound MetaTrader EA!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("send_to_ea_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Send to EA", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun ParameterBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF130D26))
            .border(BorderStroke(1.dp, Color(0x33475569)), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = Color(0xFF64748B),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
