package cn.fnrice.gpsinfo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.sp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.GnssState

@Composable
fun LocationCard(state: GnssState) {
    val loc = state.location
    val placeholder = stringResource(R.string.placeholder_no_data)
    
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.card_location),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (loc == null) {
                    Text(
                        stringResource(R.string.waiting_for_location),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            val labelStyle = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
            val valueStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )

            val locationItems = listOf(
                stringResource(R.string.label_lat) to if (loc != null) "%.6f".format(loc.latitude) else null,
                stringResource(R.string.label_lon) to if (loc != null) "%.6f".format(loc.longitude) else null,
                stringResource(R.string.label_alt) to if (loc != null) "%.1f m".format(loc.altitude) else null,
                stringResource(R.string.label_accuracy) to if (loc != null) "%.1f m".format(loc.accuracy) else null,
                stringResource(R.string.label_speed) to if (loc != null) "%.1f m/s".format(loc.speed) else null,
                stringResource(R.string.label_bearing) to if (loc != null) "%.1f°".format(loc.bearing) else null,
                stringResource(R.string.label_source) to loc?.provider
            )

            locationItems.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { (label, value) ->
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, style = labelStyle)
                            if (value != null) {
                                Text(value, style = valueStyle)
                            } else {
                                ShimmerPlaceholder(
                                    placeholder,
                                    modifier = Modifier.width(40.dp),
                                    style = valueStyle
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (rowItems !== locationItems.chunked(2).last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
