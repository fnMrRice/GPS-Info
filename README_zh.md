# GPS Info

[English](README.md) | 中文

Android 实时 GNSS 卫星定位查看器。在极坐标天图上可视化可见卫星，按星座查看信号强度，监测设备传感器数据 —— 全部基于 Material 3 自适应 UI。

## 功能特性

- **卫星天图** — 地图上叠加极坐标图，显示卫星位置，按星座着色（GPS、GLONASS、Galileo、BeiDou、QZSS、SBAS、NavIC）
- **实时卫星列表** — PRN、C/N0、仰角、方位角、载波频率、是否参与定位；同一卫星的多频信号（L1/L5/E5）自动合并
- **位置信息卡** — 经纬度、海拔、速度、方向角、精度，支持 GPS/网络/最后已知来源标识
- **传感器仪表盘** — 方向、运动（加速度计、陀螺仪、重力）、环境（磁场、光线、距离、气压）
- **设备信息与 GNSS 能力** — 制造商、型号、Android 版本、GNSS 硬件功能标志（Android 12+）
- **自适应导航** — 手机底部栏、平板导航栏、桌面端抽屉，基于 `NavigationSuiteScaffold`
- **多地图供应商** — Google Maps、高德地图、百度地图，根据网络连通性自动选择
- **深色主题与动态配色** — Android 12+ 支持 Material You 主题，深色模式自定义地图样式
- **本地化** — 英文与简体中文

## 截图

> _待添加_

## 系统要求

- Android 7.0（API 24）或更高版本
- `ACCESS_FINE_LOCATION` 权限（GNSS 数据必需）
- 至少一个地图供应商的 API Key（Google Maps、高德地图或百度地图）

## 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/fnrice/GPS-Info.git
cd GPS-Info
```

### 2. 配置 API Key

地图 SDK 的 API Key 通过 `gradle.properties` 或 `local.properties` 注入：

```properties
GOOGLE_MAPS_KEY=你的Google地图API Key
AMAP_KEY=你的高德地图API Key
BAIDU_MAPS_KEY=你的百度地图API Key
```

各供应商 API Key 的获取方式详见 [`docs/api_keys.md`](docs/api_keys.md)。

### 3. 构建运行

```bash
./gradlew installDebug
```

或直接构建 APK：

```bash
./gradlew assembleDebug
```

调试 APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`。

### 4. 运行测试

```bash
./gradlew test                 # 单元测试
./gradlew connectedAndroidTest # 设备测试
```

## 项目结构

```
app/src/main/java/cn/fnrice/gpsinfo/
  MainActivity.kt              -- 入口，导航宿主，权限处理
  data/
    GnssModels.kt              -- 数据模型：SatelliteInfo、SatelliteGroup、LocationInfo、GnssState
    SettingsRepository.kt      -- DataStore 偏好存储
    MockDataProvider.kt        -- 模拟数据（测试/开发用）
  viewmodel/
    GnssViewModel.kt           -- GNSS、传感器、设置状态管理
  ui/
    screen/
      HomeScreen.kt            -- 卫星列表 + 天图
      SensorScreen.kt          -- 硬件传感器读数
      DeviceScreen.kt          -- 设备信息、能力、设置
    components/
      SkyView.kt               -- Canvas 极坐标天图
      MapComponents.kt         -- 高德地图和 Google Maps 集成
      SatelliteCard.kt         -- 可展开卫星详情卡片
      ...
    theme/                     -- Material 3 主题、配色、字体
```

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 导航 | NavigationSuiteScaffold（自适应） |
| 状态管理 | Kotlin StateFlow |
| 持久化 | DataStore Preferences |
| 地图 | Google Maps Compose、高德 3D 地图 SDK |
| 构建 | Gradle 9.5.1（Kotlin DSL）、AGP 9.2.1 |
| 最低 SDK | 24（Android 7.0） |
| 目标 SDK | 37 |

## 开发者模式

在设备页面连续点击应用版本号 7 次，解锁开发者选项：

- **自定义 API Key** — 按供应商覆盖地图 Key，附带测试按钮
- **模拟数据模式** — 以北京为中心的模拟卫星数据，无需 GPS 信号即可调试
- **日志查看器** — 系统日志，支持自动滚动和清空

## 许可证

详见 [LICENSE](LICENSE)。
