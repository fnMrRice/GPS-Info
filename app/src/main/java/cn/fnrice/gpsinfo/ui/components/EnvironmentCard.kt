package cn.fnrice.gpsinfo.ui.components

import android.hardware.Sensor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R

@Composable
fun EnvironmentCard(
    sensorValues: Map<Int, FloatArray>,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    // 缓存静态字符串
    val nameMag = stringResource(R.string.sensor_name_magnetic_field)
    val nameLight = stringResource(R.string.sensor_name_light)
    val nameProximity = stringResource(R.string.sensor_name_proximity)
    val namePressure = stringResource(R.string.sensor_name_pressure)
    val unitUt = stringResource(R.string.unit_ut)
    val unitLx = stringResource(R.string.unit_lx)
    val unitCm = stringResource(R.string.unit_cm)
    val unitHpa = stringResource(R.string.unit_hpa)
    val axisX = stringResource(R.string.axis_x)
    val axisY = stringResource(R.string.axis_y)
    val axisZ = stringResource(R.string.axis_z)
    val xyz = listOf(axisX, axisY, axisZ)

    AppCard(
        title = stringResource(R.string.sensor_section_environment),
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = onExpandChange,
        icon = Icons.Default.Eco
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            sensorValues[Sensor.TYPE_MAGNETIC_FIELD]?.let { v ->
                SensorMultiData(name = nameMag, unit = unitUt, axes = xyz.zip(v.take(3)))
            }
            SensorSingleData(
                name = nameLight,
                unit = unitLx,
                value = sensorValues[Sensor.TYPE_LIGHT]?.get(0),
                noDataText = "--",
                format = "%.1f"
            )
            SensorSingleData(
                name = nameProximity,
                unit = unitCm,
                value = sensorValues[Sensor.TYPE_PROXIMITY]?.get(0),
                noDataText = "--",
                format = "%.1f"
            )
            sensorValues[Sensor.TYPE_PRESSURE]?.let { v ->
                SensorSingleData(name = namePressure, unit = unitHpa, value = v[0])
            }
        }
    }
}
