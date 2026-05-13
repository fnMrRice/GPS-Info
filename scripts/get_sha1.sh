#!/bin/bash

# 获取 Android 签名 SHA1 的脚本 (Shell 版)

get_sha1() {
    local keystore_path=$1
    local alias=$2
    local password=$3

    if [ -f "$keystore_path" ]; then
        echo "正在读取密钥库: $keystore_path"
        keytool -list -v -keystore "$keystore_path" -alias "$alias" -storepass "$password" | grep "SHA1:"
    else
        echo "错误: 未找到密钥库 $keystore_path"
    fi
}

DEBUG_KEYSTORE="$HOME/.android/debug.keystore"

echo "--- Android SHA1 获取工具 ---"
echo "1. 获取调试版 (Debug) SHA1"
echo "2. 获取发布版 (Release) SHA1 (需要路径和别名)"
read -p "请选择 (1/2): " choice

case $choice in
    1)
        get_sha1 "$DEBUG_KEYSTORE" "androiddebugkey" "android"
        ;;
    2)
        read -p "请输入密钥库路径 (.jks 或 .keystore): " path
        read -p "请输入别名 (Alias): " alias
        read -p "请输入密码: " pass
        get_sha1 "$path" "$alias" "$pass"
        ;;
    *)
        echo "无效选择"
        ;;
esac

read -p "按回车键退出..."
