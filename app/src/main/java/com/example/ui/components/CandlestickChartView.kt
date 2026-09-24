package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TradeGreen
import com.example.ui.theme.TradeRed
import com.example.ui.theme.VortexPrimary
import com.example.ui.theme.VortexSecondary
import com.example.ui.theme.VortexSurfaceCard
import java.util.Locale

data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
) {
    val isBullish: Boolean get() = close >= open
}

@Composable
fun CandlestickChartView(
    candles: List<Candle>,
    symbol: String,
    timeframe: String,
    signalEntry: Double?,
    signalSL: Double?,
    signalTP1: Double?,
    signalTP2: Double?,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    val minPrice = remember(candles) { candles.minOf { it.low } }
    val maxPrice = remember(candles) { candles.maxOf { it.high } }
    val priceRange = (maxPrice - minPrice).coerceAtLeast(0.0001)

    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(VortexSurfaceCard)
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Chart HUD Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = symbol,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " • $timeframe",
                        color = VortexPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                val activeCandle = selectedCandleIndex?.let { candles.getOrNull(it) } ?: candles.last()
                val priceColor = if (activeCandle.isBullish) TradeGreen else TradeRed

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "O:${String.format(Locale.US, "%.4f", activeCandle.open)}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "H:${String.format(Locale.US, "%.4f", activeCandle.high)}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "C:${String.format(Locale.US, "%.4f", activeCandle.close)}",
                        fontSize = 10.sp,
                        color = priceColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Canvas Drawing
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp, bottom = 4.dp)
                    .pointerInput(candles) {
                        detectTapGestures { offset ->
                            val candleWidth = size.width / candles.size
                            val index = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                            selectedCandleIndex = index
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val candleCount = candles.size
                val candleWidth = width / candleCount
                val bodyWidth = (candleWidth * 0.7f).coerceAtLeast(3f)

                fun priceToY(price: Double): Float {
                    val normalized = (price - minPrice) / priceRange
                    return (height - (normalized * height)).toFloat()
                }

                // Draw Grid Lines
                val gridSteps = 4
                for (i in 0..gridSteps) {
                    val y = (height / gridSteps) * i
                    drawLine(
                        color = Color(0x1AFFFFFF),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                // Draw Order Block / Institutional Area if present
                if (signalEntry != null && signalSL != null) {
                    val topY = priceToY(maxOf(signalEntry, signalSL))
                    val bottomY = priceToY(minOf(signalEntry, signalSL))
                    drawRect(
                        color = Color(0x2E9333EA),
                        topLeft = Offset(width * 0.35f, topY),
                        size = Size(width * 0.65f, (bottomY - topY).coerceAtLeast(4f))
                    )
                }

                // Draw Moving Average Line (EMA 20 simulated)
                val emaPath = Path()
                var emaInitialized = false
                var currentEma = candles.first().close
                val alpha = 2.0 / (14 + 1)

                candles.forEachIndexed { index, c ->
                    currentEma = (c.close * alpha) + (currentEma * (1 - alpha))
                    val x = (index * candleWidth) + (candleWidth / 2)
                    val y = priceToY(currentEma)
                    if (!emaInitialized) {
                        emaPath.moveTo(x, y)
                        emaInitialized = true
                    } else {
                        emaPath.lineTo(x, y)
                    }
                }

                drawPath(
                    path = emaPath,
                    color = Color(0x9906B6D4),
                    style = Stroke(width = 2.5f)
                )

                // Draw Candlesticks
                candles.forEachIndexed { index, candle ->
                    val centerX = (index * candleWidth) + (candleWidth / 2)
                    val candleColor = if (candle.isBullish) TradeGreen else TradeRed

                    // Wick
                    val highY = priceToY(candle.high)
                    val lowY = priceToY(candle.low)
                    drawLine(
                        color = candleColor,
                        start = Offset(centerX, highY),
                        end = Offset(centerX, lowY),
                        strokeWidth = 2f
                    )

                    // Body
                    val openY = priceToY(candle.open)
                    val closeY = priceToY(candle.close)
                    val topBodyY = minOf(openY, closeY)
                    val bodyHeight = (Math.abs(openY - closeY)).coerceAtLeast(3f)

                    drawRect(
                        color = candleColor,
                        topLeft = Offset(centerX - (bodyWidth / 2), topBodyY),
                        size = Size(bodyWidth, bodyHeight)
                    )
                }

                // Draw Entry / Stop Loss / Take Profit Overlay Levels
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                signalEntry?.let { entry ->
                    val y = priceToY(entry)
                    drawLine(
                        color = VortexSecondary,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )
                }

                signalSL?.let { sl ->
                    val y = priceToY(sl)
                    drawLine(
                        color = TradeRed,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )
                }

                signalTP1?.let { tp1 ->
                    val y = priceToY(tp1)
                    drawLine(
                        color = TradeGreen,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 2f,
                        pathEffect = dashEffect
                    )
                }
            }
        }
    }
}
