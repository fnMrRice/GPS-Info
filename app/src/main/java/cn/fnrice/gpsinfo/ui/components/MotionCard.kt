package cn.fnrice.gpsinfo.ui.components

import android.hardware.Sensor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R

@Composable
fun MotionCard(
    sensorValues: Map<Int, FloatArray>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    // 缓存静态字符串
    val nameAccel = stringResource(R.string.sensor_name_accelerometer)
    val nameGravity = stringResource(R.string.sensor_name_gravity)
    val nameLinear = stringResource(R.string.sensor_name_linear_acceleration)
    val nameGyro = stringResource(R.string.sensor_name_gyroscope)
    val unitMps2 = stringResource(R.string.unit_mps2)
    val unitRadS = stringResource(R.string.unit_rad_s)
    val axisX = stringResource(R.string.axis_x)
    val axisY = stringResource(R.string.axis_y)
    val axisZ = stringResource(R.string.axis_z)
    val xyz = listOf(axisX, axisY, axisZ)

    AppCard(
        title = stringResource(R.string.sensor_section_motion),
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = onExpandChange,
        icon = Icons.Default.Speed
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            sensorValues[Sensor.TYPE_ACCELEROMETER]?.let { v ->
                SensorMultiData(name = nameAccel, unit = unitMps2, axes = xyz.zip(v.take(3)))
            }
            sensorValues[Sensor.TYPE_GRAVITY]?.let { v ->
                SensorMultiData(name = nameGravity, unit = unitMps2, axes = xyz.zip(v.take(3)))
            }
            sensorValues[Sensor.TYPE_LINEAR_ACCELERATION]?.let { v ->
                SensorMultiData(name = nameLinear, unit = unitMps2, axes = xyz.zip(v.take(3)))
            }
            sensorValues[Sensor.TYPE_GYROSCOPE]?.let { v ->
                SensorMultiData(name = nameGyro, unit = unitRadS, axes = xyz.zip(v.take(3)))
            }
        }
    }
}
