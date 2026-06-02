package cn.fnrice.gpsinfo.ui.screen

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    var showHelpDialog by remember { mutableStateOf(false) }
    var showOrientationHelp by remember { mutableStateOf(false) }

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
            icon = Icons.Default.CheckCircle,
            headerExtra = {
                IconButton(onClick = { showHelpDialog = true }, modifier = Modifier.size(24.dp)) {
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
            icon = Icons.Default.CompassCalibration,
            headerExtra = {
                IconButton(onClick = { showOrientationHelp = true }, modifier = Modifier.size(24.dp)) {
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
                    name = stringResource(R.string.sensor_name_azimuth),
                    unit = stringResource(R.string.unit_deg),
                    value = state.azimuth,
                    format = "%.1f"
                )
                val rvValues = sensorValues[Sensor.TYPE_ROTATION_VECTOR]
                    ?: sensorValues[Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR]
                rvValues?.let { values ->
                    val rotMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotMatrix, values)
                    val orient = FloatArray(3)
                    SensorManager.getOrientation(rotMatrix, orient)
                    SensorMultiData(
                        name = stringResource(R.string.sensor_name_orientation),
                        unit = stringResource(R.string.unit_deg),
                        axes = listOf(
                            stringResource(R.string.axis_azim) to Math.toDegrees(orient[0].toDouble()).toFloat(),
                            stringResource(R.string.axis_pitch) to Math.toDegrees(orient[1].toDouble()).toFloat(),
                            stringResource(R.string.axis_roll) to Math.toDegrees(orient[2].toDouble()).toFloat()
                        )
                    )
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
                val xyz = listOf(
                    stringResource(R.string.axis_x),
                    stringResource(R.string.axis_y),
                    stringResource(R.string.axis_z)
                )
                sensorValues[Sensor.TYPE_ACCELEROMETER]?.let { values ->
                    SensorMultiData(
                        name = stringResource(R.string.sensor_name_accelerometer),
                        unit = stringResource(R.string.unit_mps2),
                        axes = xyz.zip(values.take(3).toList())
                    )
                }
                sensorValues[Sensor.TYPE_GRAVITY]?.let { values ->
                    SensorMultiData(
                        name = stringResource(R.string.sensor_name_gravity),
                        unit = stringResource(R.string.unit_mps2),
                        axes = xyz.zip(values.take(3).toList())
                    )
                }
                sensorValues[Sensor.TYPE_LINEAR_ACCELERATION]?.let { values ->
                    SensorMultiData(
                        name = stringResource(R.string.sensor_name_linear_acceleration),
                        unit = stringResource(R.string.unit_mps2),
                        axes = xyz.zip(values.take(3).toList())
                    )
                }
                sensorValues[Sensor.TYPE_GYROSCOPE]?.let { values ->
                    SensorMultiData(
                        name = stringResource(R.string.sensor_name_gyroscope),
                        unit = stringResource(R.string.unit_rad_s),
                        axes = xyz.zip(values.take(3).toList())
                    )
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
                val xyz = listOf(
                    stringResource(R.string.axis_x),
                    stringResource(R.string.axis_y),
                    stringResource(R.string.axis_z)
                )
                sensorValues[Sensor.TYPE_MAGNETIC_FIELD]?.let { values ->
                    SensorMultiData(
                        name = stringResource(R.string.sensor_name_magnetic_field),
                        unit = stringResource(R.string.unit_ut),
                        axes = xyz.zip(values.take(3).toList())
                    )
                }
                sensorValues[Sensor.TYPE_LIGHT]?.let { values ->
                    SensorSingleData(
                        name = stringResource(R.string.sensor_name_light),
                        unit = stringResource(R.string.unit_lx),
                        value = values[0],
                        format = "%.1f"
                    )
                }
                sensorValues[Sensor.TYPE_PROXIMITY]?.let { values ->
                    SensorSingleData(
                        name = stringResource(R.string.sensor_name_proximity),
                        unit = stringResource(R.string.unit_cm),
                        value = values[0],
                        format = "%.1f"
                    )
                }
                sensorValues[Sensor.TYPE_PRESSURE]?.let { values ->
                    SensorSingleData(
                        name = stringResource(R.string.sensor_name_pressure),
                        unit = stringResource(R.string.unit_hpa),
                        value = values[0]
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(stringResource(R.string.sensor_help_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.sensor_help_available),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.sensor_help_this_bg),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.sensor_help_unavailable),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.sensor_help_this_bg),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.sensor_help_tap_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Text(
                    text = stringResource(android.R.string.ok),
                    modifier = Modifier.clickable { showHelpDialog = false },
                    color = MaterialTheme.colorScheme.primary
                )
            }
        )
    }

    if (showOrientationHelp) {
        AlertDialog(
            onDismissRequest = { showOrientationHelp = false },
            title = { Text(stringResource(R.string.sensor_orientation_help_title)) },
            text = { Text(stringResource(R.string.sensor_orientation_help_message)) },
            confirmButton = {
                Text(
                    text = stringResource(android.R.string.ok),
                    modifier = Modifier.clickable { showOrientationHelp = false },
                    color = MaterialTheme.colorScheme.primary
                )
            }
        )
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
private fun SensorMultiData(
    name: String,
    unit: String,
    axes: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            axes.forEach { (label, value) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "%.2f".format(value),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun SensorSingleData(
    name: String,
    unit: String,
    value: Float,
    modifier: Modifier = Modifier,
    format: String = "%.2f"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = format.format(value),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
