# Anti-Theft Pro - Android Application 🛡️

## 📱 نظام حماية متقدم ضد السرقة

تطبيق أندرويد كامل مع 27 لغة أوروبية متكامل مع Firebase

---

## ✅ الميزات المكتملة

### 🔐 الأمان
- ✅ Google Sign In
- ✅ تسجيل بالإيميل + تحقق الهوية (Email Verification)
- ✅ إنشاء PIN عند أول تسجيل دخول
- ✅ PIN Lock عند إطفاء الجهاز وإعادة تشغيله
- ✅ 3 محاولات خاطئة → شاشة سوداء + تصوير تلقائي

### 📍 التتبع
- ✅ TrackingActivity مع Google Maps
- ✅ تتبع GPS في الوقت الفعلي
- ✅ حفظ سجل المواقع في Firestore
- ✅ عرض آخر 10 مواقع على الخريطة
- ✅ تحديث تلقائي للموقع

### 📸 كشف المتسللين
- ✅ IntrudersActivity لعرض الصور
- ✅ CameraService للتصوير التلقائي
- ✅ حفظ الصور في Firebase Storage
- ✅ ربط الصور بالموقع الجغرافي

### ⚙️ الإعدادات
- ✅ SettingsActivity كاملة
- ✅ تفعيل/تعطيل PIN Lock
- ✅ تفعيل/تعطيل Intruder Detection
- ✅ تفعيل/تعطيل Location Tracking
- ✅ تفعيل/تعطيل WiFi Scanner
- ✅ إعداد بريد الإشعارات
- ✅ **تبديل اللغات (27 لغة)!**

### 🌍 اللغات (27 لغة مدعومة)
✅ العربية، الإنجليزية، اليونانية، الألمانية، الفرنسية، الإسبانية، الإيطالية، البرتغالية، الهولندية، البولندية، الرومانية، التشيكية، السويدية، المجرية، الدنماركية، الفنلندية، السلوفاكية، البلغارية، الكرواتية، السلوفينية، الليتوانية، اللاتفية، الإستونية، المالطية، الأيرلندية

**LanguageManager مدمج في الإعدادات!**

---

## 🏗️ البنية المكتملة

### Activities (8):
1. ✅ **MainActivity** - الصفحة الرئيسية
2. ✅ **LoginActivity** - تسجيل الدخول (Email/Google)
3. ✅ **RegisterActivity** - إنشاء حساب جديد
4. ✅ **EmailVerificationActivity** - تحقق من البريد
5. ✅ **SetPinActivity** - إنشاء PIN لأول مرة (مع زر Skip)
6. ✅ **PinLockActivity** - إدخال PIN
7. ✅ **SettingsActivity** - الإعدادات (مع تبديل اللغات!)
8. ✅ **TrackingActivity** - التتبع والخريطة
9. ✅ **IntrudersActivity** - عرض صور المتسللين

### Services (3):
1. ✅ **CameraService** - التصوير التلقائي
2. ✅ **LocationTrackingService** - تتبع الموقع (في Manifest)
3. ✅ **WifiScannerService** - ماسح WiFi (في Manifest)

### Receivers (3):
1. ✅ **PowerButtonReceiver** - عند إطفاء/تشغيل الشاشة
2. ✅ **BootReceiver** - عند إعادة تشغيل الجهاز
3. ✅ **SimChangeReceiver** - عند تغيير الشريحة

### Utils (2):
1. ✅ **PreferenceManager** - إدارة الإعدادات المحلية
2. ✅ **LanguageManager** - تبديل اللغات

### Layouts (9):
1. ✅ activity_main.xml
2. ✅ activity_login.xml
3. ✅ activity_register.xml
4. ✅ activity_email_verification.xml
5. ✅ activity_set_pin.xml (مع زر Skip)
6. ✅ activity_pin_lock.xml
7. ✅ activity_settings.xml
8. ✅ activity_tracking.xml (مع Google Maps)
9. ✅ activity_intruders.xml

