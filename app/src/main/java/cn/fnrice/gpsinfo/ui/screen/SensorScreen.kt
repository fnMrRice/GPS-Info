package cn.fnrice.gpsinfo.ui.screen

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

@Composable
fun SensorScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    val sensorValues = state.sensorValues

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // 方向
        SensorSection(title = stringResource(R.string.sensor_section_orientation)) {
            SensorItem(stringResource(R.string.sensor_azimuth, state.azimuth))
            sensorValues[Sensor.TYPE_ROTATION_VECTOR]?.let { values ->
                orientationText(values)?.let { SensorItem(it) }
            }
            sensorValues[Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR]?.let { values ->
                orientationText(values)?.let { SensorItem(it) }
            }
        }

        // 运动与加速度
        SensorSection(title = stringResource(R.string.sensor_section_motion)) {
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

        // 磁场
        SensorSection(title = stringResource(R.string.sensor_section_magnetic)) {
            sensorValues[Sensor.TYPE_MAGNETIC_FIELD]?.let { values ->
                SensorItem(stringResource(R.string.sensor_magnetic_field, values[0], values[1], values[2]))
            }
        }

        // 光线
        SensorSection(title = stringResource(R.string.sensor_section_light)) {
            sensorValues[Sensor.TYPE_LIGHT]?.let { values ->
                SensorItem(stringResource(R.string.sensor_light, values[0]))
            }
            sensorValues[Sensor.TYPE_PROXIMITY]?.let { values ->
                SensorItem(stringResource(R.string.sensor_proximity, values[0]))
            }
        }

        // 环境
        SensorSection(title = stringResource(R.string.sensor_section_environment)) {
            sensorValues[Sensor.TYPE_PRESSURE]?.let { values ->
                SensorItem(stringResource(R.string.sensor_pressure, values[0]))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SensorSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SensorItem(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
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
