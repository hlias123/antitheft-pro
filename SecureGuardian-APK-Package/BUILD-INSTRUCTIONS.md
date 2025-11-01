# 🛡️ Secure Guardian Pro - APK Build Instructions

## 📱 Quick Build (Recommended)

### Method 1: Android Studio
1. Install Android Studio from: https://developer.android.com/studio
2. Open this project folder in Android Studio
3. Wait for Gradle sync to complete
4. Go to Build > Generate Signed Bundle/APK
5. Choose APK and follow the wizard
6. APK will be generated in: app/build/outputs/apk/

### Method 2: Command Line
```bash
# Navigate to project root
cd SecureGuardian-APK-Package

# Build debug APK
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk
```

## 📋 Requirements
- Android Studio 4.0+
- Android SDK API 21+ (Android 5.0)
- Java 8 or higher
- Gradle 7.0+

## 🚀 Installation
```bash
# Install on connected Android device
adb install app-debug.apk

# Or transfer APK to device and install manually
```

## ✅ Features Included
- 🔑 PIN Generator for Web Access
- 📍 Live GPS Tracking
- 👁️ Intruder Detection
- 🔋 Battery Monitoring
- 🚨 Security Alerts
- ⚙️ Advanced Settings
- 🌐 Web Dashboard Sync

## 🔒 Permissions Required
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- ACCESS_BACKGROUND_LOCATION
- CAMERA
- FOREGROUND_SERVICE
- INTERNET
- ACCESS_NETWORK_STATE

## 📞 Support
- GitHub: https://github.com/hlias123/antitheft-pro
- Issues: https://github.com/hlias123/antitheft-pro/issues

Built on: 2025-11-01T18:10:34.841Z
