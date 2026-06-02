package cn.fnrice.gpsinfo.ui.components

import android.hardware.Sensor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.SensorCapabilitiesInfo

data class SensorChipInfo(val labelResId: Int, val sensorType: Int, val supported: Boolean)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SupportedSensorsCard(
    sensorCaps: SensorCapabilitiesInfo,
    sensorValues: Map<Int, FloatArray>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onHelpClick: () -> Unit
) {
    val context = LocalContext.current

    // 缓存静态 chip 列表，避免每次重组都创建
    val chipList = remember(sensorCaps) {
        listOf(
            SensorChipInfo(R.string.label_accelerometer, Sensor.TYPE_ACCELEROMETER, sensorCaps.hasAccelerometer),
            SensorChipInfo(R.string.label_gyroscope, Sensor.TYPE_GYROSCOPE, sensorCaps.hasGyroscope),
            SensorChipInfo(R.string.label_magnetometer, Sensor.TYPE_MAGNETIC_FIELD, sensorCaps.hasMagnetometer),
            SensorChipInfo(R.string.label_pressure, Sensor.TYPE_PRESSURE, sensorCaps.hasPressure),
            SensorChipInfo(R.string.label_proximity, Sensor.TYPE_PROXIMITY, sensorCaps.hasProximity),
            SensorChipInfo(R.string.label_light, Sensor.TYPE_LIGHT, sensorCaps.hasLight),
            SensorChipInfo(R.string.label_rotation_vector, Sensor.TYPE_ROTATION_VECTOR, sensorCaps.hasRotationVector),
            SensorChipInfo(R.string.label_gravity, Sensor.TYPE_GRAVITY, sensorCaps.hasGravity),
            SensorChipInfo(R.string.label_linear_acceleration, Sensor.TYPE_LINEAR_ACCELERATION, sensorCaps.hasLinearAcceleration),
            SensorChipInfo(R.string.label_game_rotation_vector, Sensor.TYPE_GAME_ROTATION_VECTOR, sensorCaps.hasGameRotationVector),
            SensorChipInfo(R.string.label_geo_rotation_vector, Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, sensorCaps.hasGeoRotationVector),
            SensorChipInfo(R.string.label_step_counter, Sensor.TYPE_STEP_COUNTER, sensorCaps.hasStepCounter),
            SensorChipInfo(R.string.label_step_detector, Sensor.TYPE_STEP_DETECTOR, sensorCaps.hasStepDetector),
            SensorChipInfo(R.string.label_ambient_temperature, Sensor.TYPE_AMBIENT_TEMPERATURE, sensorCaps.hasAmbientTemperature),
            SensorChipInfo(R.string.label_relative_humidity, Sensor.TYPE_RELATIVE_HUMIDITY, sensorCaps.hasRelativeHumidity),
        )
    }

    // 缓存 label 字符串
    val labels = remember {
        mutableMapOf<Int, String>()
    }

    AppCard(
        title = stringResource(R.string.supported_sensors_title),
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = onExpandChange,
        icon = Icons.Default.CheckCircle,
        headerExtra = {
            IconButton(onClick = onHelpClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = stringResource(R.string.sensor_help_icon_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chipList.forEach { chip ->
                val label = stringResource(chip.labelResId)
                SupportedChip(label, chip.supported) {
                    val values = sensorValues[chip.sensorType]
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
}
