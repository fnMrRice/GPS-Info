# GitHub Actions CI/CD 配置说明

## 工作流概览

| 工作流 | 触发条件 | 用途 |
|--------|----------|------|
| `ci.yml` | push/PR → master/main | 构建 Debug APK、运行单元测试、Lint 检查 |
| `release.yml` | push tag `v*` | 构建签名 Release APK、自动创建 GitHub Release |

## 配置 GitHub Secrets

在仓库 **Settings → Secrets and variables → Actions** 中添加以下 Secrets：

### 签名相关（Release 构建必需）

| Secret 名称 | 说明 |
|-------------|------|
| `RELEASE_KEYSTORE_BASE64` | 签名文件 `release.jks` 的 Base64 编码（见下方生成方法） |
| `RELEASE_STORE_PASSWORD` | Keystore 密码 |
| `RELEASE_KEY_ALIAS` | Key 别名 |
| `RELEASE_KEY_PASSWORD` | Key 密码 |

### API Keys（按需配置）

| Secret 名称 | 说明 |
|-------------|------|
| `GOOGLE_MAPS_KEY` | Google Maps API Key |
| `AMAP_KEY` | 高德地图 API Key |
| `BAIDU_MAPS_KEY` | 百度地图 API Key |

## 生成 Keystore Base64

将你的签名文件编码为 Base64，用于 GitHub Secret：

```bash
# Linux / macOS
base64 -i release.jks | tr -d '\n'

# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.jks"))
```

将输出的字符串完整复制到 `RELEASE_KEYSTORE_BASE64` Secret 中。

## 使用方式

### CI（持续集成）

自动运行，无需手动操作：
- 每次 push 到 master/main 分支
- 每次提交 Pull Request

### Release（发布新版本）

```bash
# 1. 更新 versionCode 和 versionName（在 app/build.gradle.kts）

# 2. 提交并推送
git add .
git commit -m "release: prepare v1.1.0"
git push

# 3. 打 tag 并推送，触发自动构建和发布
git tag v1.1.0
git push origin v1.1.0
```

构建成功后，GitHub 会自动创建一个 Release 页面，包含签名后的 APK 文件。
