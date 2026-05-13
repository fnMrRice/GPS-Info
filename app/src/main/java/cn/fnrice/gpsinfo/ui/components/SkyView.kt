package cn.fnrice.gpsinfo.ui.components

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import cn.fnrice.gpsinfo.data.SatelliteInfo
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SkyView(
    satellites: List<SatelliteInfo>,
    azimuth: Float,
    rotateWithCompass: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rotation = if (rotateWithCompass) -azimuth else 0f
            rotate(rotation) {
                drawSkyPlot(satellites, size.minDimension, context)
            }
        }
    }
}

private fun DrawScope.drawSkyPlot(satellites: List<SatelliteInfo>, diameter: Float, context: Context) {
    val center = Offset(diameter / 2, diameter / 2)
    val radius = diameter / 2 - 24

    // Concentric circles (90°, 60°, 30°, 0°)
    for (elev in listOf(0f, 30f, 60f)) {
        val r = radius * (1 - elev / 90f)
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = r,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            ),
        )
    }

    // Cross lines (N-S, E-W)
    drawLine(Color.White.copy(alpha = 0.5f), Offset(center.x - radius, center.y), Offset(center.x + radius, center.y), strokeWidth = 2f)
    drawLine(Color.White.copy(alpha = 0.5f), Offset(center.x, center.y - radius), Offset(center.x, center.y + radius), strokeWidth = 2f)

    // Labels
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            textSize = 28f
            color = android.graphics.Color.WHITE
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.CENTER
            setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
        }
        drawText("N", center.x, center.y - radius - 8, paint)
        drawText("S", center.x, center.y + radius + 24, paint)
        drawText("E", center.x + radius + 20, center.y + 10, paint)
        drawText("W", center.x - radius - 20, center.y + 10, paint)
    }

    // Satellites
    satellites.forEach { sat ->
        val elevRad = Math.toRadians(sat.elevationDegrees.toDouble())
        val azimRad = Math.toRadians(sat.azimuthDegrees.toDouble())
        val r = radius * (1 - sat.elevationDegrees / 90f)
        val x = center.x + (r * sin(azimRad)).toFloat()
        val y = center.y - (r * cos(azimRad)).toFloat()

        val satColor = getConstellationColor(sat.getConstellationName(context))
        val dotRadius = if (sat.usedInFix) 8f else 5f

        drawCircle(color = satColor, radius = dotRadius, center = Offset(x, y))
        if (sat.usedInFix) {
            drawCircle(color = satColor.copy(alpha = 0.3f), radius = dotRadius + 4f, center = Offset(x, y))
        }

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                textSize = 18f
                color = android.graphics.Color.WHITE
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                textAlign = android.graphics.Paint.Align.CENTER
                setShadowLayer(4f, 0f, 0f, android.graphics.Color.BLACK)
            }
            drawText("${sat.svid}", x, y - dotRadius - 4, paint)
        }
    }
}
