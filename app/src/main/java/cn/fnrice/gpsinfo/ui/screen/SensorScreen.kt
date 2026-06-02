package cn.fnrice.gpsinfo.ui.screen

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.ui.components.AppCard
import cn.fnrice.gpsinfo.ui.components.ToastUtils
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

@Composable
fun SensorScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    val sensorValues = state.sensorValues
    val context = LocalContext.current
    val sensorCaps = remember { viewModel.getSensorCapabilities(context) }

    var supportedExpanded by remember { mutableStateOf(true) }
    var orientationExpanded by remember { mutableStateOf(false) }
    var motionExpanded by remember { mutableStateOf(false) }
    var environmentExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(orientationExpanded, motionExpanded, environmentExpanded) {
        viewModel.setSensorUiActive(orientationExpanded || motionExpanded || environmentExpanded)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.setSensorUiActive(false) }
    }

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
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val sensorTypeMap = mapOf(
                    R.string.label_accelerometer to Sensor.TYPE_ACCELEROMETER,
                    R.string.label_gyroscope to Sensor.TYPE_GYROSCOPE,
                    R.string.label_magnetometer to Sensor.TYPE_MAGNETIC_FIELD,
                    R.string.label_pressure to Sensor.TYPE_PRESSURE,
                    R.string.label_proximity to Sensor.TYPE_PROXIMITY,
                    R.string.label_light to Sensor.TYPE_LIGHT,
                    R.string.label_rotation_vector to Sensor.TYPE_ROTATION_VECTOR,
                    R.string.label_gravity to Sensor.TYPE_GRAVITY,
                    R.string.label_linear_acceleration to Sensor.TYPE_LINEAR_ACCELERATION,
                    R.string.label_game_rotation_vector to Sensor.TYPE_GAME_ROTATION_VECTOR,
                    R.string.label_geo_rotation_vector to Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
                    R.string.label_step_counter to Sensor.TYPE_STEP_COUNTER,
                    R.string.label_step_detector to Sensor.TYPE_STEP_DETECTOR,
                    R.string.label_ambient_temperature to Sensor.TYPE_AMBIENT_TEMPERATURE,
                    R.string.label_relative_humidity to Sensor.TYPE_RELATIVE_HUMIDITY,
                )
                val supportedMap = mapOf(
                    R.string.label_accelerometer to sensorCaps.hasAccelerometer,
                    R.string.label_gyroscope to sensorCaps.hasGyroscope,
                    R.string.label_magnetometer to sensorCaps.hasMagnetometer,
                    R.string.label_pressure to sensorCaps.hasPressure,
                    R.string.label_proximity to sensorCaps.hasProximity,
                    R.string.label_light to sensorCaps.hasLight,
                    R.string.label_rotation_vector to sensorCaps.hasRotationVector,
                    R.string.label_gravity to sensorCaps.hasGravity,
                    R.string.label_linear_acceleration to sensorCaps.hasLinearAcceleration,
                    R.string.label_game_rotation_vector to sensorCaps.hasGameRotationVector,
                    R.string.label_geo_rotation_vector to sensorCaps.hasGeoRotationVector,
                    R.string.label_step_counter to sensorCaps.hasStepCounter,
                    R.string.label_step_detector to sensorCaps.hasStepDetector,
                    R.string.label_ambient_temperature to sensorCaps.hasAmbientTemperature,
                    R.string.label_relative_humidity to sensorCaps.hasRelativeHumidity,
                )
                sensorTypeMap.forEach { (resId, sensorType) ->
                    val label = stringResource(resId)
                    val supported = supportedMap[resId] ?: false
                    SupportedChip(label, supported) {
                        val values = sensorValues[sensorType]
                        if (values != null) {
                            val valueStr = values.joinToString(", ") { "%.2f".format(it) }
                            ToastUtils.showToast(context, "$label: $valueStr")
                        } else {
                            ToastUtils.showToast(context, "$label: --")
                        }
                    }
                }
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
private fun SupportedChip(label: String, supported: Boolean, onClick: () -> Unit = {}) {
    val bgColor = if (supported) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val textColor = if (supported) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = textColor,
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
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
