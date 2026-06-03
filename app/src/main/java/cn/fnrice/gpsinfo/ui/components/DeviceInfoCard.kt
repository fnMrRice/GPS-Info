package cn.fnrice.gpsinfo.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import java.io.File
import java.util.Locale

// ── 硬件信息卡片 ──
@Composable
fun HardwareInfoCard() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val cpuAbis = remember { Build.SUPPORTED_ABIS.toList() }
    val cpuCores = remember { Runtime.getRuntime().availableProcessors() }
    AppCard(
        title = stringResource(R.string.section_hardware),
        icon = Icons.Default.PhoneAndroid,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
    ) {
        InfoRow(stringResource(R.string.label_manufacturer), localizedManufacturer(Build.MANUFACTURER))
        InfoRow(stringResource(R.string.label_brand), localizedManufacturer(Build.BRAND))
        InfoRow(stringResource(R.string.label_model), Build.MODEL)
        InfoRow(stringResource(R.string.label_product), Build.PRODUCT)
        InfoRow(stringResource(R.string.label_device), Build.DEVICE)
        InfoRow(stringResource(R.string.label_board), Build.BOARD)
        InfoRow(stringResource(R.string.label_hardware), Build.HARDWARE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            InfoRow(stringResource(R.string.label_soc_manufacturer), localizedSocManufacturer(Build.SOC_MANUFACTURER))
            val socModel = Build.SOC_MODEL
            val commercialName = socCommercialName(Build.SOC_MANUFACTURER, socModel)
            InfoRow(stringResource(R.string.label_soc_model), if (commercialName != null) "$commercialName ($socModel)" else socModel)
        }
        InfoRow(stringResource(R.string.label_cpu_abi), cpuAbis.joinToString(", "))
        InfoRow(stringResource(R.string.label_cpu_cores), cpuCores.toString())
    }
}

// ── 系统信息卡片 ──
@Composable
fun SystemInfoCard() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val uptime = remember { getUptime() }
    AppCard(
        title = stringResource(R.string.section_system),
        icon = Icons.Default.Android,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
    ) {
        InfoRow(stringResource(R.string.label_android_version), "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        InfoRow(stringResource(R.string.label_codename), Build.VERSION.CODENAME)
        InfoRow(stringResource(R.string.label_build_id), Build.ID)
        InfoRow(stringResource(R.string.label_build_display), Build.DISPLAY)
        InfoRow(stringResource(R.string.label_build_type), Build.TYPE)
        InfoRow(stringResource(R.string.label_build_tags), Build.TAGS)
        InfoRow(stringResource(R.string.label_bootloader), Build.BOOTLOADER)
        InfoRow(stringResource(R.string.label_fingerprint), Build.FINGERPRINT.take(80))
        InfoRow(stringResource(R.string.label_security_patch), Build.VERSION.SECURITY_PATCH.ifEmpty { "---" })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val baseOs = Build.VERSION.BASE_OS
            InfoRow(stringResource(R.string.label_base_os), if (baseOs.isNotEmpty()) baseOs else "---")
        }
        InfoRow(stringResource(R.string.label_kernel), getKernelVersion())
        val radioVersion = Build.getRadioVersion()
        InfoRow(stringResource(R.string.label_baseband), radioVersion.ifEmpty { "---" })
        InfoRow(stringResource(R.string.label_uptime), uptime)
    }
}

// ── 屏幕信息卡片 ──
@Composable
fun DisplayInfoCard() {
    val context = LocalContext.current
    val displayData = remember { getDisplayData(context) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.section_display),
        icon = Icons.Default.Straighten,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
    ) {
        InfoRow(stringResource(R.string.label_resolution), displayData.resolution)
        InfoRow(stringResource(R.string.label_density), displayData.density)
        InfoRow(stringResource(R.string.label_density_dpi), displayData.densityDpi)
        InfoRow(stringResource(R.string.label_refresh_rate), displayData.refreshRate)
        InfoRow(stringResource(R.string.label_screen_size), displayData.screenSize)
        InfoRow(stringResource(R.string.label_orientation), displayData.orientation)
    }
}

// ── 存储与内存卡片 ──
@Composable
fun StorageInfoCard() {
    val context = LocalContext.current
    val storageData = remember { getStorageData(context) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.section_storage),
        icon = Icons.Default.SdStorage,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
    ) {
        InfoRow(stringResource(R.string.label_total_ram), storageData.totalRam)
        InfoRow(stringResource(R.string.label_available_ram), storageData.availableRam)
        InfoRow(stringResource(R.string.label_internal_total), storageData.internalTotal)
        InfoRow(stringResource(R.string.label_internal_available), storageData.internalAvail)
        InfoRow(stringResource(R.string.label_external_total), storageData.externalTotal)
        InfoRow(stringResource(R.string.label_external_available), storageData.externalAvail)
    }
}

