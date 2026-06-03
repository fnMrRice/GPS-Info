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
 * 例: "Xiaomi (小米)"
 */
private fun localizedManufacturer(raw: String): String {
    val lower = raw.lowercase()
    val match = manufacturerMap[lower]
    return if (match != null && match != raw) "$raw ($match)" else raw
}

// ── 硬件信息卡片 ──
@Composable
fun HardwareInfoCard() {
    var expanded by rememberSaveable { mutableStateOf(false) }
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
            InfoRow(stringResource(R.string.label_soc_manufacturer), Build.SOC_MANUFACTURER)
            val socModel = Build.SOC_MODEL
            val commercialName = socCommercialName(socModel)
            InfoRow(stringResource(R.string.label_soc_model), if (commercialName != null) "$commercialName ($socModel)" else socModel)
        }
    }
}

// ── 系统信息卡片 ──
@Composable
fun SystemInfoCard() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.section_system),
        icon = Icons.Default.Android,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
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
        val radioVersion = Build.getRadioVersion()
        InfoRow(stringResource(R.string.label_baseband), radioVersion.ifEmpty { "---" })
    }
}

// ── 屏幕信息卡片 ──
@Composable
fun DisplayInfoCard() {
    val context = LocalContext.current
    val displayInfo = remember { getDisplayInfo(context) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.section_display),
        icon = Icons.Default.Straighten,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
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
    var expanded by rememberSaveable { mutableStateOf(false) }
    AppCard(
        title = stringResource(R.string.section_memory),
        icon = Icons.Default.Memory,
        isExpandable = true,
        isExpanded = expanded,
        onExpandChange = { expanded = it },
    ) {
        InfoRow(stringResource(R.string.label_total_ram), memInfo.totalRam)
        InfoRow(stringResource(R.string.label_available_ram), memInfo.availableRam)
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

// ── SoC 商用名映射 ──

private val socNameMap = mapOf(
    // Qualcomm Snapdragon 8 系列
    "SM8750" to "Snapdragon 8 Elite",
    "SM8650" to "Snapdragon 8 Gen 3",
    "SM8550" to "Snapdragon 8 Gen 2",
    "SM8475" to "Snapdragon 8+ Gen 1",
    "SM8450" to "Snapdragon 8 Gen 1",
    "SM8350" to "Snapdragon 888",
    "SM8250" to "Snapdragon 865",
    "SM8150" to "Snapdragon 855",
    // Qualcomm Snapdragon 7 系列
    "SM7675" to "Snapdragon 7+ Gen 3",
    "SM7550" to "Snapdragon 7 Gen 3",
    "SM7475" to "Snapdragon 7+ Gen 2",
    "SM7450" to "Snapdragon 7 Gen 1",
    "SM7350" to "Snapdragon 778G",
    "SM7325" to "Snapdragon 778G",
    "SM7225" to "Snapdragon 750G",
    "SM7150" to "Snapdragon 730",
    // Qualcomm Snapdragon 6 系列
    "SM6475" to "Snapdragon 6s Gen 3",
    "SM6450" to "Snapdragon 6 Gen 1",
    "SM6375" to "Snapdragon 695",
    "SM6225" to "Snapdragon 680",
    "SM6150" to "Snapdragon 675",
    "SM6125" to "Snapdragon 665",
    // Qualcomm Snapdragon 4 系列
    "SM4450" to "Snapdragon 4 Gen 2",
    "SM4375" to "Snapdragon 480",
    "SM4350" to "Snapdragon 4 Gen 1",
    // Qualcomm 其他
    "QCM2290" to "Qualcomm 215",
    "QCM4490" to "Qualcomm QCM4490",
    // MediaTek Dimensity 9000 系列
    "MT6989" to "Dimensity 9300",
    "MT6985" to "Dimensity 9200",
    "MT6983" to "Dimensity 9000",
    // MediaTek Dimensity 8000 系列
    "MT6897" to "Dimensity 8300",
    "MT6895" to "Dimensity 8200",
    "MT6893" to "Dimensity 8100",
    "MT6891" to "Dimensity 8000",
    // MediaTek Dimensity 7000 系列
    "MT6886" to "Dimensity 7200",
    "MT6879" to "Dimensity 7050",
    "MT6878" to "Dimensity 7030",
    "MT6877" to "Dimensity 1080",
    // MediaTek Dimensity 6000 / 其他
    "MT6875" to "Dimensity 800U",
    "MT6873" to "Dimensity 720",
    "MT6853" to "Dimensity 700",
    "MT6833" to "Dimensity 6020",
    "MT6789" to "Helio G99",
    "MT6785" to "Helio G90T",
    "MT6769" to "Helio G70",
    "MT6768" to "Helio G80",
    "MT6765" to "Helio G37",
    "MT6762" to "Helio P22",
    // Samsung Exynos
    "S5E9945" to "Exynos 2500",
    "S5E9940" to "Exynos 2400",
    "S5E9925" to "Exynos 2200",
    "S5E9840" to "Exynos 2100",
    "S5E9830" to "Exynos 990",
    "S5E9820" to "Exynos 9820",
    "S5E9810" to "Exynos 9810",
    "S5E8825" to "Exynos 1280",
    "S5E8815" to "Exynos 850",
    "S5E8530" to "Exynos 1330",
    // Google Tensor
    "GS201" to "Tensor G2",
    "GS301" to "Tensor G3",
    "GS401" to "Tensor G4",
    "GS101" to "Tensor",
    // HiSilicon Kirin
    "Kirin9000" to "Kirin 9000",
    "Kirin990" to "Kirin 990",
    "Kirin980" to "Kirin 980",
    "Kirin970" to "Kirin 970",
    "Kirin810" to "Kirin 810",
    "Kirin710" to "Kirin 710",
)

private fun socCommercialName(model: String): String? = socNameMap[model.uppercase()]

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
