const fs = require('fs');
const path = require('path');

console.log('🛡️ Creating Secure Guardian APK Package...');

// Create APK info file
const apkInfo = {
    name: 'Secure Guardian Pro',
    package: 'com.antitheft.pro',
    version: '1.0.0',
    versionCode: 1,
    minSdkVersion: 21,
    targetSdkVersion: 34,
    features: [
        '🔑 PIN Generator for Web Access',
        '📍 Live GPS Tracking',
        '👁️ Intruder Detection',
        '🔋 Battery Monitoring',
        '🚨 Security Alerts',
        '⚙️ Advanced Settings',
        '🌐 Web Dashboard Sync'
    ],
    permissions: [
        'ACCESS_FINE_LOCATION',
        'ACCESS_COARSE_LOCATION',
        'ACCESS_BACKGROUND_LOCATION',
        'CAMERA',
        'FOREGROUND_SERVICE',
        'INTERNET',
        'ACCESS_NETWORK_STATE'
    ],
    buildDate: new Date().toISOString(),
    buildInstructions: {
        requirements: [
            'Android Studio 4.0+',
            'Android SDK API 21+',
            'Java 8+',
            'Gradle 7.0+'
        ],
        steps: [
            '1. Install Android Studio',
            '2. Open project in Android Studio',
            '3. Sync Gradle files',
            '4. Build > Generate Signed Bundle/APK',
            '5. Choose APK and build'
        ],
        alternativeMethod: [
            '1. Run: gradlew assembleDebug',
            '2. APK will be in: app/build/outputs/apk/debug/',
            '3. Install with: adb install app-debug.apk'
        ]
    }
};

// Create APK directory structure
const apkDir = 'SecureGuardian-APK-Package';
if (!fs.existsSync(apkDir)) {
    fs.mkdirSync(apkDir);
}

// Copy source files
const sourceFiles = [
    'app/src/main/java/com/antitheft/pro/MainActivity.java',
    'app/src/main/java/com/antitheft/pro/activities/PinGeneratorActivity.java',
    'app/src/main/java/com/antitheft/pro/activities/TrackingActivity.java',
    'app/src/main/java/com/antitheft/pro/activities/IntrudersActivity.java',
    'app/src/main/java/com/antitheft/pro/activities/SettingsActivity.java',
    'app/src/main/java/com/antitheft/pro/activities/DeviceRegistrationActivity.java',
    'app/src/main/java/com/antitheft/pro/services/LocationTrackingService.java',
    'app/src/main/java/com/antitheft/pro/api/ApiService.java',
    'app/src/main/java/com/antitheft/pro/utils/SecurityManager.java',
    'app/src/main/java/com/antitheft/pro/utils/PermissionManager.java',
    'app/src/main/AndroidManifest.xml',
    'app/build.gradle',
    'app/src/main/res/values/strings.xml',
    'app/src/main/res/values/colors.xml',
    'app/src/main/res/values/themes.xml'
];

console.log('📁 Creating APK package structure...');

// Create source directory
const srcDir = path.join(apkDir, 'source');
if (!fs.existsSync(srcDir)) {
    fs.mkdirSync(srcDir, { recursive: true });
}

// Copy files that exist
let copiedFiles = 0;
sourceFiles.forEach(file => {
    if (fs.existsSync(file)) {
        const destPath = path.join(srcDir, path.basename(file));
        try {
            fs.copyFileSync(file, destPath);
            copiedFiles++;
            console.log(`✅ Copied: ${file}`);
        } catch (err) {
            console.log(`⚠️ Could not copy: ${file}`);
        }
    }
});

// Write APK info
fs.writeFileSync(
    path.join(apkDir, 'APK-INFO.json'),
    JSON.stringify(apkInfo, null, 2)
);

// Create build instructions
const buildInstructions = `# 🛡️ Secure Guardian Pro - APK Build Instructions

## 📱 Quick Build (Recommended)

### Method 1: Android Studio
1. Install Android Studio from: https://developer.android.com/studio
2. Open this project folder in Android Studio
3. Wait for Gradle sync to complete
4. Go to Build > Generate Signed Bundle/APK
5. Choose APK and follow the wizard
6. APK will be generated in: app/build/outputs/apk/

### Method 2: Command Line
\`\`\`bash
# Navigate to project root
cd SecureGuardian-APK-Package

# Build debug APK
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk
\`\`\`

## 📋 Requirements
- Android Studio 4.0+
- Android SDK API 21+ (Android 5.0)
- Java 8 or higher
- Gradle 7.0+

## 🚀 Installation
\`\`\`bash
# Install on connected Android device
adb install app-debug.apk

# Or transfer APK to device and install manually
\`\`\`

## ✅ Features Included
${apkInfo.features.map(f => `- ${f}`).join('\n')}

## 🔒 Permissions Required
${apkInfo.permissions.map(p => `- ${p}`).join('\n')}

## 📞 Support
- GitHub: https://github.com/hlias123/antitheft-pro
- Issues: https://github.com/hlias123/antitheft-pro/issues

Built on: ${apkInfo.buildDate}
`;

fs.writeFileSync(
    path.join(apkDir, 'BUILD-INSTRUCTIONS.md'),
    buildInstructions
);

// Create a simple APK placeholder (for demonstration)
const apkPlaceholder = `# 📱 Secure Guardian Pro APK

This is a placeholder for the actual APK file.

## 🔧 To build the actual APK:

1. Follow instructions in BUILD-INSTRUCTIONS.md
2. Use Android Studio or Gradle command line
3. APK will be generated with all features included

## 📋 App Information:
- Package: ${apkInfo.package}
- Version: ${apkInfo.version}
- Min SDK: ${apkInfo.minSdkVersion}
- Target SDK: ${apkInfo.targetSdkVersion}

## 🛡️ Security Features:
${apkInfo.features.map(f => `${f}`).join('\n')}

Built: ${apkInfo.buildDate}
`;

fs.writeFileSync(
    path.join(apkDir, 'app-debug.apk.info'),
    apkPlaceholder
);

console.log('\n🎉 APK Package Created Successfully!');
console.log(`📁 Package Location: ${apkDir}/`);
console.log(`📋 Files Copied: ${copiedFiles}`);
console.log(`📖 Instructions: ${apkDir}/BUILD-INSTRUCTIONS.md`);
console.log('\n🚀 To build actual APK:');
console.log('1. Install Android Studio');
console.log('2. Open project in Android Studio');
console.log('3. Build > Generate Signed Bundle/APK');
console.log('\n✅ Ready for Android development!');