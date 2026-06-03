package cn.fnrice.gpsinfo.ui.components

// ── 设备厂商中英文映射 ──

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
 * 为已知设备厂商添加本地化名称，例: "Xiaomi (小米)"
 */
fun localizedManufacturer(raw: String): String {
    val lower = raw.lowercase()
    val match = manufacturerMap[lower]
    return if (match != null && match != raw) "$raw ($match)" else raw
}

// ── SoC 制造商中英文映射 ──

private val socManufacturerMap = mapOf(
    "qualcomm" to "高通",
    "mediatek" to "联发科",
    "samsung" to "三星",
    "google" to "谷歌",
    "hisilicon" to "海思",
    "unisoc" to "紫光展锐",
    "spreadtrum" to "展讯",
)

/**
 * 为已知 SoC 制造商添加本地化名称，例: "Qualcomm (高通)"
 */
fun localizedSocManufacturer(raw: String): String {
    val lower = raw.lowercase()
    val match = socManufacturerMap[lower]
    return if (match != null && match != raw) "$raw ($match)" else raw
}

// ── SoC 商用名映射（按厂商分组） ──

private data class SocKey(val manufacturer: String, val model: String)

private val socNameMap = mapOf(
    // Qualcomm Snapdragon 8 系列
    SocKey("qualcomm", "SM8750") to "Snapdragon 8 Elite",
    SocKey("qualcomm", "SM8650") to "Snapdragon 8 Gen 3",
    SocKey("qualcomm", "SM8550") to "Snapdragon 8 Gen 2",
    SocKey("qualcomm", "SM8475") to "Snapdragon 8+ Gen 1",
    SocKey("qualcomm", "SM8450") to "Snapdragon 8 Gen 1",
    SocKey("qualcomm", "SM8350") to "Snapdragon 888",
    SocKey("qualcomm", "SM8250") to "Snapdragon 865",
    SocKey("qualcomm", "SM8150") to "Snapdragon 855",
    // Qualcomm Snapdragon 7 系列
    SocKey("qualcomm", "SM7675") to "Snapdragon 7+ Gen 3",
    SocKey("qualcomm", "SM7550") to "Snapdragon 7 Gen 3",
    SocKey("qualcomm", "SM7475") to "Snapdragon 7+ Gen 2",
    SocKey("qualcomm", "SM7450") to "Snapdragon 7 Gen 1",
    SocKey("qualcomm", "SM7350") to "Snapdragon 778G",
    SocKey("qualcomm", "SM7325") to "Snapdragon 778G",
    SocKey("qualcomm", "SM7225") to "Snapdragon 750G",
    SocKey("qualcomm", "SM7150") to "Snapdragon 730",
    // Qualcomm Snapdragon 6 系列
    SocKey("qualcomm", "SM6475") to "Snapdragon 6s Gen 3",
    SocKey("qualcomm", "SM6450") to "Snapdragon 6 Gen 1",
    SocKey("qualcomm", "SM6375") to "Snapdragon 695",
    SocKey("qualcomm", "SM6225") to "Snapdragon 680",
    SocKey("qualcomm", "SM6150") to "Snapdragon 675",
    SocKey("qualcomm", "SM6125") to "Snapdragon 665",
    // Qualcomm Snapdragon 4 系列
    SocKey("qualcomm", "SM4450") to "Snapdragon 4 Gen 2",
    SocKey("qualcomm", "SM4375") to "Snapdragon 480",
    SocKey("qualcomm", "SM4350") to "Snapdragon 4 Gen 1",
    // Qualcomm 其他
    SocKey("qualcomm", "QCM2290") to "Qualcomm 215",
    SocKey("qualcomm", "QCM4490") to "Qualcomm QCM4490",
    // MediaTek Dimensity 9000 系列
    SocKey("mediatek", "MT6989") to "Dimensity 9300",
    SocKey("mediatek", "MT6985") to "Dimensity 9200",
    SocKey("mediatek", "MT6983") to "Dimensity 9000",
    // MediaTek Dimensity 8000 系列
    SocKey("mediatek", "MT6897") to "Dimensity 8300",
    SocKey("mediatek", "MT6895") to "Dimensity 8200",
    SocKey("mediatek", "MT6893") to "Dimensity 8100",
    SocKey("mediatek", "MT6891") to "Dimensity 8000",
    // MediaTek Dimensity 7000 系列
    SocKey("mediatek", "MT6886") to "Dimensity 7200",
    SocKey("mediatek", "MT6879") to "Dimensity 7050",
    SocKey("mediatek", "MT6878") to "Dimensity 7030",
    SocKey("mediatek", "MT6877") to "Dimensity 1080",
    // MediaTek Dimensity 6000 / 其他
    SocKey("mediatek", "MT6875") to "Dimensity 800U",
    SocKey("mediatek", "MT6873") to "Dimensity 720",
    SocKey("mediatek", "MT6853") to "Dimensity 700",
    SocKey("mediatek", "MT6833") to "Dimensity 6020",
    SocKey("mediatek", "MT6789") to "Helio G99",
    SocKey("mediatek", "MT6785") to "Helio G90T",
    SocKey("mediatek", "MT6769") to "Helio G70",
    SocKey("mediatek", "MT6768") to "Helio G80",
    SocKey("mediatek", "MT6765") to "Helio G37",
    SocKey("mediatek", "MT6762") to "Helio P22",
    // Samsung Exynos
    SocKey("samsung", "S5E9945") to "Exynos 2500",
    SocKey("samsung", "S5E9940") to "Exynos 2400",
    SocKey("samsung", "S5E9925") to "Exynos 2200",
    SocKey("samsung", "S5E9840") to "Exynos 2100",
    SocKey("samsung", "S5E9830") to "Exynos 990",
    SocKey("samsung", "S5E9820") to "Exynos 9820",
    SocKey("samsung", "S5E9810") to "Exynos 9810",
    SocKey("samsung", "S5E8825") to "Exynos 1280",
    SocKey("samsung", "S5E8815") to "Exynos 850",
    SocKey("samsung", "S5E8530") to "Exynos 1330",
    // Google Tensor
    SocKey("google", "GS201") to "Tensor G2",
    SocKey("google", "GS301") to "Tensor G3",
    SocKey("google", "GS401") to "Tensor G4",
    SocKey("google", "GS101") to "Tensor",
    // HiSilicon Kirin
    SocKey("hisilicon", "Kirin9000") to "Kirin 9000",
    SocKey("hisilicon", "Kirin990") to "Kirin 990",
    SocKey("hisilicon", "Kirin980") to "Kirin 980",
    SocKey("hisilicon", "Kirin970") to "Kirin 970",
    SocKey("hisilicon", "Kirin810") to "Kirin 810",
    SocKey("hisilicon", "Kirin710") to "Kirin 710",
)

/**
 * SoC 内部代号转商用名（需同时匹配厂商和型号）
 * 例: ("Qualcomm", "SM8550") → "Snapdragon 8 Gen 2"
 */
fun socCommercialName(manufacturer: String, model: String): String? {
    return socNameMap[SocKey(manufacturer.lowercase(), model.uppercase())]
}
