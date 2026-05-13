package cn.fnrice.gpsinfo.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.SatelliteInfo

@Composable
fun SatelliteCard(sat: SatelliteInfo) {
    val context = LocalContext.current
    val constellationName = remember(sat.constellationType, context) { sat.getConstellationName(context) }
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val color = getConstellationColor(constellationName)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(color.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = constellationName.take(1),
                            color = color,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "$constellationName ${sat.svid}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            if (sat.hasCarrierFrequency) {
                                val band = when {
                                    sat.carrierFrequencyHz > 1.5e9 -> "L1"
                                    sat.carrierFrequencyHz > 1.1e9 -> "L5"
                                    else -> "L"
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(2.dp)
                                ) {
                                    Text(
                                        band,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (sat.usedInFix) {
                                Text(
                                    stringResource(R.string.sat_used_in_fix),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    stringResource(R.string.sat_not_used),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                )
                            }
                            Text(
                                "${stringResource(R.string.label_elev)}: %.0f°".format(sat.elevationDegrees),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 4.dp)) {
                    val signal = if (sat.cn0DbHz > 0) sat.cn0DbHz else if (sat.hasBasebandCn0DbHz) sat.basebandCn0DbHz else 0f
                    val signalColor = when {
                        signal > 35 -> Color(0xFF4CAF50)
                        signal > 25 -> Color(0xFF8BC34A)
                        signal > 15 -> Color(0xFFFF9800)
                        signal > 0 -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    }
                    
                    Text(
                        if (signal > 0) "%.1f".format(signal) else "---",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = signalColor,
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    // 简易信号强度条
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.5.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(1.25.dp))
                    ) {
                        val progress = (signal / 45f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(signalColor, RoundedCornerShape(1.25.dp))
                        )
                    }
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SatelliteDataField(label = stringResource(R.string.label_elev), value = "%.1f°".format(sat.elevationDegrees), modifier = Modifier.weight(1f))
                        SatelliteDataField(label = stringResource(R.string.label_azim), value = "%.1f°".format(sat.azimuthDegrees), modifier = Modifier.weight(1f))
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SatelliteDataField(label = stringResource(R.string.label_cn0), value = if (sat.cn0DbHz > 0) "%.1f dB-Hz".format(sat.cn0DbHz) else "---", modifier = Modifier.weight(1f))
                        SatelliteDataField(label = stringResource(R.string.label_baseband_cn0), value = if (sat.hasBasebandCn0DbHz) "%.1f dB-Hz".format(sat.basebandCn0DbHz) else "---", modifier = Modifier.weight(1f))
                    }

                    if (sat.hasCarrierFrequency) {
                        SatelliteDataField(label = stringResource(R.string.label_freq), value = "%.3f MHz".format(sat.carrierFrequencyHz / 1e6))
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        SatelliteDataField(
                            label = stringResource(R.string.label_almanac),
                            value = stringResource(if (sat.hasAlmanacData) R.string.yes else R.string.no),
                            valueColor = if (sat.hasAlmanacData) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        SatelliteDataField(
                            label = stringResource(R.string.label_ephemeris),
                            value = stringResource(if (sat.hasEphemerisData) R.string.yes else R.string.no),
                            valueColor = if (sat.hasEphemerisData) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SatelliteDataField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier.padding(vertical = 2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = valueColor)
    }
}
