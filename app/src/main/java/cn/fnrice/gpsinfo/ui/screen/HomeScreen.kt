package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.fnrice.gpsinfo.data.GnssState
import cn.fnrice.gpsinfo.data.SatelliteInfo
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel
import kotlin.math.cos
import kotlin.math.sin

private val constellationColors = mapOf(
    "GPS" to Color(0xFF4CAF50),
    "GLONASS" to Color(0xFFFF9800),
    "Galileo" to Color(0xFF2196F3),
    "北斗" to Color(0xFFF44336),
    "QZSS" to Color(0xFF9C27B0),
    "SBAS" to Color(0xFF795548),
    "IRNSS" to Color(0xFF00BCD4),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    val actualMapProvider by viewModel.actualMapProvider.collectAsState()

    var filterConstellation by remember { mutableStateOf<String?>(null) }
    val filteredSatellites = state.satellites.filter {
        filterConstellation == null || it.constellationName == filterConstellation
    }
    val constellations = state.satellites.map { it.constellationName }.distinct().sorted()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        item {
            StatusHeader(state, actualMapProvider)
        }

        if (state.location != null) {
            item {
                LocationCard(state)
            }
        }

        item {
            SkyView(
                satellites = filteredSatellites,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }

        if (constellations.isNotEmpty()) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FilterChip(
                        selected = filterConstellation == null,
                        onClick = { filterConstellation = null },
                        label = { Text("All") },
                    )
                    constellations.forEach { name ->
                        FilterChip(
                            selected = filterConstellation == name,
                            onClick = { filterConstellation = name },
                            label = { Text(name) },
                        )
                    }
                }
            }
        }

        items(filteredSatellites, key = { "${it.constellationType}-${it.svid}" }) { sat ->
            SatelliteCard(sat)
        }
    }
}

@Composable
private fun StatusHeader(state: GnssState, mapProvider: cn.fnrice.gpsinfo.data.MapProvider) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Satellites", style = MaterialTheme.typography.headlineSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${state.satellitesUsedInFix} used / ${state.satellitesTotal} visible",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        mapProvider.displayName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        if (!state.isLocationEnabled) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(
                    "GPS Disabled",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun LocationCard(state: GnssState) {
    val loc = state.location ?: return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Lat", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.6f".format(loc.latitude), fontWeight = FontWeight.Medium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Lon", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.6f".format(loc.longitude), fontWeight = FontWeight.Medium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Alt", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.1f m".format(loc.altitude), fontWeight = FontWeight.Medium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Accuracy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.1f m".format(loc.accuracy), fontWeight = FontWeight.Medium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Speed", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.1f m/s".format(loc.speed), fontWeight = FontWeight.Medium)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bearing", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("%.1f°".format(loc.bearing), fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SkyView(satellites: List<SatelliteInfo>, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawSkyPlot(satellites, size.minDimension)
            }
        }
    }
}

private fun DrawScope.drawSkyPlot(satellites: List<SatelliteInfo>, diameter: Float) {
    val center = Offset(diameter / 2, diameter / 2)
    val radius = diameter / 2 - 24

    // Concentric circles (90°, 60°, 30°, 0°)
    for (elev in listOf(0f, 30f, 60f)) {
        val r = radius * (1 - elev / 90f)
        drawCircle(
            color = Color.Gray.copy(alpha = 0.3f),
            radius = r,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
            ),
        )
    }

    // Cross lines (N-S, E-W)
    drawLine(Color.Gray.copy(alpha = 0.3f), Offset(center.x - radius, center.y), Offset(center.x + radius, center.y))
    drawLine(Color.Gray.copy(alpha = 0.3f), Offset(center.x, center.y - radius), Offset(center.x, center.y + radius))

    // Labels
    drawContext.canvas.nativeCanvas.apply {
        val paint = android.graphics.Paint().apply {
            textSize = 20f
            color = android.graphics.Color.GRAY
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawText("N", center.x, center.y - radius - 4, paint)
        drawText("S", center.x, center.y + radius + 16, paint)
        drawText("E", center.x + radius + 12, center.y + 6, paint)
        drawText("W", center.x - radius - 12, center.y + 6, paint)
    }

    // Satellites
    satellites.forEach { sat ->
        val elevRad = Math.toRadians(sat.elevationDegrees.toDouble())
        val azimRad = Math.toRadians(sat.azimuthDegrees.toDouble())
        val r = radius * (1 - sat.elevationDegrees / 90f)
        val x = center.x + (r * sin(azimRad)).toFloat()
        val y = center.y - (r * cos(azimRad)).toFloat()

        val satColor = constellationColors[sat.constellationName] ?: Color.White
        val dotRadius = if (sat.usedInFix) 8f else 5f

        drawCircle(color = satColor, radius = dotRadius, center = Offset(x, y))
        if (sat.usedInFix) {
            drawCircle(color = satColor.copy(alpha = 0.3f), radius = dotRadius + 4f, center = Offset(x, y))
        }

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                textSize = 16f
                color = satColor.toArgb()
                textAlign = android.graphics.Paint.Align.CENTER
            }
            drawText("${sat.svid}", x, y - dotRadius - 2, paint)
        }
    }
}

@Composable
private fun SatelliteCard(sat: SatelliteInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val color = constellationColors[sat.constellationName] ?: Color.Gray
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(color = color)
                }
                Column {
                    Text(
                        "${sat.constellationName} ${sat.svid}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    if (sat.usedInFix) {
                        Text("Used in fix", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "%.1f dB-Hz".format(sat.cn0DbHz),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        sat.cn0DbHz > 30 -> Color(0xFF4CAF50)
                        sat.cn0DbHz > 20 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                )
                Text(
                    "El %.1f°  Az %.1f°".format(sat.elevationDegrees, sat.azimuthDegrees),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
