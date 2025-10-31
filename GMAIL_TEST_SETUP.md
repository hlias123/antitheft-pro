# 📧 إعداد Gmail للاختبار الحقيقي

## 🎯 **الهدف: إرسال إيميل حقيقي للاختبار**

### 📋 **الخطوات:**

#### **1. إنشاء حساب Gmail للاختبار:**
- اذهب إلى [Gmail](https://accounts.google.com/signup)
- أنشئ حساب جديد: `secureguardiantest2024@gmail.com`
- كلمة المرور قوية

#### **2. تفعيل 2-Step Verification:**
1. اذهب إلى [Google Account Security](https://myaccount.google.com/security)
2. اختر **2-Step Verification**
3. اتبع الخطوات لتفعيلها

#### **3. إنشاء App Password:**
1. في نفس صفحة Security
2. اختر **App passwords**
3. اختر **Mail** و **Other (Custom name)**
4. اكتب "Secure Guardian Test"
5. اضغط **Generate**
6. انسخ كلمة المرور (16 رقم)

#### **4. تحديث .env:**
```env
EMAIL_USER=secureguardiantest2024@gmail.com
EMAIL_PASS=your-16-digit-app-password
```

#### **5. إعادة تشغيل الخادم:**
```bash
npm start
```

---

## 🧪 **اختبار الإرسال:**

### **بعد الإعداد:**
1. أنشئ حساب جديد في التطبيق
2. ستحصل على إيميل حقيقي
3. استخدم الرمز للتحقق

### **النتيجة المتوقعة:**
```
✅ Gmail service configured and tested successfully
📧 Real emails will be sent to users
📧 Verification code for user@example.com: 1234
✅ Email sent successfully: <message-id>
```

---

## 🎨 **شكل الإيميل:**

سيصل إيميل احترافي بهذا الشكل:

```
🛡️ Secure Guardian Pro
نظام الحماية الذكي المتقدم

رمز التحقق الخاص بك

    1234

⏰ هذا الرمز صالح لمدة 10 دقائق فقط

🔒 إذا لم تطلب هذا الرمز، يرجى تجاهل هذه الرسالة
```

---

## ✅ **التأكيد:**
بعد هذا الإعداد، ستحصل على إرسال إيميل حقيقي فوري لكل مستخدم جديد!