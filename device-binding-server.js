const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const path = require('path');
const crypto = require('crypto');
const bcrypt = require('bcrypt');
const nodemailer = require('nodemailer');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST"]
    }
});

const PORT = 8080;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static('public'));

// Device-Email binding storage
const deviceBindings = new Map(); // deviceId -> email
const registeredDevices = new Map(); // email -> deviceInfo
const users = new Map(); // userId -> userInfo
const sessions = new Map(); // token -> sessionInfo
const verificationCodes = new Map(); // userId -> codeInfo

// Email service
let emailTransporter = null;

async function initializeEmail() {
    try {
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
        console.log('✅ Email service ready');
        return true;
    } catch (error) {
        console.error('Email setup failed:', error);
        return false;
    }
}

// Device registration - Step 1: Register device with email
app.post('/api/device/register', async (req, res) => {
    try {
        const { deviceId, email, deviceName, deviceModel } = req.body;

        if (!deviceId || !email || !deviceName) {
            return res.status(400).json({ error: 'Device ID, email, and device name are required' });
        }

        // Validate email format
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(email)) {
            return res.status(400).json({ error: 'Invalid email format' });
        }

        // Check if device is already registered
        if (deviceBindings.has(deviceId)) {
            const existingEmail = deviceBindings.get(deviceId);
            if (existingEmail !== email) {
                return res.status(400).json({ 
                    error: `Device is already registered to ${existingEmail.replace(/(.{2}).*(@.*)/, '$1***$2')}` 
                });
            }
        }

        // Check if email already has a device
        if (registeredDevices.has(email)) {
            const existingDevice = registeredDevices.get(email);
            if (existingDevice.deviceId !== deviceId) {
                return res.status(400).json({ 
                    error: 'This email is already registered to another device' 
                });
            }
        }

        // Generate device binding code
        const bindingCode = Math.floor(100000 + Math.random() * 900000).toString();
        const bindingId = crypto.randomBytes(32).toString('hex');

        // Store device registration temporarily
        const deviceInfo = {
            deviceId,
            email,
            deviceName,
            deviceModel: deviceModel || 'Unknown',
            bindingCode,
            bindingId,
            registeredAt: new Date(),
            verified: false,
            expiresAt: new Date(Date.now() + 15 * 60 * 1000) // 15 minutes
        };

        registeredDevices.set(email, deviceInfo);

        // Determine email language based on domain or default to English
        const isArabic = email.includes('.sa') || email.includes('arab') || email.includes('emirates');
        const isGreek = email.includes('.gr') || email.includes('greece') || email.includes('greek');
        
        let emailTitle, emailGreeting, emailContent, securityNotice;
        
        if (isArabic) {
            emailTitle = "طلب ربط الجهاز";
            emailGreeting = "مرحباً";
            emailContent = "جهاز يطلب التسجيل بعنوان البريد الإلكتروني هذا:";
            securityNotice = "تنبيه أمني";
        } else if (isGreek) {
            emailTitle = "Αίτημα Σύνδεσης Συσκευής";
            emailGreeting = "Γεια σας";
            emailContent = "Μια συσκευή ζητά να εγγραφεί σε αυτή τη διεύθυνση email:";
            securityNotice = "Ειδοποίηση Ασφαλείας";
        } else {
            emailTitle = "Device Binding Request";
            emailGreeting = "Hello";
            emailContent = "A device is requesting to be registered to this email address:";
            securityNotice = "Security Notice";
        }

        // Send binding confirmation email
        const emailHtml = `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
            <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                <h1 style="margin: 0;">🛡️ Secure Guardian</h1>
                <p style="margin: 10px 0 0 0;">Device Registration Confirmation</p>
            </div>
            <div style="background: white; padding: 30px; border: 1px solid #e2e8f0; border-radius: 0 0 10px 10px; color: #333;">
                <h2>🔗 ${emailTitle}</h2>
                <p>${emailGreeting},</p>
                <p>${emailContent}</p>
                
                <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                    <strong>Device Details:</strong><br>
                    📱 Name: ${deviceName}<br>
                    🔧 Model: ${deviceModel || 'Unknown'}<br>
                    🆔 ID: ${deviceId}<br>
                    📅 Time: ${new Date().toLocaleString()}
                </div>
                
                <p>Your device binding code is:</p>
                <div style="background: #e3f2fd; border: 2px solid #2196f3; padding: 20px; text-align: center; border-radius: 10px; margin: 20px 0;">
                    <div style="font-size: 32px; font-weight: bold; color: #1976d2; letter-spacing: 5px; font-family: monospace;">${bindingCode}</div>
                </div>
                
                <div style="background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <strong>⚠️ ${securityNotice}:</strong>
                    <ul style="margin: 10px 0; padding-left: 20px;">
                        <li>Only enter this code if you initiated this device registration</li>
                        <li>This code expires in 15 minutes</li>
                        <li>Never share this code with anyone</li>
                        <li>If you didn't request this, ignore this email</li>
                    </ul>
                </div>
                
                <p style="font-size: 14px; color: #666; margin-top: 30px;">
                    Once verified, this device will be permanently linked to your email address and only you will be able to access the anti-theft controls.
                </p>
            </div>
        </div>`;

        const info = await emailTransporter.sendMail({
            from: '"Secure Guardian Device Registration" <device@secureguardian.com>',
            to: email,
            subject: 'Device Registration - Secure Guardian',
            html: emailHtml
        });

        const previewUrl = nodemailer.getTestMessageUrl(info);

        res.json({
            success: true,
            message: 'Device registration initiated. Check your email for the binding code.',
            bindingId: bindingId,
            previewUrl: previewUrl,
            expiresIn: '15 minutes'
        });

    } catch (error) {
        console.error('Device registration error:', error);
        res.status(500).json({ error: 'Device registration failed' });
    }
});

