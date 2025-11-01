const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const nodemailer = require('nodemailer');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

const PORT = 4000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static('public'));

// Real-time data storage
const devices = new Map();
const users = new Map();
const sessions = new Map();

// Email configuration
let emailTransporter = null;

async function initializeEmail() {
    try {
        emailTransporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
                user: 'hliahlias1984@gmail.com',
                pass: 'your-app-password-here' // Replace with real app password
            }
        });
        
        await emailTransporter.verify();
        console.log('✅ Email service ready');
        return true;
    } catch (error) {
        console.log('❌ Email failed, using test mode');
        
        const testAccount = await nodemailer.createTestAccount();
        emailTransporter = nodemailer.createTransport({
            host: 'smtp.ethereal.email',
            port: 587,
            secure: false,
            auth: {
                user: testAccount.user,
                pass: testAccount.pass
            }
        });
        return false;
    }
}

// Device registration and tracking
class DeviceTracker {
    constructor(deviceId, userId) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.isActive = true;
        this.location = { lat: 37.9755, lng: 23.7348 }; // Default Athens
        this.battery = 100;
        this.lastSeen = new Date();
        this.isMoving = false;
        this.speed = 0;
        this.stepCount = 0;
        this.totalDistance = 0;
        this.locationHistory = [];
        this.alerts = [];
        this.cameraActive = false;
        this.isLocked = false;
        this.alarmActive = false;
        
        // Start real-time simulation
        this.startTracking();
    }
    
    startTracking() {
        // Simulate real device behavior
        setInterval(() => {
            this.updateLocation();
            this.updateBattery();
            this.checkAlerts();
            this.broadcastUpdate();
        }, 2000); // Update every 2 seconds
    }
    
    updateLocation() {
        // Simulate realistic movement
        const movementChance = Math.random();
        
        if (movementChance > 0.4) { // 60% chance of movement
            const moveDistance = Math.random() * 0.0002; // Realistic steps
            const direction = Math.random() * 2 * Math.PI;
            
            const oldLat = this.location.lat;
            const oldLng = this.location.lng;
            
            this.location.lat += Math.cos(direction) * moveDistance;
            this.location.lng += Math.sin(direction) * moveDistance;
            
            // Calculate distance and speed
            const distance = this.calculateDistance(oldLat, oldLng, this.location.lat, this.location.lng);
            this.totalDistance += distance;
            this.speed = Math.floor((distance / 1000) * 1800); // km/h (2 second intervals)
            this.stepCount += Math.floor(distance / 0.7); // Average step length
            this.isMoving = true;
            
            // Add to history
            this.locationHistory.push({
                lat: this.location.lat,
                lng: this.location.lng,
                timestamp: new Date(),
                speed: this.speed
            });
            
            // Keep only last 100 locations
            if (this.locationHistory.length > 100) {
                this.locationHistory.shift();
            }
        } else {
            this.speed = 0;
            this.isMoving = false;
        }
        
        this.lastSeen = new Date();
    }
    
    updateBattery() {
        if (this.battery > 0) {
            let drainRate = 0.05; // Base drain per update
            if (this.isMoving) drainRate += 0.02;
            if (this.cameraActive) drainRate += 0.1;
            if (this.alarmActive) drainRate += 0.2;
            
            this.battery -= drainRate;
            if (this.battery < 0) this.battery = 0;
        }
    }
    
    checkAlerts() {
        // Low battery alert
        if (this.battery < 20 && this.battery > 19.5) {
            this.addAlert('🔋 Low battery warning', 'critical');
        }
        
        // High speed alert
        if (this.speed > 30) {
            this.addAlert(`🚗 High speed detected: ${this.speed} km/h`, 'warning');
        }
        
        // Stationary alert (if not moving for 5 minutes)
        const timeSinceMovement = Date.now() - this.lastSeen.getTime();
        if (!this.isMoving && timeSinceMovement > 300000) { // 5 minutes
            this.addAlert('⏱️ Device stationary for 5+ minutes', 'info');
        }
    }
    
    addAlert(message, type = 'info') {
        const alert = {
            id: Date.now(),
            message,
            type,
            timestamp: new Date()
        };
        
        this.alerts.unshift(alert);
        
        // Keep only last 50 alerts
        if (this.alerts.length > 50) {
            this.alerts.pop();
        }
        
        // Broadcast alert immediately
        io.to(this.userId).emit('alert', alert);
    }
    
    calculateDistance(lat1, lng1, lat2, lng2) {
        const R = 6371e3;
        const φ1 = lat1 * Math.PI/180;
        const φ2 = lat2 * Math.PI/180;
        const Δφ = (lat2-lat1) * Math.PI/180;
        const Δλ = (lng2-lng1) * Math.PI/180;

        const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
                  Math.cos(φ1) * Math.cos(φ2) *
                  Math.sin(Δλ/2) * Math.sin(Δλ/2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return R * c;
    }
    
    broadcastUpdate() {
        const update = {
            deviceId: this.deviceId,
            location: this.location,
            battery: Math.floor(this.battery),
            isMoving: this.isMoving,
            speed: this.speed,
            stepCount: this.stepCount,
            totalDistance: Math.floor(this.totalDistance),
            lastSeen: this.lastSeen,
            isLocked: this.isLocked,
            alarmActive: this.alarmActive,
            cameraActive: this.cameraActive
        };
        
        io.to(this.userId).emit('deviceUpdate', update);
    }
    
    // Device control methods
    lockDevice() {
        this.isLocked = true;
        this.addAlert('🔒 Device locked remotely', 'success');
        return { success: true, message: 'Device locked successfully' };
    }
    
    activateAlarm() {
        this.alarmActive = true;
        this.addAlert('🚨 Alarm activated - 120dB siren', 'warning');
        
        // Auto-deactivate after 30 seconds
        setTimeout(() => {
            this.alarmActive = false;
            this.addAlert('🔇 Alarm deactivated', 'info');
        }, 30000);
        
        return { success: true, message: 'Alarm activated' };
    }
    
    activateCamera() {
        this.cameraActive = !this.cameraActive;
        const status = this.cameraActive ? 'activated' : 'deactivated';
        this.addAlert(`📹 Camera ${status}`, 'info');
        return { success: true, message: `Camera ${status}` };
    }
    
    capturePhoto() {
        if (this.cameraActive) {
            this.addAlert('📸 Photo captured from front camera', 'success');
            return { success: true, message: 'Photo captured', photoUrl: '/api/photo/' + Date.now() };
        }
        return { success: false, message: 'Camera not active' };
    }
    
    wipeData() {
        this.addAlert('🗑️ Remote data wipe initiated', 'critical');
        // Simulate data wipe process
        setTimeout(() => {
            this.addAlert('✅ Data wipe completed', 'success');
        }, 5000);
        return { success: true, message: 'Data wipe initiated' };
    }
}

