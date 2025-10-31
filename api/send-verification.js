// API endpoint لإرسال رمز التحقق عبر الإيميل
// يمكن استخدامه مع Vercel Functions أو أي خدمة أخرى

export default async function handler(req, res) {
    if (req.method !== 'POST') {
        return res.status(405).json({ error: 'Method not allowed' });
    }

    const { email, code, language = 'ar' } = req.body;

    if (!email || !code) {
        return res.status(400).json({ error: 'Email and code are required' });
    }

    try {
        // هنا يمكن إضافة خدمة إرسال الإيميل الحقيقية
        // مثل SendGrid, Nodemailer, AWS SES, إلخ
        
        const emailTemplates = {
            ar: {
                subject: 'رمز التحقق - Secure Guardian Pro',
                body: `
                    <div dir="rtl" style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="text-align: center; margin-bottom: 30px;">
                            <h1 style="color: #667eea;">🛡️ Secure Guardian Pro</h1>
                            <h2 style="color: #2d3748;">رمز التحقق</h2>
                        </div>
                        
                        <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center;">
                            <p style="font-size: 18px; margin-bottom: 20px;">رمز التحقق الخاص بك:</p>
                            <div style="font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; margin: 20px 0;">
                                ${code}
                            </div>
                            <p style="color: #666; font-size: 14px;">هذا الرمز صالح لمدة 10 دقائق</p>
                        </div>
                        
                        <div style="margin-top: 30px; text-align: center; color: #666; font-size: 12px;">
                            <p>إذا لم تطلب هذا الرمز، يرجى تجاهل هذا الإيميل</p>
                            <p>© 2024 Secure Guardian Pro - نظام الحماية الذكي المتقدم</p>
                        </div>
                    </div>
                `
            },
            en: {
                subject: 'Verification Code - Secure Guardian Pro',
                body: `
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="text-align: center; margin-bottom: 30px;">
                            <h1 style="color: #667eea;">🛡️ Secure Guardian Pro</h1>
                            <h2 style="color: #2d3748;">Verification Code</h2>
                        </div>
                        
                        <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center;">
                            <p style="font-size: 18px; margin-bottom: 20px;">Your verification code:</p>
                            <div style="font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; margin: 20px 0;">
                                ${code}
                            </div>
                            <p style="color: #666; font-size: 14px;">This code is valid for 10 minutes</p>
                        </div>
                        
                        <div style="margin-top: 30px; text-align: center; color: #666; font-size: 12px;">
                            <p>If you didn't request this code, please ignore this email</p>
                            <p>© 2024 Secure Guardian Pro - Advanced Smart Security System</p>
                        </div>
                    </div>
                `
            },
            el: {
                subject: 'Κωδικός Επαλήθευσης - Secure Guardian Pro',
                body: `
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="text-align: center; margin-bottom: 30px;">
                            <h1 style="color: #667eea;">🛡️ Secure Guardian Pro</h1>
                            <h2 style="color: #2d3748;">Κωδικός Επαλήθευσης</h2>
                        </div>
                        
                        <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; text-align: center;">
                            <p style="font-size: 18px; margin-bottom: 20px;">Ο κωδικός επαλήθευσής σας:</p>
                            <div style="font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; margin: 20px 0;">
                                ${code}
                            </div>
                            <p style="color: #666; font-size: 14px;">Αυτός ο κωδικός ισχύει για 10 λεπτά</p>
                        </div>
                        
                        <div style="margin-top: 30px; text-align: center; color: #666; font-size: 12px;">
                            <p>Εάν δεν ζητήσατε αυτόν τον κωδικό, παρακαλώ αγνοήστε αυτό το email</p>
                            <p>© 2024 Secure Guardian Pro - Προηγμένο Έξυπνο Σύστημα Ασφαλείας</p>
                        </div>
                    </div>
                `
            }
        };

        const template = emailTemplates[language] || emailTemplates['en'];

        // محاكاة إرسال الإيميل (يجب استبدالها بخدمة حقيقية)
        console.log(`📧 إرسال إيميل إلى: ${email}`);
        console.log(`🔑 رمز التحقق: ${code}`);
        console.log(`🌍 اللغة: ${language}`);
        console.log(`📝 الموضوع: ${template.subject}`);

        // هنا يمكن إضافة كود إرسال الإيميل الحقيقي
        // مثال مع Nodemailer:
        /*
        const nodemailer = require('nodemailer');
        
        const transporter = nodemailer.createTransporter({
            service: 'gmail', // أو أي خدمة أخرى
            auth: {
                user: process.env.EMAIL_USER,
                pass: process.env.EMAIL_PASS
            }
        });

        await transporter.sendMail({
            from: process.env.EMAIL_USER,
            to: email,
            subject: template.subject,
            html: template.body
        });
        */

        // للاختبار، نعتبر الإرسال ناجح دائماً
        const success = true; // في الواقع، يجب أن يعتمد على نتيجة إرسال الإيميل

        return res.status(200).json({
            success,
            message: success ? 'Email sent successfully' : 'Failed to send email',
            code: process.env.NODE_ENV === 'development' ? code : undefined // إظهار الرمز في وضع التطوير فقط
        });

    } catch (error) {
        console.error('خطأ في إرسال الإيميل:', error);
        return res.status(500).json({
            success: false,
            error: 'Internal server error'
        });
    }
}