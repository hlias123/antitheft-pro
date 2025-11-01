const express = require('express');
const cors = require('cors');
const path = require('path');
const nodemailer = require('nodemailer');

const app = express();
const PORT = 3001;

// Email configuration
let transporter = null;

// Initialize email transporter
async function initializeEmail() {
    try {
        // Use Gmail SMTP for REAL email sending
        transporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
                user: 'your.email@gmail.com', // ضع إيميلك هنا
                pass: 'your-app-password'     // ضع كلمة مرور التطبيق هنا
            }
        });

        console.log('📧 Email service initialized with Gmail SMTP');
        console.log('📧 Ready to send REAL emails!');
        
    } catch (error) {
        console.error('❌ Email initialization failed:', error.message);
    }
}

// Send email function
async function sendEmail(to, subject, html) {
    if (!transporter) {
        console.log('⚠️ Email not configured, showing code in console only');
        return { success: false, previewUrl: null };
    }

    try {
        const info = await transporter.sendMail({
            from: '"Secure Guardian" <your.email@gmail.com>', // نفس الإيميل المرسل
            to: to,
            subject: subject,
            html: html
        });

        console.log(`✅ REAL Email sent to ${to}`);
        console.log(`📧 Message ID: ${info.messageId}`);
        
        return { success: true, messageId: info.messageId };
        
    } catch (error) {
        console.error('❌ Email sending failed:', error.message);
        return { success: false, previewUrl: null };
    }
}

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static('public'));

// Redirect root to app.html
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'clean-app.html'));
});

