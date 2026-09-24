package com.example.data.repository

import com.example.data.local.AnalysisDao
import com.example.data.local.ChartAnalysisRecord
import kotlinx.coroutines.flow.Flow

class AnalysisRepository(private val analysisDao: AnalysisDao) {

    val allAnalyses: Flow<List<ChartAnalysisRecord>> = analysisDao.getAllAnalyses()

    suspend fun saveAnalysis(record: ChartAnalysisRecord): Long =
        analysisDao.insertAnalysis(record)

    suspend fun deleteAnalysis(record: ChartAnalysisRecord) =
        analysisDao.deleteAnalysis(record)

    suspend fun deleteAnalysisById(id: Long) =
        analysisDao.deleteAnalysisById(id)

    suspend fun seedInitialAnalysesIfNeeded() {
        if (analysisDao.getAnalysisCount() == 0) {
            val now = System.currentTimeMillis()
            val hourMs = 60 * 60 * 1000L
            val samples = listOf(
                ChartAnalysisRecord(
                    symbol = "EURUSD",
                    tradingMode = "Day Trading",
                    strategy = "Smart Money Concepts (SMC)",
                    signalType = "BUY",
                    entryPrice = 1.08450,
                    stopLoss = 1.08180,
                    takeProfit1 = 1.09100,
                    takeProfit2 = 1.09650,
                    riskReward = "1:2.8",
                    confidence = 94,
                    timeframe = "H1",
                    notes = "Asian range liquidity sweep confirmed. Strong bullish displacement into 1H Bullish Order Block with Fair Value Gap fill.",
                    timestamp = now - 2 * hourMs
                ),
                ChartAnalysisRecord(
                    symbol = "XAUUSD",
                    tradingMode = "Scalping",
                    strategy = "Liquidity Sweep & FVG",
                    signalType = "SELL",
                    entryPrice = 2735.40,
                    stopLoss = 2742.10,
                    takeProfit1 = 2721.00,
                    takeProfit2 = 2712.50,
                    riskReward = "1:3.2",
                    confidence = 91,
                    timeframe = "M15",
                    notes = "London session high swept. Market structure shift (MSS) to the downside with premium rejection.",
                    timestamp = now - 6 * hourMs
                ),
                ChartAnalysisRecord(
                    symbol = "BTCUSD",
                    tradingMode = "Swing Trading",
                    strategy = "Trend Pullback",
                    signalType = "STRONG BUY",
                    entryPrice = 86200.0,
                    stopLoss = 84100.0,
                    takeProfit1 = 91500.0,
                    takeProfit2 = 96000.0,
                    riskReward = "1:3.5",
                    confidence = 96,
                    timeframe = "H4",
                    notes = "Weekly order block bounce with 50-EMA support confluence on 4H. Bullish engulfing continuation.",
                    timestamp = now - 24 * hourMs
                )
            )
            samples.forEach { analysisDao.insertAnalysis(it) }
        }
    }
}