// Routes
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'real-app.html'));
});

// Email validation functions
function isValidEmail(email) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
}

function isValidEmailDomain(email) {
    const domain = email.split('@')[1];
    if (!domain) return false;
    
    // List of valid domains and patterns
    const validDomains = [
        'gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com', 
        'icloud.com', 'aol.com', 'protonmail.com', 'zoho.com',
        'live.com', 'msn.com', 'yandex.com', 'mail.com',
        'edu', 'gov', 'org', 'net', 'co.uk', 'de', 'fr', 'it'
    ];
    
    const domainLower = domain.toLowerCase();
    
    // Check exact match or ends with valid domain
    return validDomains.some(validDomain => 
        domainLower === validDomain || 
        domainLower.endsWith('.' + validDomain) ||
        (domainLower.includes('.') && domainLower.length > 4)
    );
}

function containsSuspiciousPatterns(email) {
    const suspiciousPatterns = [
        /[;,\s]/, // Contains semicolon, comma, or spaces
        /\.{2,}/, // Contains multiple consecutive dots
        /@.*@/, // Contains multiple @ symbols
        /^[.-]/, // Starts with dot or dash
        /[.-]$/, // Ends with dot or dash
        /[^a-zA-Z0-9._%+-@]/ // Contains invalid characters
    ];
    
    return suspiciousPatterns.some(pattern => pattern.test(email));
}

