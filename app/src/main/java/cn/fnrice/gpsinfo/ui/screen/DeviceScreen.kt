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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.viewmodel.GnssCapabilitiesInfo
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@Composable
fun DeviceScreen(viewModel: GnssViewModel, innerPadding: PaddingValues, onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    val capabilities = remember { viewModel.getGnssCapabilities(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 12.dp),
        )

        DeviceInfoCard(context)

        if (capabilities != null) {
            GnssCapabilitiesCard(capabilities)
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.gnss_android_version_required),
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        SettingsEntryCard(onNavigateToSettings)
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun DeviceInfoCard(context: Context) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(stringResource(R.string.device_info), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            InfoRow(stringResource(R.string.label_manufacturer), Build.MANUFACTURER)
            InfoRow(stringResource(R.string.label_model), Build.MODEL)
            InfoRow(stringResource(R.string.label_android_version), "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        }
    }
}

@Composable
private fun GnssCapabilitiesCard(caps: GnssCapabilitiesInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(stringResource(R.string.gnss_capabilities), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            InfoRow(stringResource(R.string.label_nav_messages), caps.hasNavigationMessages.yesNo())
            InfoRow(stringResource(R.string.label_measurements), caps.hasMeasurements.yesNo())
            InfoRow(stringResource(R.string.label_antenna_info), caps.hasAntennaInfo.yesNo())
            InfoRow(stringResource(R.string.label_meas_corrections), caps.hasMeasurementCorrections.yesNo())
            InfoRow(stringResource(R.string.label_correlation_vectors), caps.hasMeasurementCorrelationVectors.yesNo())
        }
    }
}

@Composable
private fun SettingsEntryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Text(
                stringResource(R.string.settings_title),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
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

@Composable
private fun Boolean.yesNo() = if (this) stringResource(R.string.yes) else stringResource(R.string.no)