// ── 网络信息卡片 ──
@Composable
fun NetworkInfoCard() {
    val context = LocalContext.current
    val networkData = remember { getNetworkData(context) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.section_network),
        icon = Icons.Default.NetworkWifi,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
    ) {
        InfoRow(stringResource(R.string.label_network_type), networkData.type)
        InfoRow(stringResource(R.string.label_carrier), networkData.carrier)
        InfoRow(stringResource(R.string.label_wifi_ssid), networkData.wifiSsid)
        InfoRow(stringResource(R.string.label_ip_address), networkData.ipAddress)
    }
}

// ── 电池信息卡片 ──
@Composable
fun BatteryInfoCard() {
    val context = LocalContext.current
    val batteryData = remember { getBatteryData(context) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.section_battery),
        icon = Icons.Default.BatteryChargingFull,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
    ) {
        InfoRow(stringResource(R.string.label_battery_level), batteryData.level)
        InfoRow(stringResource(R.string.label_battery_status), batteryData.status)
        InfoRow(stringResource(R.string.label_battery_health), batteryData.health)
        InfoRow(stringResource(R.string.label_battery_technology), batteryData.technology)
        InfoRow(stringResource(R.string.label_battery_temperature), batteryData.temperature)
        InfoRow(stringResource(R.string.label_battery_voltage), batteryData.voltage)
    }
}

// ── 区域与语言卡片 ──
@Composable
fun LocaleInfoCard() {
    val locale = Locale.getDefault()
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.section_locale),
        icon = Icons.Default.Public,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
    ) {
        InfoRow(stringResource(R.string.label_language), "${locale.language} (${locale.displayLanguage})")
        InfoRow(stringResource(R.string.label_country), "${locale.country} (${locale.displayCountry})")
        InfoRow(stringResource(R.string.label_timezone), java.util.TimeZone.getDefault().id)
    }
}

// ── 当前应用信息卡片 ──
@Composable
fun AppInfoCard(onVersionClick: () -> Unit) {
    val context = LocalContext.current
    val packageInfo = remember(context) { getPackageInfo(context) }
    val versionName = packageInfo?.versionName ?: "1.0.0"
    val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageInfo?.longVersionCode?.toString() ?: "1"
    } else {
        @Suppress("DEPRECATION")
        packageInfo?.versionCode?.toString() ?: "1"
    }
    val firstInstall = remember(packageInfo) {
        packageInfo?.let {
            java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date(it.firstInstallTime))
        } ?: "---"
    }
    val lastUpdate = remember(packageInfo) {
        packageInfo?.let {
            java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date(it.lastUpdateTime))
        } ?: "---"
    }
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.section_app),
        icon = Icons.Default.Apps,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
    ) {
        InfoRow(stringResource(R.string.label_package_name), context.packageName)
        InfoRow(stringResource(R.string.label_min_sdk), "API ${context.applicationInfo.minSdkVersion}")
        InfoRow(stringResource(R.string.label_target_sdk), "API ${context.applicationInfo.targetSdkVersion}")
        InfoRow(stringResource(R.string.label_first_install), firstInstall)
        InfoRow(stringResource(R.string.label_last_update), lastUpdate)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onVersionClick)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.label_app_version),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "$versionName ($versionCode)",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── 工具函数 ──

private fun getKernelVersion(): String {
    return try {
        File("/proc/version").readText().trim().let { full ->
            val match = Regex("""\d+\.\d+\.\d+[-\w]*""").find(full)
            match?.value ?: full.take(60)
        }
    } catch (_: Exception) {
        System.getProperty("os.version") ?: "---"
    }
}

private fun getUptime(): String {
    return try {
        val ms = android.os.SystemClock.elapsedRealtime()
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        "${hours}h ${minutes}m"
    } catch (_: Exception) { "---" }
}

// ── Display ──

private data class DisplayData(
    val resolution: String, val density: String, val densityDpi: String,
    val refreshRate: String, val screenSize: String, val orientation: String
)