// Device verification - Step 2: Verify binding code
app.post('/api/device/verify-binding', (req, res) => {
    try {
        const { bindingId, bindingCode } = req.body;

        if (!bindingId || !bindingCode) {
            return res.status(400).json({ error: 'Binding ID and code are required' });
        }

        // Find device by binding ID
        let deviceInfo = null;
        let deviceEmail = null;

        for (const [email, device] of registeredDevices.entries()) {
            if (device.bindingId === bindingId) {
                deviceInfo = device;
                deviceEmail = email;
                break;
            }
        }

        if (!deviceInfo) {
            return res.status(400).json({ error: 'Invalid binding request' });
        }

        // Check expiration
        if (new Date() > deviceInfo.expiresAt) {
            registeredDevices.delete(deviceEmail);
            return res.status(400).json({ error: 'Binding code has expired. Please register again.' });
        }

        // Verify code
        if (deviceInfo.bindingCode !== bindingCode) {
            return res.status(400).json({ error: 'Invalid binding code' });
        }

        // Complete device binding
        deviceInfo.verified = true;
        deviceInfo.verifiedAt = new Date();
        
        // Create permanent binding
        deviceBindings.set(deviceInfo.deviceId, deviceEmail);
        registeredDevices.set(deviceEmail, deviceInfo);

        console.log(`✅ Device ${deviceInfo.deviceId} successfully bound to ${deviceEmail}`);

        res.json({
            success: true,
            message: 'Device successfully registered and bound to your email',
            deviceId: deviceInfo.deviceId,
            email: deviceEmail,
            deviceName: deviceInfo.deviceName
        });

    } catch (error) {
        console.error('Device verification error:', error);
        res.status(500).json({ error: 'Device verification failed' });
    }
});

