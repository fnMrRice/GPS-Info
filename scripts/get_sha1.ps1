# 获取 Android 签名 SHA1 的脚本 (PowerShell 版)

function Get-SHA1 {
    param (
        [string]$KeystorePath,
        [string]$Alias,
        [string]$Password = "android"
    )

    if (Test-Path $KeystorePath) {
        Write-Host "正在读取密钥库: $KeystorePath" -ForegroundColor Cyan
        keytool -list -v -keystore "$KeystorePath" -alias "$Alias" -storepass "$Password" | Select-String "SHA1:"
    } else {
        Write-Host "未找到密钥库: $KeystorePath" -ForegroundColor Red
    }
}

$debugKeystore = "$env:USERPROFILE\.android\debug.keystore"

Write-Host "--- Android SHA1 获取工具 ---" -ForegroundColor Green
Write-Host "1. 获取调试版 (Debug) SHA1"
Write-Host "2. 获取发布版 (Release) SHA1 (需要路径和别名)"
$choice = Read-Host "请选择 (1/2)"

if ($choice -eq "1") {
    Get-SHA1 -KeystorePath $debugKeystore -Alias "androiddebugkey" -Password "android"
} elseif ($choice -eq "2") {
    $path = Read-Host "请输入密钥库路径 (.jks 或 .keystore)"
    $alias = Read-Host "请输入别名 (Alias)"
    $pass = Read-Host "请输入密码 (回车默认为空，如果是 debug 请输入 android)"
    Get-SHA1 -KeystorePath $path -Alias $alias -Password $pass
} else {
    Write-Host "无效选择" -ForegroundColor Yellow
}

Read-Host "按回车键退出..."