private fun getDisplayData(context: Context): DisplayData {
    return try {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        val densityBucket = when {
            metrics.densityDpi <= 120 -> "ldpi"
            metrics.densityDpi <= 160 -> "mdpi"
            metrics.densityDpi <= 240 -> "hdpi"
            metrics.densityDpi <= 320 -> "xhdpi"
            metrics.densityDpi <= 480 -> "xxhdpi"
            metrics.densityDpi <= 640 -> "xxxhdpi"
            else -> "xxxhdpi+"
        }
        val xInches = metrics.widthPixels.toDouble() / metrics.xdpi
        val yInches = metrics.heightPixels.toDouble() / metrics.ydpi
        val diagonal = "%.1f\"".format(Math.sqrt(xInches * xInches + yInches * yInches))
        val refresh = "%.0f Hz".format(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.display.refreshRate
            } else {
                @Suppress("DEPRECATION")
                wm.defaultDisplay.refreshRate
            }
        )
        val orient = when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> "Landscape"
            Configuration.ORIENTATION_PORTRAIT -> "Portrait"
            else -> "---"
        }
        DisplayData(
            resolution = "${metrics.widthPixels} × ${metrics.heightPixels}",
            density = "${metrics.density}x ($densityBucket)",
            densityDpi = "${metrics.densityDpi} dpi",
            refreshRate = refresh,
            screenSize = diagonal,
            orientation = orient
        )
    } catch (_: Exception) {
        DisplayData("---", "---", "---", "---", "---", "---")
    }
}

// ── Storage & Memory ──

private data class StorageData(
    val totalRam: String, val availableRam: String,
    val internalTotal: String, val internalAvail: String,
    val externalTotal: String, val externalAvail: String
)

private fun getStorageData(context: Context): StorageData {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    am.getMemoryInfo(memInfo)

    val internalStat = StatFs(Environment.getDataDirectory().path)
    val internalTotal = internalStat.blockSizeLong * internalStat.blockCountLong
    val internalAvail = internalStat.blockSizeLong * internalStat.availableBlocksLong

    var extTotal = 0L
    var extAvail = 0L
    try {
        val extDir = Environment.getExternalStorageDirectory()
        val extStat = StatFs(extDir.path)
        extTotal = extStat.blockSizeLong * extStat.blockCountLong
        extAvail = extStat.blockSizeLong * extStat.availableBlocksLong
    } catch (_: Exception) {}

    return StorageData(
        totalRam = formatBytes(memInfo.totalMem),
        availableRam = formatBytes(memInfo.availMem),
        internalTotal = formatBytes(internalTotal),
        internalAvail = formatBytes(internalAvail),
        externalTotal = if (extTotal > 0) formatBytes(extTotal) else "---",
        externalAvail = if (extAvail > 0) formatBytes(extAvail) else "---"
    )
}

// ── Network ──

private data class NetworkData(
    val type: String, val carrier: String, val wifiSsid: String, val ipAddress: String
)

@Suppress("DEPRECATION")
private fun getNetworkData(context: Context): NetworkData {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    val networkType = try {
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)
        when {
            caps == null -> "---"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "Other"
        }
    } catch (_: Exception) { "---" }

    val carrier = try { tm.networkOperatorName ?: "---" } catch (_: Exception) { "---" }

    val wifiSsid = try {
        if (networkType == "Wi-Fi") {
            val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val info = wm.connectionInfo
            info.ssid?.removeSurrounding("\"") ?: "---"
        } else "---"
    } catch (_: Exception) { "---" }

    val ip = try {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipInt = wm.connectionInfo.ipAddress
        if (ipInt != 0) {
            "${ipInt and 0xFF}.${ipInt shr 8 and 0xFF}.${ipInt shr 16 and 0xFF}.${ipInt shr 24 and 0xFF}"
        } else "---"
    } catch (_: Exception) { "---" }

    return NetworkData(networkType, carrier, wifiSsid, ip)
}

// ── Battery ──

private data class BatteryData(
    val level: String, val status: String, val health: String,
    val technology: String, val temperature: String, val voltage: String
)

private fun getBatteryData(context: Context): BatteryData {
    return try {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val battery = context.registerReceiver(null, filter) ?: return BatteryData("---", "---", "---", "---", "---", "---")
        val level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val pct = if (level >= 0 && scale > 0) "${(level * 100 / scale)}%" else "---"

        val status = when (battery.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "---"
        }
        val health = when (battery.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "---"
        }
        val tech = battery.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "---"
        val temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val volt = battery.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        BatteryData(
            level = pct,
            status = status,
            health = health,
            technology = tech,
            temperature = "%.1f°C".format(temp / 10.0),
            voltage = "${volt} mV"
        )
    } catch (_: Exception) {
        BatteryData("---", "---", "---", "---", "---", "---")
    }
}

private fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) "%.1f GB".format(gb) else "%.0f MB".format(bytes / (1024.0 * 1024.0))
}

private fun getPackageInfo(context: Context) = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
} catch (_: Exception) { null }
