const express = require('express');
const nodemailer = require('nodemailer');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = 3002;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static('public'));

// إعداد Gmail SMTP للإيميلات الحقيقية
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'hliahlias1984@gmail.com',    // إيميلك الحقيقي
        pass: 'xxxx xxxx xxxx xxxx'        // كلمة مرور التطبيق من Gmail
    }
});

// تخزين مؤقت للمستخدمين والرموز
const users = new Map();
const verificationCodes = new Map();

// الصفحة الرئيسية
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'real-email-app.html'));
});

// تسجيل حساب جديد
app.post('/register', async (req, res) => {
    try {
        const { name, email, password } = req.body;

        if (!name || !email || !password) {
            return res.status(400).json({ error: 'جميع الحقول مطلوبة' });
        }

        if (password.length < 6) {
            return res.status(400).json({ error: 'كلمة المرور يجب أن تكون 6 أحرف على الأقل' });
        }

        // توليد رمز التحقق
        const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();
        const userId = 'user_' + Date.now();

        // حفظ بيانات المستخدم
        users.set(userId, {
            name,
            email,
            password,
            isVerified: false,
            createdAt: new Date()
        });

        verificationCodes.set(userId, {
            code: verificationCode,
            expiresAt: new Date(Date.now() + 10 * 60 * 1000) // 10 دقائق
        });

        // إرسال إيميل التحقق الحقيقي
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
                    ${verificationCode}
                </div>
                
                <p style="color: #555; font-size: 14px; line-height: 1.6;">
                    هذا الرمز صالح لمدة 10 دقائق فقط. إذا لم تطلب هذا الرمز، يرجى تجاهل هذه الرسالة.
                </p>
                
                <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                
                <p style="color: #888; font-size: 12px; text-align: center;">
                    هذه رسالة تلقائية من Secure Guardian
                </p>
            </div>
        </div>`;

        try {
            await transporter.sendMail({
                from: '"Secure Guardian" <hliahlias1984@gmail.com>',
                to: email,
                subject: 'رمز التحقق - Secure Guardian',
                html: emailHtml
            });

            console.log(`✅ إيميل حقيقي تم إرساله إلى: ${email}`);
            console.log(`🔑 رمز التحقق: ${verificationCode}`);

            res.json({
                success: true,
                message: 'تم إنشاء الحساب وإرسال رمز التحقق إلى بريدك الإلكتروني',
                userId: userId,
                emailSent: true
            });

        } catch (emailError) {
            console.error('❌ فشل في إرسال الإيميل:', emailError);
            res.status(500).json({ 
                error: 'تم إنشاء الحساب لكن فشل في إرسال الإيميل. تحقق من إعدادات الإيميل.' 
            });
        }

    } catch (error) {
        console.error('خطأ في التسجيل:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// تحقق من الرمز
app.post('/verify', (req, res) => {
    try {
        const { userId, code } = req.body;

        if (!userId || !code) {
            return res.status(400).json({ error: 'معرف المستخدم ورمز التحقق مطلوبان' });
        }

        const user = users.get(userId);
        const verification = verificationCodes.get(userId);

        if (!user || !verification) {
            return res.status(400).json({ error: 'بيانات غير صحيحة' });
        }

        if (new Date() > verification.expiresAt) {
            return res.status(400).json({ error: 'انتهت صلاحية رمز التحقق' });
        }

        if (verification.code !== code) {
            return res.status(400).json({ error: 'رمز التحقق غير صحيح' });
        }

        // تأكيد المستخدم
        user.isVerified = true;
        users.set(userId, user);
        verificationCodes.delete(userId);

        console.log(`✅ تم تأكيد المستخدم: ${user.email}`);

        res.json({
            success: true,
            message: 'تم التحقق بنجاح!',
            user: {
                id: userId,
                name: user.name,
                email: user.email,
                isVerified: true
            }
        });

    } catch (error) {
        console.error('خطأ في التحقق:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// إعادة إرسال رمز التحقق
app.post('/resend', async (req, res) => {
    try {
        const { userId } = req.body;

        const user = users.get(userId);
        if (!user) {
            return res.status(400).json({ error: 'مستخدم غير موجود' });
        }

        // توليد رمز جديد
        const verificationCode = Math.floor(100000 + Math.random() * 900000).toString();
        
        verificationCodes.set(userId, {
            code: verificationCode,
            expiresAt: new Date(Date.now() + 10 * 60 * 1000)
        });

        // إرسال إيميل جديد
        const emailHtml = `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f9f9f9;">
            <div style="background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                <h2 style="color: #2c3e50; text-align: center; margin-bottom: 30px;">🛡️ Secure Guardian</h2>
                <h3 style="color: #34495e; text-align: center;">رمز التحقق الجديد</h3>
                
                <p style="color: #555; font-size: 16px; line-height: 1.6;">مرحباً ${user.name}،</p>
                
                <p style="color: #555; font-size: 16px; line-height: 1.6;">
                    تم طلب رمز تحقق جديد. استخدم الرمز التالي:
                </p>
                
                <div style="background-color: #e74c3c; color: white; font-size: 32px; font-weight: bold; text-align: center; padding: 20px; margin: 30px 0; border-radius: 8px; letter-spacing: 5px;">
                    ${verificationCode}
                </div>
                
                <p style="color: #555; font-size: 14px; line-height: 1.6;">
                    هذا الرمز صالح لمدة 10 دقائق فقط.
                </p>
            </div>
        </div>`;

        await transporter.sendMail({
            from: '"Secure Guardian" <hliahlias1984@gmail.com>',
            to: user.email,
            subject: 'رمز التحقق الجديد - Secure Guardian',
            html: emailHtml
        });

        console.log(`✅ إعادة إرسال إيميل إلى: ${user.email}`);
        console.log(`🔑 رمز التحقق الجديد: ${verificationCode}`);

        res.json({
            success: true,
            message: 'تم إعادة إرسال رمز التحقق',
            emailSent: true
        });

    } catch (error) {
        console.error('خطأ في إعادة الإرسال:', error);
        res.status(500).json({ error: 'فشل في إعادة الإرسال' });
    }
});

// بدء الخادم
app.listen(PORT, () => {
    console.log(`🚀 خادم الإيميلات الحقيقية يعمل على المنفذ ${PORT}`);
    console.log(`📧 يرسل إيميلات حقيقية باستخدام Gmail SMTP`);
    console.log(`🌐 افتح: http://localhost:${PORT}`);
    console.log(`⚠️  تذكر: ضع إيميلك وكلمة مرور التطبيق في الكود`);
});