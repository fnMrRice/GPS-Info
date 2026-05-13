package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.data.SatelliteSnapshot
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FavoritesScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val snapshots by viewModel.snapshots.collectAsState()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Favorites", style = MaterialTheme.typography.headlineSmall)
            FilledTonalButton(
                onClick = { viewModel.saveSnapshot() },
                enabled = state.satellites.isNotEmpty(),
            ) {
                Text("Save Current")
            }
        }

        if (snapshots.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "No saved snapshots",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Tap \"Save Current\" to capture satellite data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(snapshots, key = { it.id }) { snapshot ->
                    SnapshotCard(
                        snapshot = snapshot,
                        onDelete = { viewModel.deleteSnapshot(snapshot.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SnapshotCard(snapshot: SatelliteSnapshot, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        snapshot.label.ifEmpty {
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .format(Date(snapshot.timestamp))
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "${snapshot.satellites.count { it.usedInFix }} used / ${snapshot.satellites.size} visible",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FilledTonalButton(onClick = onDelete) {
                    Text("Delete")
                }
            }

            snapshot.location?.let { loc ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    "%.6f, %.6f  Alt: %.1fm  Acc: %.1fm".format(
                        loc.latitude, loc.longitude, loc.altitude, loc.accuracy
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Constellation summary
            val byConstellation = snapshot.satellites.groupBy { it.constellationName }
            if (byConstellation.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    byConstellation.forEach { (name, sats) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "${sats.count { it.usedInFix }}/${sats.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
