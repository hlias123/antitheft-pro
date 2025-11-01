const express = require('express');
const http = require('http');
const socketIo = require('socket.io');
const cors = require('cors');
const path = require('path');
const crypto = require('crypto');
const bcrypt = require('bcrypt');
const rateLimit = require('express-rate-limit');
const helmet = require('helmet');
const nodemailer = require('nodemailer');

const app = express();
const server = http.createServer(app);
const io = socketIo(server, {
    cors: {
        origin: process.env.NODE_ENV === 'production' ? ['https://yourdomain.com'] : "*",
        methods: ["GET", "POST"]
    }
});

const PORT = process.env.PORT || 5000;

// Security middleware
app.use(helmet({
    contentSecurityPolicy: {
        directives: {
            defaultSrc: ["'self'"],
            styleSrc: ["'self'", "'unsafe-inline'", "https://unpkg.com"],
            scriptSrc: ["'self'", "https://unpkg.com"],
            imgSrc: ["'self'", "data:", "https:"],
            connectSrc: ["'self'", "ws:", "wss:"]
        }
    }
}));

// Rate limiting
const authLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 5, // 5 attempts per window
    message: { error: 'Too many authentication attempts. Please try again later.' },
    standardHeaders: true,
    legacyHeaders: false,
});

const generalLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, // 100 requests per window
    message: { error: 'Too many requests. Please try again later.' }
});

app.use('/api/auth', authLimiter);
app.use(generalLimiter);

// CORS with strict settings
app.use(cors({
    origin: process.env.NODE_ENV === 'production' ? ['https://yourdomain.com'] : true,
    credentials: true,
    optionsSuccessStatus: 200
}));

app.use(express.json({ limit: '10mb' }));
app.use(express.static('public'));

// Secure data storage with encryption
const users = new Map();
const sessions = new Map();
const devices = new Map();
const failedAttempts = new Map();
const verificationCodes = new Map();

// Encryption functions
const ENCRYPTION_KEY = process.env.ENCRYPTION_KEY || crypto.randomBytes(32);
const IV_LENGTH = 16;

function encrypt(text) {
    const iv = crypto.randomBytes(IV_LENGTH);
    const cipher = crypto.createCipher('aes-256-cbc', ENCRYPTION_KEY);
    let encrypted = cipher.update(text, 'utf8', 'hex');
    encrypted += cipher.final('hex');
    return iv.toString('hex') + ':' + encrypted;
}

function decrypt(text) {
    const textParts = text.split(':');
    const iv = Buffer.from(textParts.shift(), 'hex');
    const encryptedText = textParts.join(':');
    const decipher = crypto.createDecipher('aes-256-cbc', ENCRYPTION_KEY);
    let decrypted = decipher.update(encryptedText, 'hex', 'utf8');
    decrypted += decipher.final('utf8');
    return decrypted;
}

// Secure token generation
function generateSecureToken() {
    return crypto.randomBytes(64).toString('hex');
}

function generateSecureCode() {
    return crypto.randomInt(100000, 999999).toString();
}

// Email validation with enhanced security
function isValidEmail(email) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email) && email.length <= 254; // RFC 5321 limit
}

function isValidEmailDomain(email) {
    const domain = email.split('@')[1];
    if (!domain) return false;
    
    // Whitelist of trusted domains
    const trustedDomains = [
        'gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com', 
        'icloud.com', 'aol.com', 'protonmail.com', 'zoho.com',
        'live.com', 'msn.com', 'yandex.com', 'mail.com',
        'edu', 'gov', 'org'
    ];
    
    const domainLower = domain.toLowerCase();
    return trustedDomains.some(trusted => 
        domainLower === trusted || domainLower.endsWith('.' + trusted)
    );
}

function isReasonableEmailUsername(email) {
    const username = email.split('@')[0];
    if (!username || username.length < 3 || username.length > 30) return false;
    
    // Enhanced suspicious pattern detection
    const suspiciousPatterns = [
        /(.)\1{4,}/, // 5+ repeated characters
        /(.{2,})\1{2,}/, // 3+ repeated patterns
        /^[0-9]+$/, // Only numbers
        /qwerty|asdf|zxcv|hjkl|uiop|123456|abcdef/i, // Keyboard patterns
        /test|fake|temp|dummy|spam|bot|admin/i, // Common fake patterns
    ];
    
    if (suspiciousPatterns.some(pattern => pattern.test(username))) return false;
    
    // Check consonant/vowel ratio
    const consonants = (username.match(/[bcdfghjklmnpqrstvwxyz]/gi) || []).length;
    const vowels = (username.match(/[aeiou]/gi) || []).length;
    if (consonants > 0 && vowels === 0 && consonants > 5) return false;
    
    return true;
}

