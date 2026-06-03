plugins {
    id("com.android.application")
//    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "cn.fnrice.gpsinfo"
    compileSdk = 37

    defaultConfig {
        applicationId = "cn.fnrice.gpsinfo"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val googleApiKey = project.findProperty("GOOGLE_MAPS_KEY") ?: ""
        val amapApiKey = project.findProperty("AMAP_KEY") ?: ""
        val baiduApiKey = project.findProperty("BAIDU_MAPS_KEY") ?: ""

        manifestPlaceholders["GOOGLE_MAPS_KEY"] = googleApiKey
        manifestPlaceholders["AMAP_KEY"] = amapApiKey
        manifestPlaceholders["BAIDU_MAPS_KEY"] = baiduApiKey

        buildConfigField("String", "GOOGLE_MAPS_KEY", "\"$googleApiKey\"")
        buildConfigField("String", "AMAP_KEY", "\"$amapApiKey\"")
        buildConfigField("String", "BAIDU_MAPS_KEY", "\"$baiduApiKey\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.findProperty("RELEASE_STORE_FILE") ?: "release.jks")
            storePassword = project.findProperty("RELEASE_STORE_PASSWORD")?.toString() ?: ""
            keyAlias = project.findProperty("RELEASE_KEY_ALIAS")?.toString() ?: ""
            keyPassword = project.findProperty("RELEASE_KEY_PASSWORD")?.toString() ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
    implementation("androidx.navigation:navigation-compose:2.9.0")

    // Google Maps for Compose
    implementation("com.google.maps.android:maps-compose:8.3.0")
    implementation("com.google.android.gms:play-services-maps:20.0.0")

    // 高德地图 (AMap) - 3D地图
    implementation("com.amap.api:3dmap:10.0.600")

    // 百度地图 (Baidu Maps)
    // 百度地图目前官方推荐通过 JAR/AAR 手动导入或特定私有仓库，
    // 此处预留依赖配置，实际使用时建议参考 docs/api_keys.md
    // implementation("com.baidu.lbsapi:sdk:x.x.x")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}