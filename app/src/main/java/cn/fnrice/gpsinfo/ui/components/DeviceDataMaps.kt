package cn.fnrice.gpsinfo.ui.components

// ── 厂商中英文映射 ──

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
 * 为已知厂商添加本地化名称，例: "Xiaomi (小米)"
 */
fun localizedManufacturer(raw: String): String {
    val lower = raw.lowercase()
    val match = manufacturerMap[lower]
    return if (match != null && match != raw) "$raw ($match)" else raw
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

/**
 * SoC 内部代号转商用名，例: "SM8550" → "Snapdragon 8 Gen 2"
 */
fun socCommercialName(model: String): String? = socNameMap[model.uppercase()]