// Account lockout protection
function isAccountLocked(email) {
    const attempts = failedAttempts.get(email);
    if (!attempts) return false;
    
    const now = Date.now();
    const recentAttempts = attempts.filter(time => now - time < 15 * 60 * 1000); // 15 minutes
    
    if (recentAttempts.length >= 5) {
        return true;
    }
    
    // Clean old attempts
    failedAttempts.set(email, recentAttempts);
    return false;
}

function recordFailedAttempt(email) {
    const attempts = failedAttempts.get(email) || [];
    attempts.push(Date.now());
    failedAttempts.set(email, attempts);
}

function clearFailedAttempts(email) {
    failedAttempts.delete(email);
}

// Email service with security
let emailTransporter = null;

async function initializeSecureEmail() {
    try {
        emailTransporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
                user: process.env.EMAIL_USER || 'your-email@gmail.com',
                pass: process.env.EMAIL_PASS || 'your-app-password'
            },
            secure: true,
            requireTLS: true
        });
        
        await emailTransporter.verify();
        console.log('✅ Secure email service ready');
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

// Secure routes
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'secure-app.html'));
});

// Enhanced registration with security
app.post('/api/auth/register', async (req, res) => {
    try {
        let { email, password, name } = req.body;
        
        // Input validation
        if (!email || !password || !name) {
            return res.status(400).json({ error: 'All fields are required' });
        }

        // Sanitize inputs
        email = email.trim().toLowerCase();
        name = name.trim();

        // Check account lockout
        if (isAccountLocked(email)) {
            return res.status(429).json({ 
                error: 'Account temporarily locked due to multiple failed attempts. Please try again later.' 
            });
        }

        // Enhanced email validation
        if (!isValidEmail(email)) {
            recordFailedAttempt(email);
            return res.status(400).json({ error: 'Invalid email format' });
        }

        if (!isValidEmailDomain(email)) {
            recordFailedAttempt(email);
            return res.status(400).json({ error: 'Email domain not allowed. Please use a trusted email provider.' });
        }

        if (!isReasonableEmailUsername(email)) {
            recordFailedAttempt(email);
            return res.status(400).json({ error: 'Email appears to be fake or suspicious. Please use a real email address.' });
        }

        // Password strength validation
        if (password.length < 8) {
            return res.status(400).json({ error: 'Password must be at least 8 characters long' });
        }

        if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(password)) {
            return res.status(400).json({ error: 'Password must contain at least one uppercase letter, one lowercase letter, and one number' });
        }

        // Name validation
        if (name.length < 2 || name.length > 50 || !/^[a-zA-Z\s]+$/.test(name)) {
            return res.status(400).json({ error: 'Name must be 2-50 characters and contain only letters and spaces' });
        }

        // Check if email already exists
        const existingUser = Array.from(users.values()).find(user => 
            decrypt(user.email) === email && user.verified
        );
        if (existingUser) {
            recordFailedAttempt(email);
            return res.status(400).json({ error: 'Email already registered' });
        }

        // Generate secure verification code
        const verificationCode = generateSecureCode();
        const userId = generateSecureToken();
        const hashedPassword = await bcrypt.hash(password, 12);

        // Store user with encryption
        users.set(userId, {
            email: encrypt(email),
            password: hashedPassword,
            name: encrypt(name),
            verified: false,
            createdAt: new Date(),
            lastLogin: null,
            loginAttempts: 0
        });

        // Store verification code with expiration
        verificationCodes.set(userId, {
            code: verificationCode,
            expiresAt: new Date(Date.now() + 10 * 60 * 1000), // 10 minutes
            attempts: 0
        });

        // Send secure verification email
        const emailHtml = `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
            <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                <h1 style="margin: 0;">🛡️ Secure Guardian</h1>
                <p style="margin: 10px 0 0 0;">Advanced Anti-Theft Protection</p>
            </div>
            <div style="background: white; padding: 30px; border: 1px solid #e2e8f0; border-radius: 0 0 10px 10px; color: #333;">
                <h2>Email Verification Required</h2>
                <p>Hello ${name},</p>
                <p>Your secure verification code is:</p>
                <div style="background: #f7fafc; border: 2px solid #667eea; padding: 20px; text-align: center; border-radius: 10px; margin: 20px 0;">
                    <div style="font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; font-family: monospace;">${verificationCode}</div>
                </div>
                <p><strong>Security Notice:</strong></p>
                <ul style="color: #666; font-size: 14px;">
                    <li>This code expires in 10 minutes</li>
                    <li>Never share this code with anyone</li>
                    <li>We will never ask for this code via phone or email</li>
                    <li>If you didn't request this, please ignore this email</li>
                </ul>
                <hr style="margin: 20px 0; border: none; border-top: 1px solid #eee;">
                <p style="font-size: 12px; color: #999; text-align: center;">
                    This is an automated security message from Secure Guardian Anti-Theft System
                </p>
            </div>
        </div>`;

        const info = await emailTransporter.sendMail({
            from: '"Secure Guardian Security" <security@secureguardian.com>',
            to: email,
            subject: 'Security Verification Code - Secure Guardian',
            html: emailHtml
        });

        const previewUrl = nodemailer.getTestMessageUrl(info);
        
        // Clear failed attempts on successful registration
        clearFailedAttempts(email);

        res.json({
            success: true,
            message: 'Secure verification code sent to your email',
            userId: userId,
            previewUrl: previewUrl,
            expiresIn: '10 minutes'
        });

    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({ error: 'Registration failed due to server error' });
    }
});

