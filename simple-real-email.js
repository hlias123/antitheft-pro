const express = require('express');
const nodemailer = require('nodemailer');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = 3003;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static('public'));

// إعداد إيميل بسيط باستخدام Ethereal (للاختبار) أو Gmail
let transporter;

async function setupEmail() {
    try {
        // جرب Gmail أولاً
        transporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
                user: 'hliahlias1984@gmail.com',
                pass: 'PUT-YOUR-16-DIGIT-APP-PASSWORD-HERE' // ضع كلمة مرور التطبيق الحقيقية هنا
            }
        });
        
        // اختبار الاتصال
        await transporter.verify();
        console.log('✅ Gmail SMTP متصل بنجاح');
        return true;
        
    } catch (error) {
        console.log('❌ فشل Gmail، جاري التبديل إلى Ethereal...');
        
        // استخدم Ethereal كبديل
        const testAccount = await nodemailer.createTestAccount();
        transporter = nodemailer.createTransport({
            host: 'smtp.ethereal.email',
            port: 587,
            secure: false,
            auth: {
                user: testAccount.user,
                pass: testAccount.pass
            }
        });
        
        console.log('✅ Ethereal Email متصل (للاختبار)');
        console.log(`📧 حساب الاختبار: ${testAccount.user}`);
        return false;
    }
}

// تخزين مؤقت
const users = new Map();
const codes = new Map();

// الصفحة الرئيسية
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'multilingual-app.html'));
});

// نظام التتبع المتقدم
app.get('/advanced-tracking.html', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'advanced-tracking.html'));
});

// تسجيل وإرسال رمز
app.post('/send-code', async (req, res) => {
    try {
        const { email, name } = req.body;
        
        if (!email) {
            return res.status(400).json({ error: 'البريد الإلكتروني مطلوب' });
        }
        
        // توليد رمز
        const code = Math.floor(100000 + Math.random() * 900000).toString();
        const userId = Date.now().toString();
        
        // حفظ البيانات
        users.set(userId, { email, name: name || 'مستخدم', verified: false });
        codes.set(userId, { code, expires: Date.now() + 10 * 60 * 1000 });
        
        // إعداد الإيميل
        const emailHtml = `
        <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
            <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                <h1 style="margin: 0; font-size: 2rem;">🛡️ Secure Guardian</h1>
                <p style="margin: 10px 0 0 0; opacity: 0.9;">رمز التحقق الخاص بك</p>
            </div>
            
            <div style="background: white; padding: 30px; border: 1px solid #e2e8f0; border-radius: 0 0 10px 10px;">
                <p style="font-size: 16px; color: #4a5568; margin-bottom: 20px;">
                    مرحباً ${name || 'عزيزي المستخدم'}،
                </p>
                
                <p style="font-size: 16px; color: #4a5568; margin-bottom: 30px;">
                    استخدم الرمز التالي لتأكيد بريدك الإلكتروني:
                </p>
                
                <div style="background: #f7fafc; border: 2px dashed #667eea; padding: 20px; text-align: center; border-radius: 10px; margin: 30px 0;">
                    <div style="font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; font-family: monospace;">
                        ${code}
                    </div>
                </div>
                
                <p style="font-size: 14px; color: #718096; margin-top: 30px;">
                    هذا الرمز صالح لمدة 10 دقائق فقط.
                </p>
                
                <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 30px 0;">
                
                <p style="font-size: 12px; color: #a0aec0; text-align: center;">
                    إذا لم تطلب هذا الرمز، يرجى تجاهل هذه الرسالة.
                </p>
            </div>
        </div>`;
        
        // إرسال الإيميل
        const info = await transporter.sendMail({
            from: '"Secure Guardian" <hliahlias1984@gmail.com>',
            to: email,
            subject: `رمز التحقق: ${code}`,
            html: emailHtml
        });
        
        console.log(`📧 إيميل مرسل إلى: ${email}`);
        console.log(`🔑 الرمز: ${code}`);
        
        // إذا كان Ethereal، أعطي رابط المعاينة
        const previewUrl = nodemailer.getTestMessageUrl(info);
        
        res.json({
            success: true,
            message: 'تم إرسال رمز التحقق بنجاح',
            userId: userId,
            previewUrl: previewUrl || null,
            testCode: code // للاختبار فقط
        });
        
    } catch (error) {
        console.error('❌ خطأ في الإرسال:', error);
        res.status(500).json({ 
            error: 'فشل في إرسال الإيميل',
            details: error.message 
        });
    }
});

// تحقق من الرمز
app.post('/verify-code', (req, res) => {
    try {
        const { userId, code } = req.body;
        
        const user = users.get(userId);
        const codeData = codes.get(userId);
        
        if (!user || !codeData) {
            return res.status(400).json({ error: 'بيانات غير صحيحة' });
        }
        
        if (Date.now() > codeData.expires) {
            return res.status(400).json({ error: 'انتهت صلاحية الرمز' });
        }
        
        if (codeData.code !== code) {
            return res.status(400).json({ error: 'رمز التحقق غير صحيح' });
        }
        
        // تأكيد المستخدم
        user.verified = true;
        users.set(userId, user);
        codes.delete(userId);
        
        console.log(`✅ تم تأكيد: ${user.email}`);
        
        res.json({
            success: true,
            message: 'تم التحقق بنجاح!',
            user: user
        });
        
    } catch (error) {
        console.error('خطأ في التحقق:', error);
        res.status(500).json({ error: 'خطأ في الخادم' });
    }
});

// بدء الخادم
app.listen(PORT, async () => {
    console.log(`🚀 خادم الإيميل البسيط يعمل على المنفذ ${PORT}`);
    console.log(`🌐 افتح: http://localhost:${PORT}`);
    
    const isRealEmail = await setupEmail();
    
    if (isRealEmail) {
        console.log('📧 جاهز لإرسال إيميلات حقيقية!');
    } else {
        console.log('📧 وضع الاختبار - ستحصل على روابط معاينة');
    }
});