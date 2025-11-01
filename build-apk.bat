@echo off
echo 🛡️ Building Secure Guardian APK...
echo.

REM Set Android SDK path (adjust if needed)
set ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk
set PATH=%ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools;%PATH%

echo 📱 Checking Android SDK...
if not exist "%ANDROID_HOME%" (
    echo ❌ Android SDK not found at %ANDROID_HOME%
    echo Please install Android Studio or set correct ANDROID_HOME path
    pause
    exit /b 1
)

echo ✅ Android SDK found at %ANDROID_HOME%

REM Create local.properties for app
echo sdk.dir=%ANDROID_HOME:\=/% > app\local.properties

echo 🔧 Building APK using Gradle...
cd app
if exist gradlew.bat (
    gradlew.bat assembleDebug
) else (
    echo ❌ Gradle wrapper not found
    echo Copying from android directory...
    copy ..\android\gradlew.bat .
    copy ..\android\gradlew .
    xcopy /E /I ..\android\gradle gradle
    gradlew.bat assembleDebug
)

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ APK built successfully!
    echo 📱 APK Location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    if exist build\outputs\apk\debug\app-debug.apk (
        echo 📋 APK Details:
        dir build\outputs\apk\debug\app-debug.apk
        echo.
        echo 🚀 Ready to install on Android device!
        echo Use: adb install app-debug.apk
    )
) else (
    echo ❌ Build failed with error code %ERRORLEVEL%
)

pause