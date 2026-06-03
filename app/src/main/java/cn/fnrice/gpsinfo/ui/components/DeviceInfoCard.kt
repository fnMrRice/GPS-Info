package cn.fnrice.gpsinfo.ui.components

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

// 厂商中英文映射
private val manufacturerMap = mapOf(
    "xiaomi" to "小米",
    "huawei" to "华为",
    "honor" to "荣耀",
    "oneplus" to "一加",
    "oppo" to "OPPO",
    "vivo" to "vivo",
    "samsung" to "三星",
    "realme" to "真我",
    "meizu" to "魅族",
    "zte" to "中兴",
    "lenovo" to "联想",
    "motorola" to "摩托罗拉",
    "nokia" to "诺基亚",
    "sony" to "索尼",
    "asus" to "华硕",
    "google" to "谷歌",
    "tecno" to "传音",
    "black shark" to "黑鲨",
    "nothing" to "Nothing",
    "fairphone" to "Fairphone",
)

/**
 * 根据当前语言环境，为已知厂商添加本地化名称
 * 中文环境: "小米 (Xiaomi)"，英文环境: "Xiaomi"
 */
private fun localizedManufacturer(raw: String): String {
    val isChinese = Locale.getDefault().language == "zh"
    val lower = raw.lowercase()
    val match = manufacturerMap[lower]
    return when {
        match == null -> raw
        isChinese && match != raw -> "$match ($raw)"
        !isChinese && match != raw -> "$raw ($match)"
        else -> raw
    }
}

// ── 硬件信息卡片 ──
@Composable
fun HardwareInfoCard() {
    DeviceDetailCard(
        title = stringResource(R.string.section_hardware),
        icon = Icons.Default.PhoneAndroid,
    ) {
        InfoRow(stringResource(R.string.label_manufacturer), localizedManufacturer(Build.MANUFACTURER))
        InfoRow(stringResource(R.string.label_brand), localizedManufacturer(Build.BRAND))
        InfoRow(stringResource(R.string.label_model), Build.MODEL)
        InfoRow(stringResource(R.string.label_product), Build.PRODUCT)
        InfoRow(stringResource(R.string.label_device), Build.DEVICE)
        InfoRow(stringResource(R.string.label_board), Build.BOARD)
        InfoRow(stringResource(R.string.label_hardware), Build.HARDWARE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            InfoRow(stringResource(R.string.label_soc_manufacturer), Build.SOC_MANUFACTURER)
            InfoRow(stringResource(R.string.label_soc_model), Build.SOC_MODEL)
        }
    }
}

// ── 系统信息卡片 ──
@Composable
fun SystemInfoCard() {
    DeviceDetailCard(
        title = stringResource(R.string.section_system),
        icon = Icons.Default.Android,
    ) {
        InfoRow(stringResource(R.string.label_android_version), "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        InfoRow(stringResource(R.string.label_build_id), Build.ID)
        InfoRow(stringResource(R.string.label_build_display), Build.DISPLAY)
        InfoRow(stringResource(R.string.label_build_type), Build.TYPE)
        InfoRow(stringResource(R.string.label_build_tags), Build.TAGS)
        InfoRow(stringResource(R.string.label_bootloader), Build.BOOTLOADER)
        InfoRow(stringResource(R.string.label_security_patch), Build.VERSION.SECURITY_PATCH.ifEmpty { "---" })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val baseOs = Build.VERSION.BASE_OS
            InfoRow(stringResource(R.string.label_base_os), if (baseOs.isNotEmpty()) baseOs else "---")
        }
        InfoRow(stringResource(R.string.label_kernel), getKernelVersion())
    }
}

// ── 屏幕信息卡片 ──
@Composable
fun DisplayInfoCard() {
    val context = LocalContext.current
    val displayInfo = remember { getDisplayInfo(context) }
    DeviceDetailCard(
        title = stringResource(R.string.section_display),
        icon = Icons.Default.Straighten,
    ) {
        InfoRow(stringResource(R.string.label_resolution), displayInfo.resolution)
        InfoRow(stringResource(R.string.label_density), displayInfo.density)
        InfoRow(stringResource(R.string.label_density_dpi), displayInfo.densityDpi)
    }
}

// ── 内存信息卡片 ──
@Composable
fun MemoryInfoCard() {
    val context = LocalContext.current
    val memInfo = remember { getMemoryInfo(context) }
    DeviceDetailCard(
        title = stringResource(R.string.section_memory),
        icon = Icons.Default.Memory,
    ) {
        InfoRow(stringResource(R.string.label_total_ram), memInfo.totalRam)
        InfoRow(stringResource(R.string.label_available_ram), memInfo.availableRam)
    }
}

// ── 区域与语言卡片 ──
@Composable
fun LocaleInfoCard() {
    val locale = Locale.getDefault()
    DeviceDetailCard(
        title = stringResource(R.string.section_locale),
        icon = Icons.Default.Public,
    ) {
        InfoRow(stringResource(R.string.label_language), "${locale.language} (${locale.displayLanguage})")
        InfoRow(stringResource(R.string.label_country), "${locale.country} (${locale.displayCountry})")
        InfoRow(stringResource(R.string.label_timezone), java.util.TimeZone.getDefault().id)
    }
}

// ── 应用信息卡片 ──
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
    DeviceDetailCard(
        title = stringResource(R.string.section_app),
        icon = Icons.Default.Apps,
    ) {
        InfoRow(stringResource(R.string.label_package_name), context.packageName)
        InfoRow(stringResource(R.string.label_min_sdk), "API ${context.applicationInfo.minSdkVersion}")
        InfoRow(stringResource(R.string.label_target_sdk), "API ${context.applicationInfo.targetSdkVersion}")
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

private data class DisplayInfoData(val resolution: String, val density: String, val densityDpi: String)

private fun getDisplayInfo(context: Context): DisplayInfoData {
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
        DisplayInfoData(
            resolution = "${metrics.widthPixels} × ${metrics.heightPixels}",
            density = "${metrics.density}x ($densityBucket)",
            densityDpi = "${metrics.densityDpi} dpi"
        )
    } catch (_: Exception) {
        DisplayInfoData("---", "---", "---")
    }
}

private data class MemoryInfoData(val totalRam: String, val availableRam: String)

private fun getMemoryInfo(context: Context): MemoryInfoData {
    return try {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        MemoryInfoData(
            totalRam = formatBytes(memInfo.totalMem),
            availableRam = formatBytes(memInfo.availMem)
        )
    } catch (_: Exception) {
        MemoryInfoData("---", "---")
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
} catch (_: Exception) {
    null
}
