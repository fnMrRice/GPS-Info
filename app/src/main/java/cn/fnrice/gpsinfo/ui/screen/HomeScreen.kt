package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.GnssState
import cn.fnrice.gpsinfo.data.SatelliteInfo
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel
import kotlin.math.cos
import kotlin.math.sin

private fun getConstellationColor(name: String): Color {
    // Because names now include sources and are localized, we check for containment or use a prefix
    return when {
        name.contains("GPS") -> Color(0xFF4CAF50)
        name.contains("GLONASS") -> Color(0xFFFF9800)
        name.contains("Galileo") || name.contains("伽利略") -> Color(0xFF2196F3)
        name.contains("BDS") || name.contains("北斗") -> Color(0xFFF44336)
        name.contains("QZSS") -> Color(0xFF9C27B0)
        name.contains("SBAS") -> Color(0xFF795548)
        name.contains("NavIC") || name.contains("IRNSS") -> Color(0xFF00BCD4)
        else -> Color.Gray
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    val actualMapProvider by viewModel.actualMapProvider.collectAsState()

    var filterConstellation by remember { mutableStateOf<String?>(null) }
    var filterUsableOnly by remember { mutableStateOf(false) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var skyViewExpanded by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val filteredSatellites = remember(state.satellites, filterConstellation, filterUsableOnly, context) {
        val list = state.satellites.filter {
            (filterConstellation == null || it.getConstellationName(context) == filterConstellation) &&
                    (!filterUsableOnly || it.cn0DbHz > 0 || (it.hasBasebandCn0DbHz && it.basebandCn0DbHz > 0))
        }
        
        // 排序逻辑
        val country = java.util.Locale.getDefault().country
        val isChinaRegion = country == "CN" || country == "HK" || country == "MO"
        
        list.sortedWith(compareBy<SatelliteInfo> {
            val priority = when (it.constellationType) {
                android.location.GnssStatus.CONSTELLATION_BEIDOU -> if (isChinaRegion) 0 else 2
                android.location.GnssStatus.CONSTELLATION_GPS -> 1
                android.location.GnssStatus.CONSTELLATION_GALILEO -> if (isChinaRegion) 2 else 0
                else -> 3
            }
            priority
        }.thenBy { it.svid })
    }
    
    val constellations = remember(state.satellites, context) {
        val country = java.util.Locale.getDefault().country
        val isChinaRegion = country == "CN" || country == "HK" || country == "MO"
        
        state.satellites
            .map { it.constellationType }
            .distinct()
            .sortedWith(compareBy { type ->
                when (type) {
                    android.location.GnssStatus.CONSTELLATION_BEIDOU -> if (isChinaRegion) 0 else 2
                    android.location.GnssStatus.CONSTELLATION_GPS -> 1
                    android.location.GnssStatus.CONSTELLATION_GALILEO -> if (isChinaRegion) 2 else 0
                    else -> 3
                }
            })
            .map { type ->
                // Create a dummy SatelliteInfo to get the localized name
                val dummy = SatelliteInfo(svid = 0, constellationType = type, cn0DbHz = 0f, elevationDegrees = 0f, azimuthDegrees = 0f, usedInFix = false)
                dummy.getConstellationName(context)
            }
    }

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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.sky_view_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        IconButton(onClick = { skyViewExpanded = !skyViewExpanded }) {
                            Icon(
                                imageVector = if (skyViewExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }
                    if (skyViewExpanded) {
                        SkyView(
                            satellites = filteredSatellites,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        if (constellations.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        FilterChip(
                            selected = filterConstellation == null,
                            onClick = { filterConstellation = null },
                            label = { Text(stringResource(R.string.constellation_all)) },
                        )
                        constellations.forEach { name ->
                            FilterChip(
                                selected = filterConstellation == name,
                                onClick = { filterConstellation = name },
                                label = { Text(name) },
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { filterMenuExpanded = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = filterMenuExpanded,
                            onDismissRequest = { filterMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        androidx.compose.material3.Checkbox(
                                            checked = filterUsableOnly,
                                            onCheckedChange = null
                                        )
                                        Text(stringResource(R.string.filter_usable))
                                    }
                                },
                                onClick = {
                                    filterUsableOnly = !filterUsableOnly
                                    filterMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        items(filteredSatellites, key = { "${it.constellationType}-${it.svid}" }) { sat ->
            SatelliteCard(sat)
        }

        if (filteredSatellites.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.no_satellites_detected),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
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
            Text(stringResource(R.string.status_satellites), style = MaterialTheme.typography.headlineSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.status_used_visible, state.satellitesUsedInFix, state.satellitesTotal),
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
                    stringResource(R.string.status_gps_disabled),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ShimmerPlaceholder(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight? = FontWeight.Medium
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Text(
        text = text,
        modifier = modifier.graphicsLayer(alpha = alpha),
        style = style,
        fontWeight = fontWeight
    )
}

@Composable
private fun LocationCard(state: GnssState) {
    val loc = state.location
    val placeholder = stringResource(R.string.placeholder_no_data)
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.card_location), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (loc == null) {
                    Text(
                        stringResource(R.string.waiting_for_location),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.label_lat), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (loc != null) {
                    Text("%.6f".format(loc.latitude), fontWeight = FontWeight.Medium)
                } else {
                    ShimmerPlaceholder(placeholder)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.label_lon), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (loc != null) {
                    Text("%.6f".format(loc.longitude), fontWeight = FontWeight.Medium)
                } else {
                    ShimmerPlaceholder(placeholder)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.label_alt), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (loc != null) {
                    Text("%.1f m".format(loc.altitude), fontWeight = FontWeight.Medium)
                } else {
                    ShimmerPlaceholder(placeholder)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.label_accuracy), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (loc != null) {
                    Text("%.1f m".format(loc.accuracy), fontWeight = FontWeight.Medium)
                } else {
                    ShimmerPlaceholder(placeholder)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.label_speed), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (loc != null) {
                    Text("%.1f m/s".format(loc.speed), fontWeight = FontWeight.Medium)
                } else {
                    ShimmerPlaceholder(placeholder)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.label_bearing), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (loc != null) {
                    Text("%.1f°".format(loc.bearing), fontWeight = FontWeight.Medium)
                } else {
                    ShimmerPlaceholder(placeholder)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.label_source), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (loc != null) {
                    Text(loc.provider, fontWeight = FontWeight.Medium)
                } else {
                    ShimmerPlaceholder(placeholder)
                }
            }
        }
    }
}

@Composable
private fun SkyView(satellites: List<SatelliteInfo>, modifier: Modifier = Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSkyPlot(satellites, size.minDimension, context)
        }
    }
}

private fun DrawScope.drawSkyPlot(satellites: List<SatelliteInfo>, diameter: Float, context: android.content.Context) {
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

        val satColor = getConstellationColor(sat.getConstellationName(context))
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val constellationName = remember(sat.constellationType, context) { sat.getConstellationName(context) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val color = getConstellationColor(constellationName)
                Canvas(modifier = Modifier.size(12.dp)) {
                    drawCircle(color = color)
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "$constellationName ${sat.svid}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        if (sat.hasCarrierFrequency) {
                            val band = when {
                                sat.carrierFrequencyHz > 1.5e9 -> "L1"
                                sat.carrierFrequencyHz > 1.1e9 -> "L5"
                                else -> "L"
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                            ) {
                                Text(
                                    band,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                    if (sat.usedInFix) {
                        Text(stringResource(R.string.sat_used_in_fix), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                val signal = if (sat.cn0DbHz > 0) sat.cn0DbHz else if (sat.hasBasebandCn0DbHz) sat.basebandCn0DbHz else 0f
                Text(
                    if (signal > 0) "%.1f dB-Hz".format(signal) else "---",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        signal > 30 -> Color(0xFF4CAF50)
                        signal > 20 -> Color(0xFFFF9800)
                        signal > 0 -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