function isReasonableEmailUsername(email) {
    const username = email.split('@')[0];
    if (!username) return false;
    
    // Check username length (reasonable limits)
    if (username.length < 3 || username.length > 30) return false;
    
    // Check for excessive repetition (like "aaaaaaa" or "123123123")
    if (/(.)\1{4,}/.test(username)) return false; // 5+ same characters in a row
    if (/(.{2,})\1{2,}/.test(username)) return false; // 3+ repetitions of same pattern
    
    // Check for random-looking strings (too many consonants without vowels)
    const consonantRatio = (username.match(/[bcdfghjklmnpqrstvwxyz]/gi) || []).length / username.length;
    if (consonantRatio > 0.8) return false; // More than 80% consonants
    
    // Check for keyboard mashing patterns
    const keyboardPatterns = [
        /qwerty/i, /asdf/i, /zxcv/i, /hjkl/i, /uiop/i,
        /123456/i, /abcdef/i, /qazwsx/i, /plmokn/i,
        /fgh/i, /jkl/i, /vbn/i, /tyu/i, /dfg/i, /cvb/i
    ];
    if (keyboardPatterns.some(pattern => pattern.test(username))) return false;
    
    // Check for too many numbers (more than 50% of username)
    const numberRatio = (username.match(/[0-9]/g) || []).length / username.length;
    if (numberRatio > 0.5) return false;
    
    return true;
}

// Authentication
app.post('/api/auth/register', async (req, res) => {
    try {
        let { email, password, name } = req.body;
        
        // Basic validation
        if (!email || !password || !name) {
            return res.status(400).json({ error: 'All fields are required' });
        }

        // Clean and validate email
        email = email.trim().toLowerCase();
        name = name.trim();

        // Email format validation
        if (!isValidEmail(email)) {
            return res.status(400).json({ error: 'Invalid email format' });
        }

        // Domain validation
        if (!isValidEmailDomain(email)) {
            return res.status(400).json({ error: 'Invalid email domain. Please use a valid email provider.' });
        }

        // Check for suspicious patterns
        if (containsSuspiciousPatterns(email)) {
            return res.status(400).json({ error: 'Email contains invalid characters or format' });
        }

        // Check for reasonable username
        if (!isReasonableEmailUsername(email)) {
            return res.status(400).json({ error: 'Email username appears to be fake or suspicious. Please use a real email address.' });
        }

        // Name validation
        if (name.length < 2 || name.length > 50) {
            return res.status(400).json({ error: 'Name must be between 2 and 50 characters' });
        }

        // Password validation
        if (password.length < 6) {
            return res.status(400).json({ error: 'Password must be at least 6 characters long' });
        }

        // Check if email already exists
        const existingUser = Array.from(users.values()).find(user => user.email === email);
        if (existingUser && existingUser.verified) {
            return res.status(400).json({ error: 'Email already registered. Please use a different email or try logging in.' });
        }
        
        // Generate verification code
        const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();
        const userId = 'user_' + Date.now();
        
        // Store user temporarily
        users.set(userId, {
            email,
            password,
            name: name || 'User',
            verified: false,
            verificationCode,
            createdAt: new Date()
        });
        
        // Send verification email
        const emailHtml = `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
            <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                <h1 style="margin: 0;">🛡️ Secure Guardian</h1>
                <p style="margin: 10px 0 0 0;">Anti-Theft Protection System</p>
            </div>
            <div style="background: white; padding: 30px; border: 1px solid #e2e8f0; border-radius: 0 0 10px 10px; color: #333;">
                <h2>Email Verification</h2>
                <p>Hello ${name || 'User'},</p>
                <p>Your verification code is:</p>
                <div style="background: #f7fafc; border: 2px dashed #667eea; padding: 20px; text-align: center; border-radius: 10px; margin: 20px 0;">
                    <div style="font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px;">${verificationCode}</div>
                </div>
                <p>This code expires in 10 minutes.</p>
            </div>
        </div>`;
        
        const info = await emailTransporter.sendMail({
            from: '"Secure Guardian" <hliahlias1984@gmail.com>',
            to: email,
            subject: 'Verification Code - Secure Guardian',
            html: emailHtml
        });
        
        const previewUrl = nodemailer.getTestMessageUrl(info);
        
        res.json({
            success: true,
            message: 'Verification code sent',
            userId,
            previewUrl,
            testCode: verificationCode // For testing
        });
        
    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({ error: 'Registration failed' });
    }
});

