# 🛡️ Secure Guardian Pro - Mobile App

## 📱 Advanced Anti-Theft Android Application

### 🚀 Features

#### 🔑 PIN Generator
- **Secure 6-digit PIN generation** for web dashboard access
- **5-minute expiration** for enhanced security
- **One-time use** PINs to prevent replay attacks
- **Automatic clipboard copy** for easy use
- **Real-time countdown timer**

#### 📍 Live Location Tracking
- **GPS and Network location** tracking
- **Real-time location updates** every 10 seconds
- **Theft detection** based on movement patterns
- **Location accuracy monitoring**
- **Background service** for continuous protection

#### 👁️ Intruder Detection
- **Failed PIN attempt monitoring**
- **Automatic photo capture** on suspicious activity
- **Silent alarm activation**
- **Attempt history logging**
- **Real-time notifications**

#### ⚙️ Advanced Settings
- **Granular security controls**
- **Emergency mode** activation
- **Server URL configuration**
- **Feature toggles**
- **Settings backup/restore**

### 🏗️ Architecture

#### 📁 Project Structure
```
app/src/main/java/com/antitheft/pro/
├── activities/
│   ├── MainActivity.java           # Main dashboard
│   ├── PinGeneratorActivity.java   # PIN generation
│   ├── TrackingActivity.java       # Location tracking
│   ├── IntrudersActivity.java      # Intruder detection
│   └── SettingsActivity.java       # App settings
├── services/
│   └── LocationTrackingService.java # Background location service
├── utils/
│   ├── SecurityManager.java        # Security features manager
│   └── PermissionManager.java      # Permission handling
├── LoginActivity.java              # Authentication
└── SettingsActivity.java           # Legacy settings
```

#### 🔧 Key Components

##### MainActivity
- **Central dashboard** with security status
- **Feature access buttons** with modern UI
- **Permission management**
- **Security initialization**

##### PinGeneratorActivity
- **Secure PIN generation** using SecureRandom
- **Expiration timer** with visual countdown
- **Clipboard integration**
- **Usage instructions**

##### LocationTrackingService
- **Foreground service** for continuous tracking
- **GPS and Network providers**
- **Theft detection algorithms**
- **Notification management**

##### SecurityManager
- **Centralized security control**
- **Emergency mode management**
- **Feature state tracking**
- **Security status reporting**

### 🔒 Security Features

#### 🛡️ Multi-Layer Protection
1. **Location Tracking** - Continuous GPS monitoring
2. **Intruder Detection** - Failed attempt monitoring
3. **Camera Monitoring** - Automatic photo capture
4. **Emergency Mode** - All features activated

#### 🔐 PIN Security
- **SecureRandom generation** for cryptographic strength
- **Time-based expiration** (5 minutes)
- **Single-use tokens** prevent replay attacks
- **Secure storage** in SharedPreferences

#### 📱 Device Protection
- **Background services** for continuous monitoring
- **Boot receiver** for auto-start
- **Battery optimization** bypass
- **System alert window** for emergency alerts

### 🚀 Installation & Setup

#### 📋 Prerequisites
- **Android Studio** 4.0 or higher
- **Android SDK** API level 21+ (Android 5.0)
- **Java 8** or higher
- **Gradle** 7.0+

#### 🔧 Build Instructions
1. **Clone the repository**
   ```bash
   git clone https://github.com/hlias123/antitheft-pro.git
   cd antitheft-pro
   ```

2. **Open in Android Studio**
   - Import the project
   - Sync Gradle files
   - Wait for indexing

3. **Configure dependencies**
   - All dependencies are in `app/build.gradle`
   - Sync project with Gradle files

4. **Build and run**
   - Connect Android device or start emulator
   - Click "Run" or use `Ctrl+R`

#### 📱 APK Generation
```bash
./gradlew assembleRelease
```
APK will be generated in `app/build/outputs/apk/release/`

### 🔧 Configuration

