# 地图 API Key 获取指南

本项目集成了 Google 地图、高德地图和百度地图。为了使地图功能正常工作，你需要获取相应的 API Key 并将其配置在项目中。

## 1. 配置文件位置

为了安全起见，API Key 不应直接提交到版本控制系统。建议将它们放在项目根目录下的 `local.properties` 文件中：

```properties
GOOGLE_MAPS_KEY=你的Google地图API_KEY
AMAP_KEY=你的高德地图API_KEY
BAIDU_MAPS_KEY=你的百度地图API_KEY
```

## 2. 如何获取 API Key

### 2.1 高德地图 (AMap)

1. 登录 [高德开放平台控制台](https://console.amap.com/)。
2. 进入“应用管理” -> “我的应用”。
3. 创建新应用，或在现有应用中点击“添加 Key”。
4. 选择服务平台为 **Android 平台**。
5. 填写 **发布版安全码 SHA1** 和 **调试版安全码 SHA1**。
    - 调试版通常可以使用 Android 默认的 debug.keystore 获取。
6. 填写 **Package Name** (包名): `cn.fnrice.gpsinfo`。
7. 同意协议并提交，即可获得 API Key。

### 2.2 百度地图 (Baidu Maps)

1. 登录 [百度地图开放平台控制台](https://lbsyun.baidu.com/apiconsole/center)。
2. 进入“应用管理” -> “我的应用”。
3. 点击“创建应用”。
4. 应用类型选择 **Android SDK**。
5. 填写 **启用服务**（默认全选或勾选 Android 地图 SDK）。
6. 填写 **SHA1** (发布版和调试版) 和 **Package Name**: `cn.fnrice.gpsinfo`。
7. 提交后即可获得访问应用（AK）。

### 2.3 Google 地图 (Google Maps)

1. 前往 [Google Cloud Console](https://console.cloud.google.com/)。
2. 创建一个新项目或选择现有项目。
3. 进入“API 和服务” -> “库”。
4. 搜索并启用 **Maps SDK for Android**。
5. 进入“API 和服务” -> “凭据”。
6. 点击“创建凭据” -> “API 密钥”。
7. **建议进行限制**：
    - 在“应用限制”中选择 **Android 应用**。
    - 添加包名 `cn.fnrice.gpsinfo` 和 SHA-1 指纹。
8. 复制生成的 API 密钥。

## 3. 获取 SHA1 指纹

在申请地图 SDK 的 Key 时，通常需要提供 SHA1 指纹。

### 3.1 方法一：使用内置脚本 (快捷方式)

在项目根目录下提供了用于快速获取 SHA1 的脚本：

- **Windows**: 运行 `scripts\get_sha1.ps1` (右键点击并选择“使用 PowerShell 运行”)。
- **Linux/macOS**: 在终端运行 `bash scripts/get_sha1.sh`。

脚本支持自动检测默认的调试版 (Debug) 路径，也支持通过交互方式获取发布版 (Release) 的指纹。

### 3.2 方法二：使用 Gradle 获取 (推荐)

这是最简单的方法，可以同时获取调试版和发布版（如果已配置签名）的 SHA1：

1. 在 Android Studio 右侧边栏点击 **Gradle** 图标。
2. 依次展开 `项目名` -> `Tasks` -> `android` -> `signingReport`。
3. 双击运行 `signingReport`。
4. 在下方的 **Run** 窗口中即可看到各个 Variant 的 SHA1 值。

*注意：如果找不到 Tasks 列表，请在设置中取消勾选 "Do not build Gradle task list during Gradle sync" 并重新同步。*

### 3.3 方法三：使用命令行 (keytool)

#### 3.2.1 调试版 (Debug)
默认的调试版签名文件位于用户目录下：

```powershell
# Windows
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android

# Linux/macOS
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### 3.2.2 发布版 (Release)
如果你已经创建了生产环境的签名文件 (`.jks` 或 `.keystore`)：

1. 打开终端。
2. 输入以下命令（替换括号中的内容）：
   ```bash
   keytool -list -v -keystore [签名文件路径] -alias [别名]
   ```
3. 按提示输入签名文件的密码。

### 3.4 方法四：从 Google Play Console 获取 (适用于已上架应用)

如果你的应用使用了 **Google Play App Signing**：

1. 登录 [Google Play Console](https://play.google.com/console/)。
2. 选择你的应用。
3. 进入 **设置 (Setup)** -> **应用完整性 (App Integrity)**。
4. 在 **应用签名 (App signing)** 选项卡下，你可以直接复制 **应用签名密钥证书 (App signing key certificate)** 的 SHA-1 指纹。

## 4. 注意事项

- **百度地图依赖**：百度地图官方主要通过下载 SDK 压缩包手动导入 JAR/SO 文件。如果你希望通过 Maven 引入，请关注百度官方是否发布了公开的 Maven 仓库地址。
- **隐私合规**：高德和百度地图在中国境内使用时，必须在初始化前调用合规隐私接口（本项目会在合适的时机处理，但请确保了解相关政策）。
- **混淆配置**：如果开启了代码混淆，请确保已按照 `app/proguard-rules.pro` 中的指引保留了地图 SDK 相关类。
