package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "chart_analyses")
data class ChartAnalysisRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val symbol: String,
    val tradingMode: String,
    val strategy: String,
    val signalType: String, // "BUY", "SELL", "STRONG BUY"
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val riskReward: String,
    val confidence: Int,
    val timeframe: String = "H1",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface AnalysisDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(record: ChartAnalysisRecord): Long

    @Query("SELECT * FROM chart_analyses ORDER BY timestamp DESC")
    fun getAllAnalyses(): Flow<List<ChartAnalysisRecord>>

    @Delete
    suspend fun deleteAnalysis(record: ChartAnalysisRecord)

    @Query("DELETE FROM chart_analyses WHERE id = :id")
    suspend fun deleteAnalysisById(id: Long)

    @Query("SELECT COUNT(*) FROM chart_analyses")
    suspend fun getAnalysisCount(): Int
}
