package cn.fnrice.gpsinfo.ui.screen

import android.content.Context
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.viewmodel.GnssCapabilitiesInfo
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.RadioButton
import cn.fnrice.gpsinfo.data.MapProvider

@Composable
fun ProfileScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val context = LocalContext.current
    val capabilities = remember { viewModel.getGnssCapabilities(context) }
    val currentMapProvider by viewModel.mapProvider.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Profile",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 12.dp),
        )

        MapSettingsCard(
            currentProvider = currentMapProvider,
            onProviderSelected = { viewModel.setMapProvider(it) }
        )

        DeviceInfoCard(context)

        if (capabilities != null) {
            GnssCapabilitiesCard(capabilities)
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "GNSS capabilities require Android 12+",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MapSettingsCard(
    currentProvider: MapProvider,
    onProviderSelected: (MapProvider) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Map Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            MapProvider.values().forEach { provider ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = currentProvider == provider,
                        onClick = { onProviderSelected(provider) }
                    )
                    Text(provider.displayName, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(context: Context) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Device Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            InfoRow("Manufacturer", Build.MANUFACTURER)
            InfoRow("Model", Build.MODEL)
            InfoRow("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        }
    }
}

@Composable
private fun GnssCapabilitiesCard(caps: GnssCapabilitiesInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("GNSS Capabilities", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            InfoRow("Navigation Messages", caps.hasNavigationMessages.yesNo())
            InfoRow("Measurements", caps.hasMeasurements.yesNo())
            InfoRow("Antenna Info", caps.hasAntennaInfo.yesNo())
            InfoRow("Measurement Corrections", caps.hasMeasurementCorrections.yesNo())
            InfoRow("Correlation Vectors", caps.hasMeasurementCorrelationVectors.yesNo())
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun Boolean.yesNo() = if (this) "Yes" else "No"
