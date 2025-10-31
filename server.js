// Load environment variables
require('dotenv').config();

const express = require('express');
const cors = require('cors');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const nodemailer = require('nodemailer');
const sqlite3 = require('sqlite3').verbose();
const { v4: uuidv4 } = require('uuid');
const path = require('path');
const fs = require('fs');

const app = express();
const PORT = process.env.PORT || 3001;

// Configuration
const JWT_SECRET = process.env.JWT_SECRET || 'secure-guardian-2024-secret-key';
const EMAIL_USER = process.env.EMAIL_USER;
const EMAIL_PASS = process.env.EMAIL_PASS;

// Middleware
app.use(cors({
    origin: process.env.ALLOWED_ORIGINS?.split(',') || ['*'],
    credentials: true
}));
app.use(express.json());
app.use(express.static('public'));

// Redirect root to app.html
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'app.html'));
});

// Explicit route for app.html
app.get('/app.html', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'app.html'));
});

// Database setup
const dbPath = process.env.DATABASE_PATH || path.join(__dirname, 'data', 'guardian.db');
const dataDir = path.dirname(dbPath);
if (!fs.existsSync(dataDir)) {
    fs.mkdirSync(dataDir, { recursive: true });
}
const db = new sqlite3.Database(dbPath);

// Initialize database
db.serialize(() => {
    db.run(`CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY,
        email TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        name TEXT NOT NULL,
        is_verified INTEGER DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )`);

    db.run(`CREATE TABLE IF NOT EXISTS verification_codes (
        id TEXT PRIMARY KEY,
        user_id TEXT NOT NULL,
        code TEXT NOT NULL,
        type TEXT NOT NULL,
        expires_at DATETIME NOT NULL,
        used INTEGER DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users (id)
    )`);

    db.run(`CREATE TABLE IF NOT EXISTS sessions (
        id TEXT PRIMARY KEY,
        user_id TEXT NOT NULL,
        token TEXT NOT NULL,
        expires_at DATETIME NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users (id)
    )`);

    // No test users - Real registration system only
    console.log('✅ Database initialized - Ready for real user registration');

    console.log('✅ Database initialized successfully');
});

// Email configuration
let transporter = null;
if (EMAIL_USER && EMAIL_PASS) {
    transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: EMAIL_USER,
            pass: EMAIL_PASS
        },
        tls: {
            rejectUnauthorized: false
        }
    });
    console.log('✅ Email service configured');
} else {
    console.log('⚠️ Email service not configured');
    console.log('📧 To enable real email sending:');
    console.log('   1. Edit .env file');
    console.log('   2. Set EMAIL_USER to your Gmail address');
    console.log('   3. Set EMAIL_PASS to your Gmail App Password');
    console.log('   4. Restart the server');
    console.log('');
    console.log('🔍 For now, verification codes will be logged here:');
}

// Helper functions
function generateVerificationCode() {
    return Math.floor(100000 + Math.random() * 900000).toString();
}

// Clean expired verification codes (disabled for debugging)
function cleanExpiredCodes() {
    db.run('DELETE FROM verification_codes WHERE expires_at < datetime("now")', function(err) {
        if (err) {
            console.error('Error cleaning expired codes:', err);
        } else {
            if (this.changes > 0) {
                console.log(`🧹 Cleaned ${this.changes} expired verification codes`);
            }
        }
    });
}

// Clean expired codes every 5 minutes (reduced frequency for debugging)
setInterval(cleanExpiredCodes, 5 * 60 * 1000);

