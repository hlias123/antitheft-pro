const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = process.env.PORT || 8080;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static('public'));

// In-memory storage (في الإنتاج، استخدم قاعدة بيانات حقيقية)
let devices = new Map();
let activePins = new Map();
let locationHistory = new Map();
let securityAlerts = new Map();

// Device registration endpoint
app.post('/api/device/register', (req, res) => {
    try {
        const { device_id, email, device_name, android_version, app_version } = req.body;
        
        console.log('📱 Device registration:', { device_id, email, device_name });
        
        const deviceInfo = {
            device_id,
            email,
            device_name,
            android_version,
            app_version,
            registration_time: Date.now(),
            last_seen: Date.now(),
            status: 'online'
        };
        
        devices.set(device_id, deviceInfo);
        
        res.json({
            success: true,
            message: 'Device registered successfully',
            device_id: device_id
        });
        
    } catch (error) {
        console.error('❌ Device registration error:', error);
        res.status(500).json({
            success: false,
            message: 'Registration failed'
        });
    }
});

// Device status sync endpoint
app.post('/api/device/sync', (req, res) => {
    try {
        const { 
            device_id, 
            battery_level, 
            battery_status, 
            location, 
            security_status,
            tracking_active,
            intruder_detection,
            emergency_mode
        } = req.body;
        
        // Update device info
        if (devices.has(device_id)) {
            const device = devices.get(device_id);
            device.battery_level = battery_level;
            device.battery_status = battery_status;
            device.security_status = security_status;
            device.tracking_active = tracking_active;
            device.intruder_detection = intruder_detection;
            device.emergency_mode = emergency_mode;
            device.last_seen = Date.now();
            
            if (location) {
                device.location = location;
                
                // Store location history
                if (!locationHistory.has(device_id)) {
                    locationHistory.set(device_id, []);
                }
                const history = locationHistory.get(device_id);
                history.push({
                    ...location,
                    timestamp: Date.now()
                });
                
                // Keep only last 100 locations
                if (history.length > 100) {
                    history.shift();
                }
            }
            
            devices.set(device_id, device);
        }
        
        res.json({
            success: true,
            message: 'Device status synced'
        });
        
    } catch (error) {
        console.error('❌ Device sync error:', error);
        res.status(500).json({
            success: false,
            message: 'Sync failed'
        });
    }
});

// PIN validation endpoint
app.post('/api/pin/validate', (req, res) => {
    try {
        const { email, pin, device_id } = req.body;
        
        console.log('🔑 PIN validation request:', { email, pin, device_id });
        
        // Find device by email or device_id
        let targetDevice = null;
        for (let [id, device] of devices) {
            if (device.email === email || id === device_id) {
                targetDevice = device;
                break;
            }
        }
        
        if (!targetDevice) {
            return res.json({
                valid: false,
                message: 'Device not found. Please register your device first.'
            });
        }
        
        // Check if PIN exists and is valid
        const pinKey = `${email}_${pin}`;
        if (activePins.has(pinKey)) {
            const pinData = activePins.get(pinKey);
            
            // Check if PIN is still valid (not expired)
            if (Date.now() < pinData.expiry) {
                // PIN is valid, remove it (single use)
                activePins.delete(pinKey);
                
                // Update device last access
                targetDevice.last_web_access = Date.now();
                devices.set(targetDevice.device_id, targetDevice);
                
                res.json({
                    valid: true,
                    message: 'PIN validated successfully',
                    device_info: {
                        device_name: targetDevice.device_name,
                        battery_level: targetDevice.battery_level,
                        location: targetDevice.location,
                        security_status: targetDevice.security_status
                    }
                });
            } else {
                // PIN expired
                activePins.delete(pinKey);
                res.json({
                    valid: false,
                    message: 'PIN has expired. Please generate a new one.'
                });
            }
        } else {
            res.json({
                valid: false,
                message: 'Invalid PIN. Please check your mobile app.'
            });
        }
        
    } catch (error) {
        console.error('❌ PIN validation error:', error);
        res.status(500).json({
            valid: false,
            message: 'Validation failed'
        });
    }
});

// Store PIN from mobile app
app.post('/api/pin/store', (req, res) => {
    try {
        const { email, pin, device_id, expiry } = req.body;
        
        const pinKey = `${email}_${pin}`;
        activePins.set(pinKey, {
            email,
            pin,
            device_id,
            expiry: expiry || (Date.now() + 5 * 60 * 1000), // 5 minutes default
            created: Date.now()
        });
        
        console.log('🔑 PIN stored:', { email, pin, device_id });
        
        res.json({
            success: true,
            message: 'PIN stored successfully'
        });
        
    } catch (error) {
        console.error('❌ PIN storage error:', error);
        res.status(500).json({
            success: false,
            message: 'PIN storage failed'
        });
    }
});