// Web access - Step 3: User registration (must match device email)
app.post('/api/auth/register', async (req, res) => {
    try {
        const { email, password, name, deviceId } = req.body;

        if (!email || !password || !name) {
            return res.status(400).json({ error: 'All fields are required' });
        }

        // Check if device is registered and bound to this email
        const boundEmail = deviceBindings.get(deviceId);
        if (!boundEmail) {
            return res.status(400).json({ 
                error: 'Device not registered. Please register your device first using the mobile app.' 
            });
        }

        if (boundEmail !== email.toLowerCase().trim()) {
            return res.status(400).json({ 
                error: `Access denied. This device is registered to a different email address.` 
            });
        }

        // Verify device is properly bound
        const deviceInfo = registeredDevices.get(boundEmail);
        if (!deviceInfo || !deviceInfo.verified) {
            return res.status(400).json({ 
                error: 'Device registration not completed. Please complete device binding first.' 
            });
        }

        // Check if user already exists
        const existingUser = Array.from(users.values()).find(user => user.email === email);
        if (existingUser && existingUser.verified) {
            return res.status(400).json({ error: 'User already registered with this email' });
        }

        // Generate verification code
        const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();
        const userId = crypto.randomBytes(32).toString('hex');
        const hashedPassword = await bcrypt.hash(password, 12);

        // Store user
        users.set(userId, {
            email: email.toLowerCase().trim(),
            password: hashedPassword,
            name: name.trim(),
            deviceId: deviceId,
            verified: false,
            createdAt: new Date()
        });

        // Store verification code
        verificationCodes.set(userId, {
            code: verificationCode,
            expiresAt: new Date(Date.now() + 10 * 60 * 1000),
            attempts: 0
        });

        // Send verification email
        const emailHtml = `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
            <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                <h1 style="margin: 0;">🛡️ Secure Guardian</h1>
                <p style="margin: 10px 0 0 0;">Web Access Verification</p>
            </div>
            <div style="background: white; padding: 30px; border: 1px solid #e2e8f0; border-radius: 0 0 10px 10px; color: #333;">
                <h2>🔐 Web Access Request</h2>
                <p>Hello ${name},</p>
                <p>You are requesting web access for your registered device:</p>
                
                <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    📱 Device: ${deviceInfo.deviceName}<br>
                    🆔 Device ID: ${deviceId}<br>
                    📧 Registered Email: ${email}
                </div>
                
                <p>Your verification code is:</p>
                <div style="background: #e8f5e8; border: 2px solid #4caf50; padding: 20px; text-align: center; border-radius: 10px; margin: 20px 0;">
                    <div style="font-size: 32px; font-weight: bold; color: #2e7d32; letter-spacing: 5px; font-family: monospace;">${verificationCode}</div>
                </div>
                
                <div style="background: #e3f2fd; border: 1px solid #2196f3; padding: 15px; border-radius: 8px; margin: 20px 0;">
                    <strong>🔒 Security Confirmation:</strong><br>
                    This email confirms that the device and web access are both registered to the same email address, ensuring maximum security.
                </div>
            </div>
        </div>`;

        const info = await emailTransporter.sendMail({
            from: '"Secure Guardian Web Access" <web@secureguardian.com>',
            to: email,
            subject: 'Web Access Verification - Secure Guardian',
            html: emailHtml
        });

        const previewUrl = nodemailer.getTestMessageUrl(info);

        res.json({
            success: true,
            message: 'Verification code sent. Device and email binding confirmed.',
            userId: userId,
            previewUrl: previewUrl,
            deviceName: deviceInfo.deviceName
        });

    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({ error: 'Registration failed' });
    }
});

// Verify web access
app.post('/api/auth/verify', (req, res) => {
    try {
        const { userId, code } = req.body;

        const user = users.get(userId);
        const verification = verificationCodes.get(userId);

        if (!user || !verification) {
            return res.status(400).json({ error: 'Invalid verification request' });
        }

        if (new Date() > verification.expiresAt) {
            verificationCodes.delete(userId);
            return res.status(400).json({ error: 'Verification code expired' });
        }

        if (verification.code !== code) {
            verification.attempts++;
            if (verification.attempts >= 3) {
                verificationCodes.delete(userId);
                return res.status(400).json({ error: 'Too many failed attempts' });
            }
            return res.status(400).json({ error: 'Invalid verification code' });
        }

        // Complete verification
        user.verified = true;
        const sessionToken = crypto.randomBytes(64).toString('hex');

        sessions.set(sessionToken, {
            userId: userId,
            deviceId: user.deviceId,
            email: user.email,
            createdAt: new Date(),
            expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000)
        });

        verificationCodes.delete(userId);

        res.json({
            success: true,
            message: 'Web access granted. Device-email binding verified.',
            token: sessionToken,
            user: {
                id: userId,
                email: user.email,
                name: user.name,
                deviceId: user.deviceId
            }
        });

    } catch (error) {
        console.error('Verification error:', error);
        res.status(500).json({ error: 'Verification failed' });
    }
});