async function sendVerificationEmail(email, code, type = 'login') {
    const subject = type === 'login' ? 'Secure Guardian - Login Code' : 'Secure Guardian - Verification Code';
    const html = `
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Secure Guardian Verification</title>
        </head>
        <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
            <table width="100%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                <tr>
                    <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0" style="background-color: white; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                            <tr>
                                <td style="padding: 40px; text-align: center;">
                                    <h1 style="color: #667eea; margin: 0 0 20px 0; font-size: 28px;">🛡️ Secure Guardian</h1>
                                    <h2 style="color: #333; margin: 0 0 30px 0; font-size: 20px;">Your Verification Code</h2>
                                    
                                    <div style="background: #f8f9fa; padding: 30px; border-radius: 8px; margin: 20px 0;">
                                        <div style="font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; margin: 10px 0;">
                                            ${code}
                                        </div>
                                    </div>
                                    
                                    <p style="color: #666; font-size: 16px; margin: 20px 0;">
                                        This code is valid for 3 minutes only.
                                    </p>
                                    
                                    <p style="color: #999; font-size: 14px; margin: 30px 0 0 0;">
                                        If you didn't request this code, please ignore this email.
                                    </p>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
    `;

    if (transporter) {
        try {
            const mailOptions = {
                from: `"Secure Guardian Pro" <${EMAIL_USER}>`,
                to: email,
                subject: subject,
                html: html,
                text: `Your Secure Guardian verification code is: ${code}. This code is valid for 3 minutes only.`,
                headers: {
                    'X-Priority': '1',
                    'X-MSMail-Priority': 'High',
                    'Importance': 'high'
                }
            };
            
            await transporter.sendMail(mailOptions);
            console.log(`✅ Email sent successfully to ${email}`);
            return true;
        } catch (error) {
            console.error('❌ Email sending error:', error);
            console.log(`📧 Verification code for ${email}: ${code}`);
            return false;
        }
    } else {
        console.log(`📧 Verification code for ${email}: ${code}`);
        return false;
    }
}

// Authentication middleware
function authenticateToken(req, res, next) {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) {
        return res.status(401).json({ error: 'Access token required' });
    }

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) {
            return res.status(403).json({ error: 'Invalid or expired token' });
        }
        req.user = user;
        next();
    });
}

// Routes

// Health check
app.get('/health', (req, res) => {
    res.json({ 
        status: 'healthy', 
        timestamp: new Date().toISOString(),
        service: 'Secure Guardian Pro API'
    });
});

// Debug route to check files
app.get('/debug', (req, res) => {
    const publicPath = path.join(__dirname, 'public');
    const appHtmlPath = path.join(__dirname, 'public', 'app.html');
    
    res.json({
        __dirname: __dirname,
        publicPath: publicPath,
        appHtmlPath: appHtmlPath,
        publicExists: fs.existsSync(publicPath),
        appHtmlExists: fs.existsSync(appHtmlPath),
        publicContents: fs.existsSync(publicPath) ? fs.readdirSync(publicPath) : 'Directory not found',
        cwd: process.cwd()
    });
});

// API documentation
app.get('/api', (req, res) => {
    res.json({
        service: 'Secure Guardian Pro API',
        version: '1.0.0',
        endpoints: {
            'POST /auth/register': 'Register new user',
            'POST /auth/login': 'Login user',
            'POST /auth/verify': 'Verify MFA code',
            'POST /auth/resend-verification': 'Resend verification code',
            'POST /auth/create-test-user': 'Create test user (development only)',
            'GET /auth/profile': 'Get user profile',
            'POST /auth/logout': 'Logout user',
            'GET /health': 'Health check'
        }
    });
});