#### 🌐 Server Integration
The app connects to the web dashboard server:
- **Default URL**: `http://localhost:8080`
- **Configurable** in Settings
- **PIN validation** endpoint: `/validate-pin`
- **Location updates** endpoint: `/update-location`

#### 🔑 PIN Integration
Generated PINs work with the web dashboard:
1. **Generate PIN** in mobile app
2. **Copy to clipboard**
3. **Open web dashboard**
4. **Enter email and PIN**
5. **Access granted** for 5 minutes

### 📊 Permissions Required

#### 🔒 Critical Permissions
- `ACCESS_FINE_LOCATION` - GPS tracking
- `ACCESS_COARSE_LOCATION` - Network location
- `ACCESS_BACKGROUND_LOCATION` - Background tracking
- `CAMERA` - Intruder photo capture
- `FOREGROUND_SERVICE` - Background services

#### 📱 Optional Permissions
- `WRITE_EXTERNAL_STORAGE` - Photo storage
- `SYSTEM_ALERT_WINDOW` - Emergency alerts
- `USE_BIOMETRIC` - Fingerprint authentication
- `RECEIVE_BOOT_COMPLETED` - Auto-start

### 🎨 UI/UX Design

#### 🌙 Dark Theme
- **Modern dark interface** for better visibility
- **Gradient backgrounds** for visual appeal
- **Color-coded status** indicators
- **Rounded corners** and shadows

#### 📱 Responsive Design
- **Programmatic layouts** for flexibility
- **Adaptive sizing** for different screens
- **Touch-friendly** button sizes
- **Accessibility** considerations

### 🔄 Integration with Web Dashboard

#### 🌐 Seamless Connection
1. **Mobile app generates PIN**
2. **User opens web dashboard**
3. **Enters email + PIN**
4. **Web validates with mobile data**
5. **Access granted to tracking interface**

#### 📡 Real-time Sync
- **Location updates** sent to server
- **Security events** logged
- **Status synchronization**
- **Emergency alerts** propagated

### 🚨 Emergency Features

#### 🆘 Emergency Mode
When activated:
- **All security features** enabled
- **Continuous location** tracking
- **Intruder detection** active
- **Camera monitoring** on
- **Silent operation** mode

#### 🚨 Theft Detection
- **Movement analysis** algorithms
- **Unauthorized location** changes
- **Automatic alerts** generation
- **Evidence collection** (photos, location)

### 📈 Performance Optimization

#### 🔋 Battery Efficiency
- **Optimized location** update intervals
- **Smart service** management
- **Background task** optimization
- **Power-aware** algorithms

#### 💾 Memory Management
- **Efficient data** structures
- **Proper lifecycle** management
- **Memory leak** prevention
- **Resource cleanup**

### 🧪 Testing

#### 🔍 Test Coverage
- **Unit tests** for core logic
- **Integration tests** for services
- **UI tests** for activities
- **Security tests** for PIN generation

#### 📱 Device Testing
- **Multiple Android** versions (5.0+)
- **Different screen** sizes
- **Various hardware** configurations
- **Permission scenarios**

### 🚀 Deployment

#### 📦 Release Build
```bash
./gradlew assembleRelease
./gradlew bundleRelease  # For Play Store
```

#### 🏪 Play Store Preparation
- **Signed APK/AAB**
- **Privacy policy** compliance
- **Permission justification**
- **Security review**

### 🔮 Future Enhancements

#### 🎯 Planned Features
- **Biometric authentication**
- **Cloud backup** integration
- **Multi-device** synchronization
- **Advanced analytics**
- **Machine learning** threat detection

#### 🌐 Platform Expansion
- **iOS version** development
- **Web app** companion
- **Desktop client**
- **Smart watch** integration

---

## 📞 Support

For technical support or feature requests:
- **GitHub Issues**: [Report bugs](https://github.com/hlias123/antitheft-pro/issues)
- **Documentation**: [Wiki](https://github.com/hlias123/antitheft-pro/wiki)
- **Updates**: [Releases](https://github.com/hlias123/antitheft-pro/releases)

---

**🛡️ Secure Guardian Pro - Your device's ultimate protection**