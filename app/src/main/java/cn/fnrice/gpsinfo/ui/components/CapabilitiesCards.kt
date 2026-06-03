package cn.fnrice.gpsinfo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.viewmodel.GnssCapabilitiesInfo
import cn.fnrice.gpsinfo.viewmodel.SensorCapabilitiesInfo

@Composable
fun GnssCapabilitiesCard(caps: GnssCapabilitiesInfo?) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.gnss_capabilities),
        icon = Icons.Default.Memory,
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = { isExpanded = it }
    ) {
        if (caps != null) {
            InfoRow(stringResource(R.string.label_nav_messages), caps.hasNavigationMessages.yesNo())
            InfoRow(stringResource(R.string.label_measurements), caps.hasMeasurements.yesNo())
            InfoRow(stringResource(R.string.label_antenna_info), caps.hasAntennaInfo.yesNo())
            InfoRow(
                stringResource(R.string.label_meas_corrections),
                caps.hasMeasurementCorrections.yesNo()
            )
            InfoRow(
                stringResource(R.string.label_correlation_vectors),
                caps.hasMeasurementCorrelationVectors.yesNo()
            )
        } else {
            Text(
                stringResource(R.string.gnss_android_version_required),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SensorCapabilitiesCard(caps: SensorCapabilitiesInfo) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.label_sensor_features),
        icon = Icons.Default.Sensors,
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = { isExpanded = it }
    ) {
        InfoRow(stringResource(R.string.label_accelerometer), caps.hasAccelerometer.yesNo())
        InfoRow(stringResource(R.string.label_gyroscope), caps.hasGyroscope.yesNo())
        InfoRow(stringResource(R.string.label_magnetometer), caps.hasMagnetometer.yesNo())
        InfoRow(stringResource(R.string.label_pressure), caps.hasPressure.yesNo())
        InfoRow(stringResource(R.string.label_proximity), caps.hasProximity.yesNo())
        InfoRow(stringResource(R.string.label_light), caps.hasLight.yesNo())
        InfoRow(stringResource(R.string.label_rotation_vector), caps.hasRotationVector.yesNo())
        InfoRow(stringResource(R.string.label_gravity), caps.hasGravity.yesNo())
    }
}
