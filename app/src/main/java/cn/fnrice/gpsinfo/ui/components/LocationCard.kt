package cn.fnrice.gpsinfo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.GnssState

@Composable
fun LocationCard(state: GnssState) {
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