// Enhanced verification with security
app.post('/api/auth/verify', async (req, res) => {
    try {
        const { userId, code } = req.body;

        if (!userId || !code) {
            return res.status(400).json({ error: 'User ID and verification code are required' });
        }

        const user = users.get(userId);
        const verification = verificationCodes.get(userId);

        if (!user || !verification) {
            return res.status(400).json({ error: 'Invalid verification request' });
        }

        // Check expiration
        if (new Date() > verification.expiresAt) {
            verificationCodes.delete(userId);
            return res.status(400).json({ error: 'Verification code has expired. Please request a new one.' });
        }

        // Check attempts
        if (verification.attempts >= 3) {
            verificationCodes.delete(userId);
            return res.status(400).json({ error: 'Too many verification attempts. Please request a new code.' });
        }

        // Verify code
        if (verification.code !== code) {
            verification.attempts++;
            verificationCodes.set(userId, verification);
            return res.status(400).json({ 
                error: `Invalid verification code. ${3 - verification.attempts} attempts remaining.` 
            });
        }

        // Successful verification
        user.verified = true;
        user.lastLogin = new Date();
        const sessionToken = generateSecureToken();
        
        users.set(userId, user);
        verificationCodes.delete(userId);

        // Create secure session
        sessions.set(sessionToken, {
            userId: userId,
            createdAt: new Date(),
            expiresAt: new Date(Date.now() + 24 * 60 * 60 * 1000), // 24 hours
            ipAddress: req.ip,
            userAgent: req.get('User-Agent')
        });

        res.json({
            success: true,
            message: 'Email verified successfully',
            token: sessionToken,
            user: {
                id: userId,
                email: decrypt(user.email),
                name: decrypt(user.name),
                verified: true
            }
        });

    } catch (error) {
        console.error('Verification error:', error);
        res.status(500).json({ error: 'Verification failed due to server error' });
    }
});

// Session validation middleware
function validateSession(req, res, next) {
    const token = req.headers.authorization?.replace('Bearer ', '');
    
    if (!token) {
        return res.status(401).json({ error: 'Authentication token required' });
    }

    const session = sessions.get(token);
    if (!session) {
        return res.status(401).json({ error: 'Invalid or expired session' });
    }

    if (new Date() > session.expiresAt) {
        sessions.delete(token);
        return res.status(401).json({ error: 'Session expired' });
    }

    // Validate IP and User-Agent for additional security
    if (session.ipAddress !== req.ip || session.userAgent !== req.get('User-Agent')) {
        sessions.delete(token);
        return res.status(401).json({ error: 'Session security violation detected' });
    }

    req.session = session;
    req.user = users.get(session.userId);
    next();
}

// Secure device status endpoint
app.get('/api/device/status', validateSession, (req, res) => {
    try {
        // Return mock secure device data
        res.json({
            deviceId: 'secure_device_' + req.session.userId.slice(-8),
            location: { lat: 37.9755, lng: 23.7348 },
            battery: Math.floor(Math.random() * 100),
            isSecure: true,
            lastUpdate: new Date(),
            securityLevel: 'HIGH'
        });
    } catch (error) {
        console.error('Status error:', error);
        res.status(500).json({ error: 'Failed to get device status' });
    }
});

// Start secure server
server.listen(PORT, async () => {
    console.log(`🔒 Secure Anti-Theft System running on port ${PORT}`);
    console.log(`🌐 Open: http://localhost:${PORT}`);
    console.log(`🛡️ Security features: Rate limiting, encryption, session validation`);
    
    await initializeSecureEmail();
    
    console.log('✅ Secure system ready');
});