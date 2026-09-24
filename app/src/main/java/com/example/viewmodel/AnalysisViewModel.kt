package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChartAnalysisRecord
import com.example.data.repository.AnalysisRepository
import com.example.ui.components.Candle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

data class SignalResult(
    val symbol: String,
    val tradingMode: String,
    val strategy: String,
    val signalType: String, // "BUY", "SELL", "STRONG BUY"
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val takeProfit3: Double,
    val riskReward: String,
    val confidence: Int,
    val confluences: List<String>,
    val institutionalNotes: String
)

data class AnalysisUiState(
    val selectedSymbol: String = "EURUSD",
    val selectedTradingMode: String = "Day Trading", // "Scalping", "Day Trading", "Swing Trading"
    val selectedStrategy: String = "Smart Money Concepts (SMC)",
    val selectedTimeframe: String = "H1",
    val isAnalyzing: Boolean = false,
    val analysisProgress: Float = 0f,
    val creditsRemaining: Int = 15,
    val currentSignal: SignalResult? = null,
    val candles: List<Candle> = emptyList(),
    val toastMessage: String? = null
)

class AnalysisViewModel(
    private val repository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    val analysisHistory: StateFlow<List<ChartAnalysisRecord>> = repository.allAnalyses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.seedInitialAnalysesIfNeeded()
            generateCandlesForSetup(_uiState.value.selectedSymbol, _uiState.value.selectedTimeframe)
        }
    }

    fun setSymbol(symbol: String) {
        _uiState.update { it.copy(selectedSymbol = symbol, currentSignal = null) }
        generateCandlesForSetup(symbol, _uiState.value.selectedTimeframe)
    }

    fun setTradingMode(mode: String) {
        val tf = when (mode) {
            "Scalping" -> "M5"
            "Day Trading" -> "H1"
            "Swing Trading" -> "H4"
            else -> "H1"
        }
        _uiState.update { it.copy(selectedTradingMode = mode, selectedTimeframe = tf, currentSignal = null) }
        generateCandlesForSetup(_uiState.value.selectedSymbol, tf)
    }

    fun setStrategy(strategy: String) {
        _uiState.update { it.copy(selectedStrategy = strategy) }
    }

    fun setTimeframe(tf: String) {
        _uiState.update { it.copy(selectedTimeframe = tf) }
        generateCandlesForSetup(_uiState.value.selectedSymbol, tf)
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun generateCandlesForSetup(symbol: String, tf: String) {
        val basePrice = when (symbol) {
            "EURUSD" -> 1.08500
            "BTCUSD" -> 88200.00
            "XAUUSD" -> 2738.50
            "NAS100" -> 19850.00
            "GBPJPY" -> 194.20
            "US30" -> 42100.00
            else -> 1.1000
        }

        val step = when (symbol) {
            "EURUSD" -> 0.0008
            "BTCUSD" -> 450.0
            "XAUUSD" -> 4.5
            "NAS100" -> 35.0
            "GBPJPY" -> 0.35
            "US30" -> 55.0
            else -> 0.001
        }

        val list = mutableListOf<Candle>()
        var curr = basePrice - (step * 8)
        val rnd = Random(symbol.hashCode() + tf.hashCode())

        for (i in 0 until 24) {
            val delta = (rnd.nextDouble() - 0.45) * step
            val open = curr
            val close = curr + delta
            val high = maxOf(open, close) + (rnd.nextDouble() * step * 0.4)
            val low = minOf(open, close) - (rnd.nextDouble() * step * 0.4)
            val vol = 1000.0 + rnd.nextDouble() * 5000.0
            list.add(Candle(open, high, low, close, vol))
            curr = close
        }

        _uiState.update { it.copy(candles = list) }
    }

    fun runAIAnalysis() {
        val currentState = _uiState.value
        if (currentState.creditsRemaining <= 0) {
            _uiState.update { it.copy(toastMessage = "No analysis credits remaining. Upgrade or refresh key.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, analysisProgress = 0.1f) }

            delay(350)
            _uiState.update { it.copy(analysisProgress = 0.45f) }
            delay(400)
            _uiState.update { it.copy(analysisProgress = 0.85f) }
            delay(300)

            val lastCandle = currentState.candles.lastOrNull()
            val currentPrice = lastCandle?.close ?: when (currentState.selectedSymbol) {
                "EURUSD" -> 1.08620
                "BTCUSD" -> 88500.0
                "XAUUSD" -> 2742.0
                "NAS100" -> 19910.0
                "GBPJPY" -> 194.50
                else -> 42200.0
            }

            val isBullishStrategy = currentState.selectedStrategy.contains("SMC") ||
                    currentState.selectedStrategy.contains("Trend") ||
                    Random.nextBoolean()

            val signalType = if (isBullishStrategy) "BUY" else "SELL"
            val pipFactor = when (currentState.selectedSymbol) {
                "EURUSD" -> 0.0020
                "BTCUSD" -> 1400.0
                "XAUUSD" -> 12.0
                "NAS100" -> 120.0
                "GBPJPY" -> 1.2
                else -> 220.0
            }

            val entry = currentPrice
            val sl = if (signalType == "BUY") entry - pipFactor else entry + pipFactor
            val tp1 = if (signalType == "BUY") entry + (pipFactor * 1.5) else entry - (pipFactor * 1.5)
            val tp2 = if (signalType == "BUY") entry + (pipFactor * 2.8) else entry - (pipFactor * 2.8)
            val tp3 = if (signalType == "BUY") entry + (pipFactor * 4.2) else entry - (pipFactor * 4.2)

            val confidence = 90 + Random.nextInt(8)
            val rr = "1:2.8"

            val confluences = listOf(
                "Market Structure Shift (MSS) on ${_uiState.value.selectedTimeframe}",
                "Institutional Order Block mitigation verified",
                "Fair Value Gap (FVG) imbalance fill",
                "Relative Strength Index (RSI) momentum alignment",
                "Higher Timeframe trend confluence with EA Vortex Engine"
            )

            val notes = "EA Vortex multi-timeframe algorithm detected high-probability liquidity capture. " +
                    "Tight invalidation stop placed behind the structural swing. Optimal risk-to-reward setup."

            val signal = SignalResult(
                symbol = currentState.selectedSymbol,
                tradingMode = currentState.selectedTradingMode,
                strategy = currentState.selectedStrategy,
                signalType = signalType,
                entryPrice = entry,
                stopLoss = sl,
                takeProfit1 = tp1,
                takeProfit2 = tp2,
                takeProfit3 = tp3,
                riskReward = rr,
                confidence = confidence,
                confluences = confluences,
                institutionalNotes = notes
            )

            // Save in Room Database
            val record = ChartAnalysisRecord(
                symbol = currentState.selectedSymbol,
                tradingMode = currentState.selectedTradingMode,
                strategy = currentState.selectedStrategy,
                signalType = signalType,
                entryPrice = entry,
                stopLoss = sl,
                takeProfit1 = tp1,
                takeProfit2 = tp2,
                riskReward = rr,
                confidence = confidence,
                timeframe = currentState.selectedTimeframe,
                notes = notes
            )
            repository.saveAnalysis(record)

            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    analysisProgress = 1f,
                    currentSignal = signal,
                    creditsRemaining = (it.creditsRemaining - 1).coerceAtLeast(0),
                    toastMessage = "New Signal Generated: $signalType ${currentState.selectedSymbol} ($rr)"
                )
            }
        }
    }

    fun deleteAnalysisRecord(record: ChartAnalysisRecord) {
        viewModelScope.launch {
            repository.deleteAnalysis(record)
            _uiState.update { it.copy(toastMessage = "Analysis record deleted") }
        }
    }
}

class AnalysisViewModelFactory(private val repository: AnalysisRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalysisViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AnalysisViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
