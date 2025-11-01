# 📱 Secure Guardian Pro - APK Build Guide

## 🎯 Quick Answer: Where is the APK?

The APK needs to be **built** using Android Studio. Here's what's available:

### 📦 Available Files:
- **`SecureGuardian-APK-Package/`** - Complete source code package
- **`SecureGuardian-APK-Ready.zip`** - Compressed package ready for building
- **`SecureGuardian-Demo.apk`** - Demo placeholder file
- **`build-apk.bat`** - Automated build script for Windows

---

## 🚀 Method 1: Android Studio (Recommended)

### Step 1: Install Android Studio
```bash
# Download from: https://developer.android.com/studio
# Install with default settings
```

### Step 2: Open Project
1. Extract `SecureGuardian-APK-Ready.zip`
2. Open Android Studio
3. Choose "Open an existing project"
4. Select the extracted folder
5. Wait for Gradle sync

### Step 3: Build APK
1. Go to **Build** > **Generate Signed Bundle/APK**
2. Choose **APK**
3. Click **Next**
4. Choose **Create new keystore** (for first time)
5. Fill keystore details
6. Choose **debug** or **release**
7. Click **Finish**

### Step 4: Get APK
```
APK Location: app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔧 Method 2: Command Line

### Prerequisites
```bash
# Install Android SDK
# Set ANDROID_HOME environment variable
# Install Java 8+
```

### Build Commands
```bash
# Navigate to project
cd SecureGuardian-APK-Package

# Make gradlew executable (Linux/Mac)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

### APK Location
```
Debug APK: app/build/outputs/apk/debug/app-debug.apk
Release APK: app/build/outputs/apk/release/app-release.apk
```

---

## ⚡ Method 3: Automated Script (Windows)

### Run Build Script
```bash
# Double-click or run:
build-apk.bat
```

This script will:
1. Check Android SDK
2. Set up environment
3. Build APK automatically
4. Show APK location

---

## 📱 Installing APK

### Method 1: ADB (Developer)
```bash
# Enable USB Debugging on Android device
# Connect device to computer
adb install app-debug.apk
```

### Method 2: Direct Install
1. Transfer APK to Android device
2. Enable "Install from Unknown Sources"
3. Tap APK file to install

---

## 🛡️ App Features (Once Built)

### 🔑 PIN Generator
- Generate secure 6-digit PINs
- 5-minute expiration
- Web dashboard access

### 📍 Location Tracking
- Real-time GPS monitoring
- Background location service
- Theft detection algorithms

### 👁️ Intruder Detection
- Failed PIN attempt monitoring
- Automatic photo capture
- Silent alarm activation

### 🔋 Device Monitoring
- Battery level tracking
- Connection status
- Security status display

### ⚙️ Advanced Settings
- Feature toggles
- Server configuration
- Emergency mode

---

## 🔧 Troubleshooting

### Android Studio Issues
```bash
# Gradle sync failed
File > Invalidate Caches and Restart

# SDK not found
File > Project Structure > SDK Location
Set Android SDK path
```

### Command Line Issues
```bash
# Permission denied (Linux/Mac)
chmod +x gradlew

# ANDROID_HOME not set
export ANDROID_HOME=/path/to/android/sdk

# Java not found
# Install Java 8+ and set JAVA_HOME
```

### Build Errors
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleDebug

# Check dependencies
./gradlew dependencies
```

---

## 📊 APK Information

### Package Details
- **Package Name**: `com.antitheft.pro`
- **Version**: `1.0.0`
- **Min SDK**: `21` (Android 5.0)
- **Target SDK**: `34` (Android 14)

### Permissions Required
- `ACCESS_FINE_LOCATION` - GPS tracking
- `ACCESS_COARSE_LOCATION` - Network location
- `ACCESS_BACKGROUND_LOCATION` - Background tracking
- `CAMERA` - Photo capture
- `FOREGROUND_SERVICE` - Background services
- `INTERNET` - Server communication
- `ACCESS_NETWORK_STATE` - Network status

### APK Size (Estimated)
- **Debug APK**: ~15-20 MB
- **Release APK**: ~10-15 MB (optimized)

---

## 🌐 Integration with Web Dashboard

### Server Setup
```bash
# Start sync server
node sync-server.js
# Server runs on: http://localhost:8080
```

### Usage Flow
1. **Install APK** on Android device
2. **Register device** with email
3. **Generate PIN** in mobile app
4. **Open web dashboard**: http://localhost:8080
5. **Enter email + PIN** to access tracking

---

## 📞 Support

### Resources
- **GitHub**: https://github.com/hlias123/antitheft-pro
- **Issues**: https://github.com/hlias123/antitheft-pro/issues
- **Documentation**: `SYNC_SYSTEM_README.md`

### Common Questions

**Q: Why isn't there a ready APK file?**
A: APK files need to be built for security reasons. Each build is signed with unique keys.

**Q: Can I use the demo APK?**
A: The demo APK is just a placeholder. You need to build the actual APK using Android Studio.

**Q: Do I need Android Studio?**
A: Android Studio is recommended, but you can also use command line with Android SDK.

**Q: How long does building take?**
A: First build: 5-10 minutes. Subsequent builds: 1-2 minutes.

---

## ✅ Quick Checklist

- [ ] Android Studio installed
- [ ] Project opened and synced
- [ ] APK built successfully
- [ ] APK installed on device
- [ ] Server running (sync-server.js)
- [ ] Device registered with email
- [ ] PIN generated and tested
- [ ] Web dashboard accessible

---

**🛡️ Secure Guardian Pro - Your device's ultimate protection**

*Built with ❤️ for Android security*