// Location update endpoint
app.post('/api/location/update', (req, res) => {
    try {
        const { device_id, latitude, longitude, accuracy, battery_level } = req.body;
        
        // Update device location
        if (devices.has(device_id)) {
            const device = devices.get(device_id);
            device.location = {
                latitude,
                longitude,
                accuracy,
                timestamp: Date.now()
            };
            device.battery_level = battery_level;
            device.last_seen = Date.now();
            devices.set(device_id, device);
        }
        
        // Store in location history
        if (!locationHistory.has(device_id)) {
            locationHistory.set(device_id, []);
        }
        const history = locationHistory.get(device_id);
        history.push({
            latitude,
            longitude,
            accuracy,
            battery_level,
            timestamp: Date.now()
        });
        
        // Keep only last 100 locations
        if (history.length > 100) {
            history.shift();
        }
        
        res.json({
            success: true,
            message: 'Location updated'
        });
        
    } catch (error) {
        console.error('❌ Location update error:', error);
        res.status(500).json({
            success: false,
            message: 'Location update failed'
        });
    }
});

// Security alert endpoint
app.post('/api/security/alert', (req, res) => {
    try {
        const { device_id, alert_type, message, latitude, longitude } = req.body;
        
        const alertId = `${device_id}_${Date.now()}`;
        const alertData = {
            alert_id: alertId,
            device_id,
            alert_type,
            message,
            latitude,
            longitude,
            timestamp: Date.now(),
            status: 'active'
        };
        
        securityAlerts.set(alertId, alertData);
        
        console.log('🚨 Security alert:', alertData);
        
        res.json({
            success: true,
            message: 'Alert received',
            alert_id: alertId
        });
        
    } catch (error) {
        console.error('❌ Security alert error:', error);
        res.status(500).json({
            success: false,
            message: 'Alert failed'
        });
    }
});

// Get device info for web dashboard
app.get('/api/device/:email', (req, res) => {
    try {
        const email = req.params.email;
        
        // Find device by email
        let targetDevice = null;
        for (let [id, device] of devices) {
            if (device.email === email) {
                targetDevice = device;
                break;
            }
        }
        
        if (targetDevice) {
            // Get location history
            const history = locationHistory.get(targetDevice.device_id) || [];
            
            // Get recent alerts
            const alerts = [];
            for (let [id, alert] of securityAlerts) {
                if (alert.device_id === targetDevice.device_id) {
                    alerts.push(alert);
                }
            }
            
            res.json({
                success: true,
                device: targetDevice,
                location_history: history.slice(-10), // Last 10 locations
                recent_alerts: alerts.slice(-5) // Last 5 alerts
            });
        } else {
            res.status(404).json({
                success: false,
                message: 'Device not found'
            });
        }
        
    } catch (error) {
        console.error('❌ Get device info error:', error);
        res.status(500).json({
            success: false,
            message: 'Failed to get device info'
        });
    }
});

// Get all devices (admin endpoint)
app.get('/api/devices', (req, res) => {
    try {
        const deviceList = Array.from(devices.values());
        res.json({
            success: true,
            devices: deviceList,
            total: deviceList.length
        });
    } catch (error) {
        res.status(500).json({
            success: false,
            message: 'Failed to get devices'
        });
    }
});

// Clean expired PINs (run every minute)
setInterval(() => {
    const now = Date.now();
    for (let [key, pinData] of activePins) {
        if (now > pinData.expiry) {
            activePins.delete(key);
            console.log('🗑️ Expired PIN removed:', key);
        }
    }
}, 60000);

// Clean old alerts (run every hour)
setInterval(() => {
    const oneWeekAgo = Date.now() - (7 * 24 * 60 * 60 * 1000);
    for (let [id, alert] of securityAlerts) {
        if (alert.timestamp < oneWeekAgo) {
            securityAlerts.delete(id);
        }
    }
}, 3600000);

// Serve static files
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'app-pin-login.html'));
});

app.get('/advanced-tracking.html', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'advanced-tracking.html'));
});

// API status endpoint
app.get('/api/status', (req, res) => {
    res.json({
        status: 'online',
        timestamp: Date.now(),
        devices_count: devices.size,
        active_pins: activePins.size,
        total_alerts: securityAlerts.size,
        uptime: process.uptime()
    });
});

// Start server
app.listen(PORT, () => {
    console.log('🛡️ Secure Guardian Sync Server Started');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log(`🌐 Server running on: http://localhost:${PORT}`);
    console.log(`📱 Mobile API: http://localhost:${PORT}/api/`);
    console.log(`🔑 PIN Login: http://localhost:${PORT}/`);
    console.log(`🗺️ Tracking: http://localhost:${PORT}/advanced-tracking.html`);
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log('✅ Real-time sync between mobile app and web dashboard');
    console.log('✅ PIN validation system active');
    console.log('✅ Location tracking enabled');
    console.log('✅ Security alerts monitoring');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
});

module.exports = app;