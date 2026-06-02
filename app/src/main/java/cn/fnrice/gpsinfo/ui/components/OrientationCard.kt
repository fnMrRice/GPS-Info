package cn.fnrice.gpsinfo.ui.components

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R

@Composable
fun OrientationCard(
    azimuth: Float,
    sensorValues: Map<Int, FloatArray>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onHelpClick: () -> Unit
) {
    // 缓存旋转矩阵计算，仅在旋转矢量数据变化时重算
    val rvValues = sensorValues[Sensor.TYPE_ROTATION_VECTOR]
        ?: sensorValues[Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR]
    val orientation = remember(rvValues) {
        rvValues?.let { values ->
            val rotMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotMatrix, values)
            val orient = FloatArray(3)
            SensorManager.getOrientation(rotMatrix, orient)
            Triple(
                Math.toDegrees(orient[0].toDouble()).toFloat(),
                Math.toDegrees(orient[1].toDouble()).toFloat(),
                Math.toDegrees(orient[2].toDouble()).toFloat()
            )
        }
    }

    // 缓存静态字符串
    val nameAzimuth = stringResource(R.string.sensor_name_azimuth)
    val unitDeg = stringResource(R.string.unit_deg)
    val nameOrientation = stringResource(R.string.sensor_name_orientation)
    val axisAzim = stringResource(R.string.axis_azim)
    val axisPitch = stringResource(R.string.axis_pitch)
    val axisRoll = stringResource(R.string.axis_roll)

    AppCard(
        title = stringResource(R.string.sensor_section_orientation),
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = onExpandChange,
        icon = Icons.Default.CompassCalibration,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SensorSingleData(
                name = nameAzimuth,
                unit = unitDeg,
                value = azimuth,
                format = "%.1f"
            )
            orientation?.let { (azim, pitch, roll) ->
                SensorMultiData(
                    name = nameOrientation,
                    unit = unitDeg,
                    axes = listOf(axisAzim to azim, axisPitch to pitch, axisRoll to roll)
                )
            }
        }
    }
}