// Register
app.post('/auth/register', async (req, res) => {
    console.log('📥 Registration request received');
    console.log('📋 Request body:', { 
        name: req.body?.name, 
        email: req.body?.email, 
        password: req.body?.password ? '***' : 'missing' 
    });
    
    try {
        const { name, email, password } = req.body;

        if (!name || !email || !password) {
            return res.status(400).json({ error: 'جميع الحقول مطلوبة' });
        }

        if (password.length < 8) {
            return res.status(400).json({ error: 'كلمة المرور يجب أن تكون 8 أحرف على الأقل' });
        }

        // Check if user exists
        db.get('SELECT id FROM users WHERE email = ?', [email], async (err, row) => {
            if (err) {
                return res.status(500).json({ error: 'خطأ في قاعدة البيانات' });
            }

            if (row) {
                // Check if user is verified
                if (row.is_verified) {
                    return res.status(400).json({ 
                        error: 'البريد الإلكتروني مستخدم بالفعل. يمكنك تسجيل الدخول مباشرة.',
                        canLogin: true
                    });
                } else {
                    return res.status(400).json({ 
                        error: 'البريد الإلكتروني مستخدم بالفعل ولكن الحساب غير محقق. يرجى التحقق من بريدك الإلكتروني أو طلب رمز جديد.',
                        needsVerification: true,
                        userId: row.id
                    });
                }
            }

            // Hash password
            const passwordHash = await bcrypt.hash(password, 12);
            const userId = uuidv4();

            // Create user
            db.run('INSERT INTO users (id, name, email, password_hash) VALUES (?, ?, ?, ?)',
                [userId, name, email, passwordHash], function(err) {
                    if (err) {
                        return res.status(500).json({ error: 'فشل في إنشاء الحساب' });
                    }

                    // Generate verification code
                    const code = generateVerificationCode();
                    const codeId = uuidv4();
                    const expiresAt = new Date(Date.now() + 3 * 60 * 1000); // 3 minutes

                    console.log('📝 Creating verification code:', {
                        codeId,
                        userId,
                        code,
                        type: 'register',
                        expiresAt: expiresAt.toISOString(),
                        currentTime: new Date().toISOString()
                    });

                    db.run('INSERT INTO verification_codes (id, user_id, code, type, expires_at) VALUES (?, ?, ?, ?, ?)',
                        [codeId, userId, code, 'register', expiresAt.toISOString()], async (err) => {
                            if (err) {
                                console.error('❌ Error creating verification code:', err);
                                return res.status(500).json({ error: 'فشل في إرسال رمز التحقق' });
                            }
                            
                            console.log('✅ Verification code created successfully in database');

                            const emailSent = await sendVerificationEmail(email, code, 'register');

                            res.json({
                                success: true,
                                message: emailSent ? 'تم إنشاء الحساب بنجاح وإرسال رمز التحقق إلى بريدك الإلكتروني' : 'تم إنشاء الحساب بنجاح',
                                userId: userId,
                                requiresVerification: true,
                                emailSent: emailSent,
                                verificationCode: emailSent ? null : code,
                                note: emailSent ? 'تحقق من مجلد الرسائل غير المرغوب فيها (Spam) إذا لم تجد الإيميل' : 'لم يتم إرسال الإيميل. الرمز موضح أعلاه للاختبار.',
                                spamWarning: emailSent
                            });
                        });
                });
        });
    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// Login
app.post('/auth/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ error: 'البريد الإلكتروني وكلمة المرور مطلوبان' });
        }

        // Find user
        db.get('SELECT * FROM users WHERE email = ?', [email], async (err, user) => {
            if (err) {
                console.error('Database error during login:', err);
                return res.status(500).json({ error: 'خطأ في قاعدة البيانات' });
            }

            if (!user) {
                console.log(`Login attempt with non-existent email: ${email}`);
                return res.status(401).json({ error: 'بيانات تسجيل الدخول غير صحيحة' });
            }

            // Check password
            const validPassword = await bcrypt.compare(password, user.password_hash);
            if (!validPassword) {
                console.log(`Invalid password for email: ${email}`);
                return res.status(401).json({ error: 'بيانات تسجيل الدخول غير صحيحة' });
            }

            console.log(`Successful login attempt for: ${email}`);

            // Check if account is verified
            if (!user.is_verified) {
                return res.status(403).json({ 
                    error: 'الحساب غير موثق. يرجى التحقق من بريدك الإلكتروني أولاً',
                    requiresVerification: true,
                    userId: user.id,
                    message: 'تحتاج إلى التحقق من حسابك أولاً. تحقق من بريدك الإلكتروني أو اطلب رمز تحقق جديد.'
                });
            }

            // Generate JWT token for direct login (no MFA required for verified users)
            const token = jwt.sign(
                { 
                    userId: user.id, 
                    email: user.email,
                    name: user.name
                },
                JWT_SECRET,
                { expiresIn: '24h' }
            );

            // Create session
            const sessionId = uuidv4();
            const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000); // 24 hours

            db.run('INSERT INTO sessions (id, user_id, token, expires_at) VALUES (?, ?, ?, ?)',
                [sessionId, user.id, token, expiresAt], (err) => {
                    if (err) {
                        console.error('Session creation error:', err);
                    }

                    res.json({
                        success: true,
                        message: 'تم تسجيل الدخول بنجاح',
                        token: token,
                        requiresMFA: false, // No MFA required for verified users
                        user: {
                            id: user.id,
                            name: user.name,
                            email: user.email,
                            isVerified: true
                        }
                    });
                });
        });
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// Verify MFA
app.post('/auth/verify', (req, res) => {
    try {
        const { userId, code } = req.body;

        if (!userId || !code) {
            return res.status(400).json({ error: 'معرف المستخدم ورمز التحقق مطلوبان' });
        }

        console.log('🔍 Verifying code:', {
            userId,
            code,
            currentTime: new Date().toISOString()
        });

        // Find valid code
        db.get(`SELECT vc.*, u.name, u.email, u.is_verified,
                datetime('now') as current_db_time,
                CASE WHEN vc.expires_at > datetime('now') THEN 'valid' ELSE 'expired' END as time_status
                FROM verification_codes vc 
                JOIN users u ON vc.user_id = u.id 
                WHERE vc.user_id = ? AND vc.code = ? AND vc.used = 0`,
            [userId, code], (err, row) => {
                console.log('📊 Database query result:', row);
                
                if (err) {
                    console.error('❌ Database error during verification:', err);
                    return res.status(500).json({ error: 'خطأ في قاعدة البيانات' });
                }

                if (!row) {
                    console.log('❌ No valid code found, checking for expired/used codes...');
                    
                    // Check if code exists but expired or used
                    db.get('SELECT *, datetime("now") as current_time FROM verification_codes WHERE user_id = ? AND code = ?', 
                        [userId, code], (err, anyRow) => {
                            console.log('🔍 Code check result:', anyRow);
                            
                            if (anyRow) {
                                if (anyRow.used === 1) {
                                    console.log('⚠️ Code already used');
                                    return res.status(400).json({ 
                                        error: 'رمز التحقق مستخدم بالفعل. يرجى طلب رمز جديد',
                                        used: true
                                    });
                                } else if (anyRow.expires_at <= anyRow.current_time) {
                                    console.log('⏰ Code expired');
                                    return res.status(400).json({ 
                                        error: 'رمز التحقق منتهي الصلاحية. يرجى طلب رمز جديد',
                                        expired: true
                                    });
                                }
                            }
                            
                            console.log('🔍 Code not found in database');
                            return res.status(400).json({ 
                                error: 'رمز التحقق غير صحيح. تأكد من إدخال الرمز بشكل صحيح',
                                invalid: true
                            });
                        });
                    return;
                }
                
                console.log('✅ Valid code found:', {
                    code: row.code,
                    expires_at: row.expires_at,
                    time_status: row.time_status,
                    current_db_time: row.current_db_time
                });
                
                if (row.time_status === 'expired') {
                    console.log('⏰ Code is expired according to database');
                    return res.status(400).json({ 
                        error: 'رمز التحقق منتهي الصلاحية. يرجى طلب رمز جديد',
                        expired: true
                    });
                }

                // Mark code as used
                db.run('UPDATE verification_codes SET used = 1 WHERE id = ?', [row.id], (err) => {
                    if (err) {
                        return res.status(500).json({ error: 'خطأ في قاعدة البيانات' });
                    }

                    // Mark user as verified if registering
                    if (row.type === 'register') {
                        db.run('UPDATE users SET is_verified = 1 WHERE id = ?', [userId]);
                    }

                    // Generate JWT token
                    const token = jwt.sign(
                        { 
                            userId: userId, 
                            email: row.email,
                            name: row.name
                        },
                        JWT_SECRET,
                        { expiresIn: '24h' }
                    );

                    // Create session
                    const sessionId = uuidv4();
                    const expiresAt = new Date(Date.now() + 24 * 60 * 60 * 1000); // 24 hours

                    db.run('INSERT INTO sessions (id, user_id, token, expires_at) VALUES (?, ?, ?, ?)',
                        [sessionId, userId, token, expiresAt], (err) => {
                            if (err) {
                                console.error('Session creation error:', err);
                            }

                            res.json({
                                success: true,
                                message: 'تم التحقق بنجاح',
                                token: token,
                                user: {
                                    id: userId,
                                    name: row.name,
                                    email: row.email,
                                    isVerified: true
                                }
                            });
                        });
                });
            });
    } catch (error) {
        console.error('Verification error:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// Resend verification code
app.post('/auth/resend-verification', async (req, res) => {
    try {
        const { userId } = req.body;

        if (!userId) {
            return res.status(400).json({ error: 'معرف المستخدم مطلوب' });
        }

        // Get user info
        db.get('SELECT email, name, is_verified FROM users WHERE id = ?', [userId], async (err, user) => {
            if (err) {
                return res.status(500).json({ error: 'خطأ في قاعدة البيانات' });
            }

            if (!user) {
                return res.status(404).json({ error: 'المستخدم غير موجود' });
            }

            if (user.is_verified) {
                return res.status(400).json({ error: 'الحساب موثق بالفعل' });
            }

            // Generate new verification code
            const code = generateVerificationCode();
            const codeId = uuidv4();
            const expiresAt = new Date(Date.now() + 3 * 60 * 1000); // 3 minutes

            // Delete old codes for this user
            db.run('DELETE FROM verification_codes WHERE user_id = ? AND type = ?', [userId, 'register'], (err) => {
                if (err) {
                    console.error('Error deleting old codes:', err);
                }

                // Insert new code
                db.run('INSERT INTO verification_codes (id, user_id, code, type, expires_at) VALUES (?, ?, ?, ?, ?)',
                    [codeId, userId, code, 'register', expiresAt.toISOString()], async (err) => {
                        if (err) {
                            return res.status(500).json({ error: 'فشل في إنشاء رمز التحقق' });
                        }

                        const emailSent = await sendVerificationEmail(user.email, code, 'register');

                        res.json({
                            success: true,
                            message: 'تم إعادة إرسال رمز التحقق',
                            emailSent: emailSent,
                            verificationCode: emailSent ? null : code
                        });
                    });
            });
        });
    } catch (error) {
        console.error('Resend verification error:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// Create test user (development only)
app.post('/auth/create-test-user', async (req, res) => {
    try {
        const { name, email, password } = req.body;

        if (!name || !email || !password) {
            return res.status(400).json({ error: 'الاسم والإيميل وكلمة المرور مطلوبة' });
        }

        // Check if user already exists
        db.get('SELECT id FROM users WHERE email = ?', [email], async (err, existingUser) => {
            if (err) {
                return res.status(500).json({ error: 'خطأ في قاعدة البيانات' });
            }

            if (existingUser) {
                return res.status(400).json({ error: 'المستخدم موجود بالفعل' });
            }

            try {
                const passwordHash = await bcrypt.hash(password, 12);
                const userId = uuidv4();

                db.run('INSERT INTO users (id, name, email, password_hash, is_verified) VALUES (?, ?, ?, ?, ?)',
                    [userId, name, email, passwordHash, 1], (err) => {
                        if (err) {
                            return res.status(500).json({ error: 'فشل في إنشاء المستخدم' });
                        }

                        console.log(`✅ Test user created via API: ${email}`);
                        res.json({
                            success: true,
                            message: 'تم إنشاء المستخدم التجريبي بنجاح',
                            user: {
                                id: userId,
                                name,
                                email,
                                isVerified: true
                            }
                        });
                    });
            } catch (error) {
                console.error('Error hashing password:', error);
                res.status(500).json({ error: 'خطأ في تشفير كلمة المرور' });
            }
        });
    } catch (error) {
        console.error('Create test user error:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// Get profile
app.get('/auth/profile', authenticateToken, (req, res) => {
    db.get('SELECT id, name, email, is_verified, created_at FROM users WHERE id = ?', 
        [req.user.userId], (err, user) => {
            if (err) {
                return res.status(500).json({ error: 'خطأ في قاعدة البيانات' });
            }

            if (!user) {
                return res.status(404).json({ error: 'المستخدم غير موجود' });
            }

            res.json({
                success: true,
                user: {
                    id: user.id,
                    name: user.name,
                    email: user.email,
                    isVerified: user.is_verified === 1,
                    createdAt: user.created_at
                }
            });
        });
});

// Logout
app.post('/auth/logout', authenticateToken, (req, res) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    db.run('DELETE FROM sessions WHERE token = ?', [token], (err) => {
        if (err) {
            console.error('Logout error:', err);
        }

        res.json({
            success: true,
            message: 'تم تسجيل الخروج بنجاح'
        });
    });
});

// Debug endpoint to check database
app.get('/debug/database', (req, res) => {
    try {
        // Check verification_codes table
        db.all("SELECT * FROM verification_codes ORDER BY created_at DESC LIMIT 20", (err, rows) => {
            if (err) {
                console.error('❌ Database debug error:', err);
                res.json({ error: err.message, codes: [] });
                return;
            }
            
            console.log(`📊 Database debug: Found ${rows.length} verification codes`);
            res.json({
                success: true,
                totalCodes: rows.length,
                codes: rows.map(row => ({
                    code: row.code,
                    userId: row.user_id,
                    expires_at: row.expires_at,
                    used: row.used,
                    created_at: row.created_at,
                    type: row.type
                }))
            });
        });
    } catch (error) {
        console.error('❌ Debug endpoint error:', error);
        res.status(500).json({ error: error.message });
    }
});

// Start server
app.listen(PORT, () => {
    console.log(`🚀 Secure Guardian Pro API running on port ${PORT}`);
    console.log(`📊 Health check: http://localhost:${PORT}/health`);
    console.log(`📚 API docs: http://localhost:${PORT}/api`);
});