// Device detection API
app.post('/api/device/detect', (req, res) => {
    try {
        const { userAgent, fingerprint, email } = req.body;
        
        // Analyze user agent
        let deviceInfo = {
            type: 'Unknown',
            model: 'Unknown',
            os: 'Unknown',
            browser: 'Unknown',
            trustScore: 0
        };
        
        // Detect OS
        if (/Windows/.test(userAgent)) {
            deviceInfo.os = 'Windows';
            deviceInfo.type = 'Desktop';
            deviceInfo.trustScore += 20;
        } else if (/Mac/.test(userAgent)) {
            deviceInfo.os = 'macOS';
            deviceInfo.type = 'Desktop';
            deviceInfo.trustScore += 20;
        } else if (/iPhone/.test(userAgent)) {
            deviceInfo.os = 'iOS';
            deviceInfo.type = 'Mobile';
            deviceInfo.trustScore += 30;
            
            // Detect iPhone model
            if (/iPhone15,2/.test(userAgent)) deviceInfo.model = 'iPhone 14 Pro';
            else if (/iPhone15,3/.test(userAgent)) deviceInfo.model = 'iPhone 14 Pro Max';
            else if (/iPhone14,7/.test(userAgent)) deviceInfo.model = 'iPhone 14';
            else deviceInfo.model = 'iPhone';
        } else if (/Android/.test(userAgent)) {
            deviceInfo.os = 'Android';
            deviceInfo.type = /Mobile/.test(userAgent) ? 'Mobile' : 'Tablet';
            deviceInfo.trustScore += 25;
            
            // Detect Android model
            if (/SM-S24/.test(userAgent)) deviceInfo.model = 'Samsung Galaxy S24';
            else if (/SM-S23/.test(userAgent)) deviceInfo.model = 'Samsung Galaxy S23';
            else if (/Pixel 8/.test(userAgent)) deviceInfo.model = 'Google Pixel 8';
            else deviceInfo.model = 'Android Device';
        }
        
        // Detect Browser
        if (/Chrome/.test(userAgent)) {
            deviceInfo.browser = 'Chrome';
            deviceInfo.trustScore += 15;
        } else if (/Firefox/.test(userAgent)) {
            deviceInfo.browser = 'Firefox';
            deviceInfo.trustScore += 15;
        } else if (/Safari/.test(userAgent)) {
            deviceInfo.browser = 'Safari';
            deviceInfo.trustScore += 15;
        }
        
        // Check if device is already registered
        let isRegistered = false;
        let registeredEmail = null;
        
        for (const [registeredDeviceEmail, deviceData] of registeredDevices.entries()) {
            if (deviceData.deviceId && deviceData.deviceId.includes(fingerprint)) {
                isRegistered = true;
                registeredEmail = registeredDeviceEmail;
                break;
            }
        }
        
        // Generate suggested device ID
        const suggestedDeviceId = `device_${fingerprint}_${Date.now()}`;
        
        res.json({
            success: true,
            deviceInfo: deviceInfo,
            isRegistered: isRegistered,
            registeredEmail: registeredEmail ? registeredEmail.replace(/(.{2}).*(@.*)/, '$1***$2') : null,
            suggestedDeviceId: suggestedDeviceId,
            trustScore: deviceInfo.trustScore,
            recommendations: generateDeviceRecommendations(deviceInfo)
        });
        
    } catch (error) {
        console.error('Device detection error:', error);
        res.status(500).json({ error: 'Device detection failed' });
    }
});

function generateDeviceRecommendations(deviceInfo) {
    const recommendations = [];
    
    if (deviceInfo.trustScore < 50) {
        recommendations.push('⚠️ Low trust score - consider using a more secure device');
    }
    
    if (deviceInfo.type === 'Mobile') {
        recommendations.push('📱 Mobile device detected - ensure you have the mobile app installed');
    }
    
    if (deviceInfo.browser === 'Chrome' || deviceInfo.browser === 'Firefox') {
        recommendations.push('✅ Secure browser detected - good for web access');
    }
    
    if (deviceInfo.os === 'iOS' || deviceInfo.os === 'Android') {
        recommendations.push('📲 Mobile OS detected - perfect for anti-theft protection');
    }
    
    return recommendations;
}

// Get device bindings (for debugging)
app.get('/api/admin/bindings', (req, res) => {
    const bindings = [];
    for (const [deviceId, email] of deviceBindings.entries()) {
        const deviceInfo = registeredDevices.get(email);
        bindings.push({
            deviceId,
            email: email.replace(/(.{2}).*(@.*)/, '$1***$2'), // Mask email
            deviceName: deviceInfo?.deviceName || 'Unknown',
            verified: deviceInfo?.verified || false,
            registeredAt: deviceInfo?.registeredAt
        });
    }
    res.json({ bindings });
});

// Routes
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'app-pin-login.html'));
});

app.get('/device-register', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'device-register.html'));
});

// Start server
server.listen(PORT, async () => {
    console.log(`🛡️ Secure Guardian System running on port ${PORT}`);
    console.log(`📱 Main App (PIN Login): http://localhost:${PORT}`);
    console.log(`📱 Device Registration: http://localhost:${PORT}/device-register`);
    console.log(`🗺️ Advanced Tracking: http://localhost:${PORT}/advanced-tracking.html`);
    console.log(`🔒 Security: Multi-language PIN authentication`);
    
    await initializeEmail();
    
    console.log('✅ Device binding system ready');
});