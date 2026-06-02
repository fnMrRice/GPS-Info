package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

@Composable
fun SensorScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.sensor_azimuth, state.azimuth),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        state.sensorValues.forEach { (type, values) ->
            val text = when (type) {
                android.hardware.Sensor.TYPE_ACCELEROMETER ->
                    stringResource(R.string.sensor_accelerometer, values[0], values[1], values[2])
                android.hardware.Sensor.TYPE_GYROSCOPE ->
                    stringResource(R.string.sensor_gyroscope, values[0], values[1], values[2])
                android.hardware.Sensor.TYPE_MAGNETIC_FIELD ->
                    stringResource(R.string.sensor_magnetic_field, values[0], values[1], values[2])
                android.hardware.Sensor.TYPE_PRESSURE ->
                    stringResource(R.string.sensor_pressure, values[0])
                android.hardware.Sensor.TYPE_LIGHT ->
                    stringResource(R.string.sensor_light, values[0])
                android.hardware.Sensor.TYPE_PROXIMITY ->
                    stringResource(R.string.sensor_proximity, values[0])
                android.hardware.Sensor.TYPE_GRAVITY ->
                    stringResource(R.string.sensor_gravity, values[0], values[1], values[2])
                android.hardware.Sensor.TYPE_LINEAR_ACCELERATION ->
                    stringResource(R.string.sensor_linear_acceleration, values[0], values[1], values[2])
                android.hardware.Sensor.TYPE_ROTATION_VECTOR, android.hardware.Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                    val rotationMatrix = FloatArray(9)
                    android.hardware.SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
                    val orientation = FloatArray(3)
                    android.hardware.SensorManager.getOrientation(rotationMatrix, orientation)
                    stringResource(
                        R.string.sensor_orientation,
                        Math.toDegrees(orientation[0].toDouble()).toFloat(),
                        Math.toDegrees(orientation[1].toDouble()).toFloat(),
                        Math.toDegrees(orientation[2].toDouble()).toFloat()
                    )
                }
                else -> null
            }
            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}