---

## 🚀 خطوات التشغيل

### 1️⃣ إضافة google-services.json

**⚠️ مهم جداً!**

1. اذهب إلى [Firebase Console](https://console.firebase.google.com)
2. اختر المشروع الموجود (نفس مشروع الموقع)
3. اذهب إلى: **Project Settings** > **General**
4. في قسم **Your apps**:
   - إذا لم يكن هناك Android app، اضغط **Add app** > **Android**
   - Package name: `com.antitheft.pro`
   - App nickname: `Anti-Theft Pro`
   - اضغط **Register app**
5. حمّل ملف `google-services.json`
6. **ضع الملف في:** `app/google-services.json`

```
AntiTheftPro/
├── app/
│   ├── google-services.json  ← هنا!
│   ├── build.gradle.kts
│   └── src/
```

### 2️⃣ تفعيل Firebase Authentication

في Firebase Console:

1. اذهب إلى: **Authentication** > **Sign-in method**
2. فعّل:
   - ✅ **Email/Password**
   - ✅ **Google**
3. في Google Sign-in:
   - أضف البريد الإلكتروني للدعم
   - احفظ التغييرات

### 3️⃣ تفعيل Firestore

1. اذهب إلى: **Firestore Database**
2. اضغط **Create database**
3. اختر **Production mode**
4. اختر الموقع: **europe-west** (أقرب لليونان)

### 4️⃣ تفعيل Firebase Storage

1. اذهب إلى: **Storage**
2. اضغط **Get started**
3. اختر **Production mode**
4. الموقع نفسه

### 5️⃣ قواعد الأمان (Security Rules)

**Firestore Rules:**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /locations/{locationId} {
      allow read, write: if request.auth != null;
    }
    match /intruders/{intruderId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

**Storage Rules:**
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /intruders/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## 📂 فتح المشروع في Android Studio

### الخطوات:

1. **فك الضغط** عن ملف ZIP
2. **افتح Android Studio**
3. **File** > **Open**
4. اختر مجلد **AntiTheftPro**
5. انتظر **Gradle Sync** (3-10 دقائق)
6. تأكد من وجود **google-services.json** في `app/`
7. **Build** > **Rebuild Project**

### إذا ظهر خطأ:

#### ❌ "google-services.json is missing"
**الحل:** أضف الملف من Firebase Console (الخطوة 1 أعلاه)

#### ❌ "SDK location not found"
**الحل:** 
- **File** > **Settings** > **Android SDK**
- تأكد من تثبيت Android SDK

#### ❌ "Gradle sync failed"
**الحل:**
- **File** > **Invalidate Caches** > **Invalidate and Restart**

---

## 🔧 الإعدادات المطلوبة

### في Firebase Console:

1. **Enable Email Verification:**
   - Authentication > Templates > Email address verification
   - عدّل القالب حسب الحاجة

2. **Configure Authorized Domains:**
   - Authentication > Settings > Authorized domains
   - أضف: `antitheft-multilingual-frontend-production.up.railway.app`

3. **Enable Google Sign-In:**
   - Authentication > Sign-in method > Google
   - Web SDK configuration > Web client ID
   - انسخ **Web client ID** وضعه في:
     - `app/src/main/res/values/strings.xml`
     - `<string name="default_web_client_id">YOUR_WEB_CLIENT_ID</string>`

---

## 📱 التشغيل والاختبار

### تشغيل على جهاز حقيقي:

1. فعّل **Developer Options** على الهاتف
2. فعّل **USB Debugging**
3. وصّل الهاتف بالكمبيوتر
4. في Android Studio: **Run** > **Run 'app'**

### تشغيل على Emulator:

1. **Tools** > **Device Manager**
2. **Create Device**
3. اختر جهاز (مثل: Pixel 6)
4. API Level 24 أو أعلى
5. **Run**

---

## 🧪 اختبار الميزات

### 1. التسجيل والدخول:
- ✅ سجّل حساب جديد
- ✅ تحقق من الإيميل
- ✅ سجّل دخول

### 2. PIN Lock:
- ✅ فعّل PIN من الإعدادات
- ✅ أطفئ الشاشة
- ✅ شغّل الشاشة → يجب أن يظهر PIN

### 3. كشف المتسللين:
- ✅ ادخل PIN خاطئ 3 مرات
- ✅ يجب أن تسود الشاشة
- ✅ يجب أن تبدأ الكاميرا

### 4. التتبع:
- ✅ افتح صفحة Tracking
- ✅ يجب أن يظهر موقعك على الخريطة

---

## 🌐 ربط التطبيق بالموقع

التطبيق والموقع يستخدمان **نفس Firebase project**!

### كيف يعمل:

```
مستخدم يسجل في التطبيق
        ↓
    Firebase Auth
        ↓
    Firestore Database
        ↓
الموقع يقرأ من نفس Database
```

**النتيجة:** ✅ نفس الحساب على التطبيق والموقع!

---

## 📊 البيانات في Firestore

### Collections:

#### `users/{userId}`
```json
{
  "uid": "user_id",
  "email": "user@example.com",
  "name": "User Name",
  "createdAt": "timestamp",
  "lastLogin": "timestamp",
  "emailVerified": true
}
```

#### `locations/{locationId}`
```json
{
  "userId": "user_id",
  "latitude": 37.9838,
  "longitude": 23.7275,
  "timestamp": "timestamp",
  "accuracy": 10.0
}
```

#### `intruders/{intruderId}`
```json
{
  "userId": "user_id",
  "imageUrl": "firebase_storage_url",
  "location": {
    "latitude": 37.9838,
    "longitude": 23.7275
  },
  "timestamp": "timestamp"
}
```

---

## 🔐 الأمان

### Best Practices:

1. ✅ **لا تشارك** `google-services.json` علناً
2. ✅ **لا ترفع** المشروع إلى GitHub مع `google-services.json`
3. ✅ استخدم **.gitignore**:
```
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
google-services.json
```

---

## 🛠️ المتطلبات

- ✅ **Android Studio**: Hedgehog أو أحدث
- ✅ **JDK**: 17
- ✅ **Gradle**: 8.5
- ✅ **Min SDK**: 24 (Android 7.0)
- ✅ **Target SDK**: 34 (Android 14)
- ✅ **Kotlin**: 1.9.22

---

## 📞 الدعم

### مشاكل شائعة:

1. **التطبيق لا يفتح**
   - تأكد من `google-services.json`
   - Rebuild Project

2. **Google Sign In لا يعمل**
   - تأكد من Web client ID في strings.xml
   - تأكد من SHA-1 في Firebase Console

3. **الصلاحيات مرفوضة**
   - اذهب لإعدادات الهاتف > Apps > AntiTheftPro
   - فعّل جميع الصلاحيات

---

## 📝 ملاحظات مهمة

⚠️ **قبل النشر على Google Play:**

1. غيّر `applicationId` في `build.gradle.kts`
2. أنشئ Keystore للتوقيع
3. حدّث `versionCode` و `versionName`
4. اختبر على أجهزة متعددة
5. راجع سياسة خصوصية Google Play

---

## 🎉 جاهز!

التطبيق الآن كامل ومتكامل مع الموقع!

**المستخدمون يمكنهم:**
- ✅ التسجيل في التطبيق أو الموقع
- ✅ استخدام نفس الحساب على الاثنين
- ✅ مزامنة البيانات تلقائياً
- ✅ تتبع الجهاز من أي مكان

---

## 📧 تواصل

لأي استفسارات أو مساعدة، تواصل معنا.

---

**صنع بـ ❤️ للحماية من السرقة**
