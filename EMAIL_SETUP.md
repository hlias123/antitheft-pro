# 📧 إعداد نظام إرسال الإيميل

## 🎯 نظرة عامة
يحتوي التطبيق على نظام إرسال إيميل متكامل لرموز التحقق بثلاث لغات:
- العربية (RTL)
- الإنجليزية (LTR) 
- اليونانية (LTR)

## 🔧 طرق الإعداد

### 1. استخدام Gmail SMTP
```javascript
// في ملف api/send-verification.js
const nodemailer = require('nodemailer');

const transporter = nodemailer.createTransporter({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER, // your-email@gmail.com
        pass: process.env.EMAIL_PASS  // your-app-password
    }
});
```

### 2. استخدام SendGrid
```javascript
const sgMail = require('@sendgrid/mail');
sgMail.setApiKey(process.env.SENDGRID_API_KEY);

const msg = {
    to: email,
    from: 'noreply@yourapp.com',
    subject: template.subject,
    html: template.body,
};

await sgMail.send(msg);
```

### 3. استخدام AWS SES
```javascript
const AWS = require('aws-sdk');
const ses = new AWS.SES({ region: 'us-east-1' });

const params = {
    Source: 'noreply@yourapp.com',
    Destination: { ToAddresses: [email] },
    Message: {
        Subject: { Data: template.subject },
        Body: { Html: { Data: template.body } }
    }
};

await ses.sendEmail(params).promise();
```

## 🌍 قوالب الإيميل المترجمة

### العربية
- **الاتجاه**: من اليمين لليسار (RTL)
- **الخط**: Arial مع دعم العربية
- **الموضوع**: "رمز التحقق - Secure Guardian Pro"

### الإنجليزية
- **الاتجاه**: من اليسار لليمين (LTR)
- **الخط**: Arial
- **الموضوع**: "Verification Code - Secure Guardian Pro"

### اليونانية
- **الاتجاه**: من اليسار لليمين (LTR)
- **الخط**: Arial مع دعم اليونانية
- **الموضوع**: "Κωδικός Επαλήθευσης - Secure Guardian Pro"

## ⚙️ متغيرات البيئة المطلوبة

إنشئ ملف `.env` في جذر المشروع:

```env
# Gmail SMTP
EMAIL_USER=your-email@gmail.com
EMAIL_PASS=your-app-password

# أو SendGrid
SENDGRID_API_KEY=your-sendgrid-api-key

# أو AWS SES
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1

# إعدادات عامة
NODE_ENV=production
```

## 🚀 النشر على Vercel

1. **رفع المشروع إلى GitHub**
2. **ربط Vercel بـ GitHub**
3. **إضافة متغيرات البيئة في Vercel Dashboard**
4. **النشر التلقائي**

## 🧪 الاختبار

### رموز الاختبار (تعمل دائماً):
- `1234`
- `0000` 
- `9999`

### في وضع التطوير:
- يظهر الرمز المولد في Console
- يمكن رؤية تفاصيل الإيميل في الـ logs

## 📱 مميزات النظام

✅ **إرسال إيميل حقيقي** مع رمز عشوائي
✅ **قوالب مترجمة** بثلاث لغات
✅ **تصميم متجاوب** للإيميل
✅ **رموز اختبار** للتطوير السريع
✅ **معالجة الأخطاء** الشاملة
✅ **أمان عالي** مع انتهاء صلاحية الرمز

## 🔒 الأمان

- **انتهاء صلاحية الرمز**: 10 دقائق
- **رموز عشوائية**: 4 أرقام (1000-9999)
- **تشفير الاتصال**: HTTPS/TLS
- **عدم تخزين الرموز**: في قاعدة البيانات

## 📞 الدعم

للمساعدة في الإعداد أو حل المشاكل، يرجى:
1. التحقق من الـ Console للأخطاء
2. مراجعة متغيرات البيئة
3. اختبار الاتصال بخدمة الإيميل

---

**🎉 نظام إرسال الإيميل جاهز للاستخدام مع دعم كامل للغات الثلاث!**