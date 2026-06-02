package cn.fnrice.gpsinfo.ui.screen

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.ui.components.AppCard
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel
import cn.fnrice.gpsinfo.viewmodel.SensorCapabilitiesInfo

@Composable
fun SensorScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    val sensorValues = state.sensorValues
    val context = LocalContext.current
    val sensorCaps = remember { viewModel.getSensorCapabilities(context) }

    var supportedExpanded by remember { mutableStateOf(true) }
    var orientationExpanded by remember { mutableStateOf(true) }
    var motionExpanded by remember { mutableStateOf(true) }
    var environmentExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 支持的传感器
        AppCard(
            title = stringResource(R.string.supported_sensors_title),
            isExpandable = true,
            isExpanded = supportedExpanded,
            onExpandChange = { supportedExpanded = it },
            icon = Icons.Default.CheckCircle
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SupportedRow(stringResource(R.string.label_accelerometer), sensorCaps.hasAccelerometer)
                SupportedRow(stringResource(R.string.label_gyroscope), sensorCaps.hasGyroscope)
                SupportedRow(stringResource(R.string.label_magnetometer), sensorCaps.hasMagnetometer)
                SupportedRow(stringResource(R.string.label_pressure), sensorCaps.hasPressure)
                SupportedRow(stringResource(R.string.label_proximity), sensorCaps.hasProximity)
                SupportedRow(stringResource(R.string.label_light), sensorCaps.hasLight)
                SupportedRow(stringResource(R.string.label_rotation_vector), sensorCaps.hasRotationVector)
                SupportedRow(stringResource(R.string.label_gravity), sensorCaps.hasGravity)
                SupportedRow(stringResource(R.string.label_linear_acceleration), sensorCaps.hasLinearAcceleration)
                SupportedRow(stringResource(R.string.label_game_rotation_vector), sensorCaps.hasGameRotationVector)
                SupportedRow(stringResource(R.string.label_geo_rotation_vector), sensorCaps.hasGeoRotationVector)
                SupportedRow(stringResource(R.string.label_step_counter), sensorCaps.hasStepCounter)
                SupportedRow(stringResource(R.string.label_step_detector), sensorCaps.hasStepDetector)
                SupportedRow(stringResource(R.string.label_ambient_temperature), sensorCaps.hasAmbientTemperature)
                SupportedRow(stringResource(R.string.label_relative_humidity), sensorCaps.hasRelativeHumidity)
            }
        }

        // 方向
        AppCard(
            title = stringResource(R.string.sensor_section_orientation),
            isExpandable = true,
            isExpanded = orientationExpanded,
            onExpandChange = { orientationExpanded = it },
            icon = Icons.Default.CompassCalibration
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SensorItem(stringResource(R.string.sensor_azimuth, state.azimuth))
                sensorValues[Sensor.TYPE_ROTATION_VECTOR]?.let { values ->
                    orientationText(values)?.let { SensorItem(it) }
                }
                sensorValues[Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR]?.let { values ->
                    orientationText(values)?.let { SensorItem(it) }
                }
            }
        }

        // 运动与加速度
        AppCard(
            title = stringResource(R.string.sensor_section_motion),
            isExpandable = true,
            isExpanded = motionExpanded,
            onExpandChange = { motionExpanded = it },
            icon = Icons.Default.Speed
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sensorValues[Sensor.TYPE_ACCELEROMETER]?.let { values ->
                    SensorItem(stringResource(R.string.sensor_accelerometer, values[0], values[1], values[2]))
                }
                sensorValues[Sensor.TYPE_GRAVITY]?.let { values ->
                    SensorItem(stringResource(R.string.sensor_gravity, values[0], values[1], values[2]))
                }
                sensorValues[Sensor.TYPE_LINEAR_ACCELERATION]?.let { values ->
                    SensorItem(stringResource(R.string.sensor_linear_acceleration, values[0], values[1], values[2]))
                }
                sensorValues[Sensor.TYPE_GYROSCOPE]?.let { values ->
                    SensorItem(stringResource(R.string.sensor_gyroscope, values[0], values[1], values[2]))
                }
            }
        }

        // 环境
        AppCard(
            title = stringResource(R.string.sensor_section_environment),
            isExpandable = true,
            isExpanded = environmentExpanded,
            onExpandChange = { environmentExpanded = it },
            icon = Icons.Default.Eco
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sensorValues[Sensor.TYPE_MAGNETIC_FIELD]?.let { values ->
                    SensorItem(stringResource(R.string.sensor_magnetic_field, values[0], values[1], values[2]))
                }
                sensorValues[Sensor.TYPE_LIGHT]?.let { values ->
                    SensorItem(stringResource(R.string.sensor_light, values[0]))
                }
                sensorValues[Sensor.TYPE_PROXIMITY]?.let { values ->
                    SensorItem(stringResource(R.string.sensor_proximity, values[0]))
                }
                sensorValues[Sensor.TYPE_PRESSURE]?.let { values ->
                    SensorItem(stringResource(R.string.sensor_pressure, values[0]))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun SupportedRow(label: String, supported: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (supported) Icons.Default.CheckCircle else Icons.Default.HighlightOff,
            contentDescription = null,
            tint = if (supported) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun SensorItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun orientationText(values: FloatArray): String? {
    val rotationMatrix = FloatArray(9)
    SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
    val orientation = FloatArray(3)
    SensorManager.getOrientation(rotationMatrix, orientation)
    return "Orientation: Azim=${Math.toDegrees(orientation[0].toDouble()).toFloat()}°, " +
            "Pitch=${Math.toDegrees(orientation[1].toDouble()).toFloat()}°, " +
            "Roll=${Math.toDegrees(orientation[2].toDouble()).toFloat()}°"
}