app.post('/api/auth/verify', (req, res) => {
    try {
        const { userId, code } = req.body;
        
        const user = users.get(userId);
        if (!user) {
            return res.status(400).json({ error: 'Invalid user' });
        }
        
        if (user.verificationCode !== code) {
            return res.status(400).json({ error: 'Invalid verification code' });
        }
        
        // Verify user
        user.verified = true;
        user.sessionToken = 'token_' + Date.now();
        users.set(userId, user);
        
        // Create device tracker
        const deviceId = 'device_' + Date.now();
        const deviceTracker = new DeviceTracker(deviceId, userId);
        devices.set(deviceId, deviceTracker);
        
        // Store session
        sessions.set(user.sessionToken, { userId, deviceId });
        
        res.json({
            success: true,
            message: 'Verification successful',
            token: user.sessionToken,
            deviceId,
            user: {
                id: userId,
                email: user.email,
                name: user.name
            }
        });
        
    } catch (error) {
        console.error('Verification error:', error);
        res.status(500).json({ error: 'Verification failed' });
    }
});

// Device control endpoints
app.post('/api/device/:action', (req, res) => {
    try {
        const { action } = req.params;
        const { token } = req.body;
        
        const session = sessions.get(token);
        if (!session) {
            return res.status(401).json({ error: 'Unauthorized' });
        }
        
        const device = devices.get(session.deviceId);
        if (!device) {
            return res.status(404).json({ error: 'Device not found' });
        }
        
        let result;
        switch (action) {
            case 'lock':
                result = device.lockDevice();
                break;
            case 'alarm':
                result = device.activateAlarm();
                break;
            case 'camera':
                result = device.activateCamera();
                break;
            case 'photo':
                result = device.capturePhoto();
                break;
            case 'wipe':
                result = device.wipeData();
                break;
            default:
                return res.status(400).json({ error: 'Invalid action' });
        }
        
        res.json(result);
        
    } catch (error) {
        console.error('Device control error:', error);
        res.status(500).json({ error: 'Control failed' });
    }
});

// Get device status
app.get('/api/device/status', (req, res) => {
    try {
        const token = req.headers.authorization?.replace('Bearer ', '');
        
        const session = sessions.get(token);
        if (!session) {
            return res.status(401).json({ error: 'Unauthorized' });
        }
        
        const device = devices.get(session.deviceId);
        if (!device) {
            return res.status(404).json({ error: 'Device not found' });
        }
        
        res.json({
            deviceId: device.deviceId,
            location: device.location,
            battery: Math.floor(device.battery),
            isMoving: device.isMoving,
            speed: device.speed,
            stepCount: device.stepCount,
            totalDistance: Math.floor(device.totalDistance),
            lastSeen: device.lastSeen,
            isLocked: device.isLocked,
            alarmActive: device.alarmActive,
            cameraActive: device.cameraActive,
            locationHistory: device.locationHistory.slice(-20), // Last 20 locations
            alerts: device.alerts.slice(0, 10) // Last 10 alerts
        });
        
    } catch (error) {
        console.error('Status error:', error);
        res.status(500).json({ error: 'Failed to get status' });
    }
});

// WebSocket connection handling
io.on('connection', (socket) => {
    console.log('Client connected:', socket.id);
    
    socket.on('authenticate', (token) => {
        const session = sessions.get(token);
        if (session) {
            socket.join(session.userId);
            socket.emit('authenticated', { success: true });
            console.log('Client authenticated:', session.userId);
            
            // Send connection quality info
            socket.emit('connectionInfo', {
                quality: 'excellent',
                latency: Math.floor(Math.random() * 50) + 10, // 10-60ms
                signalStrength: 'strong'
            });
        } else {
            socket.emit('authenticated', { success: false });
        }
    });
    
    // Simulate connection issues for testing
    socket.on('simulateDisconnect', () => {
        console.log('Simulating disconnect for testing');
        socket.disconnect(true);
    });
    
    socket.on('disconnect', (reason) => {
        console.log('Client disconnected:', socket.id, 'Reason:', reason);
    });
    
    // Ping-pong for connection monitoring
    socket.on('ping', (callback) => {
        callback();
    });
});

// Start server
server.listen(PORT, async () => {
    console.log(`🚀 Real Anti-Theft System running on port ${PORT}`);
    console.log(`🌐 Open: http://localhost:${PORT}`);
    
    await initializeEmail();
    
    console.log('✅ System ready for real-time tracking');
});