// Simple register - accepts any email
app.post('/auth/register', async (req, res) => {
    try {
        const { name, email, password } = req.body;

        if (!name || !email || !password) {
            return res.status(400).json({ error: 'جميع الحقول مطلوبة' });
        }

        if (password.length < 6) {
            return res.status(400).json({ error: 'كلمة المرور يجب أن تكون 6 أحرف على الأقل' });
        }

        // Generate verification code
        const code = Math.floor(1000 + Math.random() * 9000).toString();
        const userId = 'user_' + Date.now();

        console.log(`📧 Registration for ${email}`);
        console.log(`🔑 Verification code: ${code}`);

        // Send verification email
        const emailHtml = `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;">
            <div style="background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                <h2 style="color: #2c3e50; text-align: center; margin-bottom: 30px;">🛡️ Secure Guardian</h2>
                <h3 style="color: #34495e; text-align: center;">رمز التحقق من البريد الإلكتروني</h3>
                
                <p style="color: #555; font-size: 16px; line-height: 1.6;">مرحباً ${name}،</p>
                
                <p style="color: #555; font-size: 16px; line-height: 1.6;">
                    شكراً لك على التسجيل في Secure Guardian. استخدم الرمز التالي لتأكيد بريدك الإلكتروني:
                </p>
                
                <div style="background-color: #3498db; color: white; font-size: 32px; font-weight: bold; text-align: center; padding: 20px; margin: 30px 0; border-radius: 8px; letter-spacing: 5px;">
                    ${code}
                </div>
                
                <p style="color: #555; font-size: 14px; line-height: 1.6;">
                    هذا الرمز صالح لمدة 10 دقائق فقط. إذا لم تطلب هذا الرمز، يرجى تجاهل هذه الرسالة.
                </p>
                
                <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                
                <p style="color: #888; font-size: 12px; text-align: center;">
                    هذه رسالة تلقائية، يرجى عدم الرد عليها.
                </p>
            </div>
        </div>`;

        const emailResult = await sendEmail(email, 'رمز التحقق - Secure Guardian', emailHtml);

        res.json({
            success: true,
            message: 'تم إنشاء الحساب بنجاح وإرسال رمز التحقق',
            userId: userId,
            requiresVerification: true,
            emailSent: emailResult.success,
            previewUrl: emailResult.previewUrl,
            verificationCode: code // Keep for testing
        });

    } catch (error) {
        console.error('Registration error:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// Simple login - accepts any email/password
app.post('/auth/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ error: 'البريد الإلكتروني وكلمة المرور مطلوبان' });
        }

        // Generate MFA code
        const code = Math.floor(1000 + Math.random() * 9000).toString();
        const userId = 'user_' + Date.now();

        console.log(`🔑 Login for ${email}`);
        console.log(`🔑 MFA code: ${code}`);

        // Send MFA email
        const emailHtml = `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;">
            <div style="background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                <h2 style="color: #2c3e50; text-align: center; margin-bottom: 30px;">🛡️ Secure Guardian</h2>
                <h3 style="color: #34495e; text-align: center;">رمز التحقق من تسجيل الدخول</h3>
                
                <p style="color: #555; font-size: 16px; line-height: 1.6;">مرحباً،</p>
                
                <p style="color: #555; font-size: 16px; line-height: 1.6;">
                    تم طلب تسجيل دخول إلى حسابك. استخدم الرمز التالي لإكمال عملية تسجيل الدخول:
                </p>
                
                <div style="background-color: #27ae60; color: white; font-size: 32px; font-weight: bold; text-align: center; padding: 20px; margin: 30px 0; border-radius: 8px; letter-spacing: 5px;">
                    ${code}
                </div>
                
                <p style="color: #555; font-size: 14px; line-height: 1.6;">
                    هذا الرمز صالح لمدة 5 دقائق فقط. إذا لم تحاول تسجيل الدخول، يرجى تجاهل هذه الرسالة.
                </p>
                
                <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                
                <p style="color: #888; font-size: 12px; text-align: center;">
                    هذه رسالة تلقائية، يرجى عدم الرد عليها.
                </p>
            </div>
        </div>`;

        const emailResult = await sendEmail(email, 'رمز تسجيل الدخول - Secure Guardian', emailHtml);

        res.json({
            success: true,
            message: 'تم إرسال رمز التحقق إلى بريدك الإلكتروني',
            userId: userId,
            requiresMFA: true,
            emailSent: emailResult.success,
            previewUrl: emailResult.previewUrl,
            verificationCode: code // Keep for testing
        });

    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// Simple verify - accepts any code
app.post('/auth/verify', (req, res) => {
    try {
        const { userId, code } = req.body;

        if (!userId || !code) {
            return res.status(400).json({ error: 'معرف المستخدم ورمز التحقق مطلوبان' });
        }

        console.log(`✅ Verification for user ${userId} with code ${code}`);

        // Accept any 4-digit code
        if (code.length === 4) {
            res.json({
                success: true,
                message: 'تم التحقق بنجاح',
                token: 'demo_token_' + Date.now(),
                user: {
                    id: userId,
                    name: 'مستخدم تجريبي',
                    email: 'test@example.com',
                    isVerified: true
                }
            });
        } else {
            res.status(400).json({ error: 'رمز التحقق غير صحيح' });
        }

    } catch (error) {
        console.error('Verification error:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// Resend verification
app.post('/auth/resend-verification', async (req, res) => {
    try {
        const { userId, email } = req.body;
        const code = Math.floor(1000 + Math.random() * 9000).toString();
        
        console.log(`📧 Resending code for user ${userId}: ${code}`);

        // Send resend email
        const emailHtml = `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;">
            <div style="background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                <h2 style="color: #2c3e50; text-align: center; margin-bottom: 30px;">🛡️ Secure Guardian</h2>
                <h3 style="color: #34495e; text-align: center;">إعادة إرسال رمز التحقق</h3>
                
                <p style="color: #555; font-size: 16px; line-height: 1.6;">مرحباً،</p>
                
                <p style="color: #555; font-size: 16px; line-height: 1.6;">
                    تم طلب إعادة إرسال رمز التحقق. استخدم الرمز الجديد التالي:
                </p>
                
                <div style="background-color: #e74c3c; color: white; font-size: 32px; font-weight: bold; text-align: center; padding: 20px; margin: 30px 0; border-radius: 8px; letter-spacing: 5px;">
                    ${code}
                </div>
                
                <p style="color: #555; font-size: 14px; line-height: 1.6;">
                    هذا الرمز صالح لمدة 10 دقائق فقط.
                </p>
                
                <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                
                <p style="color: #888; font-size: 12px; text-align: center;">
                    هذه رسالة تلقائية، يرجى عدم الرد عليها.
                </p>
            </div>
        </div>`;

        const emailResult = await sendEmail(email, 'إعادة إرسال رمز التحقق - Secure Guardian', emailHtml);

        res.json({
            success: true,
            message: 'تم إعادة إرسال رمز التحقق إلى بريدك الإلكتروني',
            emailSent: emailResult.success,
            previewUrl: emailResult.previewUrl,
            verificationCode: code
        });

    } catch (error) {
        console.error('Resend error:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// Health check
app.get('/health', (req, res) => {
    res.json({ 
        status: 'healthy', 
        timestamp: new Date().toISOString(),
        service: 'Simple Secure Guardian API'
    });
});

// Start server
app.listen(PORT, async () => {
    console.log(`🚀 Simple Secure Guardian API running on port ${PORT}`);
    console.log(`📧 Accepts ANY email address for testing`);
    console.log(`🔑 Verification codes will be shown in console AND sent to real emails`);
    console.log(`🌐 Open: http://localhost:${PORT}/clean-app.html`);
    
    // Initialize email service
    await initializeEmail();
});