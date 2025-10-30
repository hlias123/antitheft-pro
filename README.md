# 🛡️ Secure Guardian Pro - حارس الأمان المتقدم

## 🌟 المميزات / Features / Χαρακτηριστικά

### 🌐 دعم متعدد اللغات / Multi-Language Support / Πολυγλωσσική Υποστήριξη
- **العربية** مع دعم RTL كامل / **Arabic** with full RTL support / **Αραβικά** με πλήρη υποστήριξη RTL
- **الإنجليزية** / **English** / **Αγγλικά**
- **اليونانية** / **Greek** / **Ελληνικά**
- تبديل فوري بين اللغات / Instant language switching / Άμεση εναλλαγή γλώσσας

### 🔐 نظام أمان متقدم / Advanced Security System / Προηγμένο Σύστημα Ασφαλείας
- تسجيل دخول آمن / Secure login / Ασφαλής σύνδεση
- دعم Google OAuth / Google OAuth support / Υποστήριξη Google OAuth
- تشفير كلمات المرور / Password encryption / Κρυπτογράφηση κωδικών
- جلسات آمنة / Secure sessions / Ασφαλείς συνεδρίες

### 📱 مراقبة شاملة / Comprehensive Monitoring / Ολοκληρωμένη Παρακολούθηση
- 🗺️ تتبع الموقع في الوقت الفعلي / Real-time location tracking / Παρακολούθηση τοποθεσίας σε πραγματικό χρόνο
- 📷 مراقبة الكاميرا المباشرة / Live camera monitoring / Ζωντανή παρακολούθηση κάμερας
- 🔋 مراقبة حالة البطارية / Battery status monitoring / Παρακολούθηση κατάστασης μπαταρίας
- 🔔 تنبيهات فورية / Instant alerts / Άμεσες ειδοποιήσεις

## 🚀 البدء السريع / Quick Start / Γρήγορη Έναρξη

### التثبيت / Installation / Εγκατάσταση

```bash
# Clone the repository
git clone https://github.com/hlias123/antitheft-pro.git
cd antitheft-pro

# Install dependencies
npm install

# Start the server
npm start
```

### الاستخدام / Usage / Χρήση

1. افتح المتصفح واذهب إلى / Open browser and go to / Ανοίξτε το πρόγραμμα περιήγησης και πηγαίνετε στο:
   ```
   http://localhost:3001
   ```

2. استخدم بيانات التجربة / Use demo credentials / Χρησιμοποιήστε τα διαπιστευτήρια δοκιμής:
   ```
   📧 Email: user@test.com
   🔒 Password: 12345678
   ```

## 🛠️ التقنيات المستخدمة / Technologies Used / Τεχνολογίες που Χρησιμοποιούνται

- **Backend**: Node.js, Express.js
- **Database**: SQLite3
- **Authentication**: JWT, bcrypt, Google OAuth
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Email**: Nodemailer
- **Security**: CORS, Input validation

## 📁 هيكل المشروع / Project Structure / Δομή Έργου

```
secure-guardian-pro/
├── server.js              # الخادم الرئيسي / Main server / Κύριος διακομιστής
├── package.json           # تبعيات المشروع / Project dependencies / Εξαρτήσεις έργου
├── public/
│   └── app.html          # الواجهة الأمامية / Frontend interface / Διεπαφή frontend
├── data/
│   └── guardian.db       # قاعدة البيانات / Database / Βάση δεδομένων
└── README.md             # هذا الملف / This file / Αυτό το αρχείο
```

## 🔧 الإعداد / Configuration / Διαμόρφωση

### متغيرات البيئة / Environment Variables / Μεταβλητές Περιβάλλοντος

إنشئ ملف `.env` / Create `.env` file / Δημιουργήστε αρχείο `.env`:

```env
# Server Configuration
PORT=3001
NODE_ENV=production

# Security
JWT_SECRET=your-super-secret-jwt-key

# Email Configuration (Optional)
EMAIL_USER=your-gmail@gmail.com
EMAIL_PASS=your-app-password

# Google OAuth (Optional)
GOOGLE_CLIENT_ID=your-google-client-id

# Database
DATABASE_PATH=./data/guardian.db
```

## 🌐 API Endpoints

### Authentication / المصادقة / Πιστοποίηση
- `POST /auth/register` - تسجيل مستخدم جديد / Register new user / Εγγραφή νέου χρήστη
- `POST /auth/login` - تسجيل الدخول / User login / Σύνδεση χρήστη
- `POST /auth/verify` - التحقق من الرمز / Verify MFA code / Επαλήθευση κωδικού MFA
- `POST /auth/google` - تسجيل دخول Google / Google OAuth login / Σύνδεση Google OAuth
- `GET /auth/profile` - الملف الشخصي / User profile / Προφίλ χρήστη
- `POST /auth/logout` - تسجيل الخروج / User logout / Αποσύνδεση χρήστη

### System / النظام / Σύστημα
- `GET /health` - فحص حالة النظام / Health check / Έλεγχος υγείας
- `GET /api` - وثائق API / API documentation / Τεκμηρίωση API

## 🔒 الأمان / Security / Ασφάλεια

- ✅ تشفير كلمات المرور باستخدام bcrypt / Password hashing with bcrypt / Κρυπτογράφηση κωδικών με bcrypt
- ✅ JWT tokens للجلسات الآمنة / JWT tokens for secure sessions / JWT tokens για ασφαλείς συνεδρίες
- ✅ CORS protection / حماية CORS / Προστασία CORS
- ✅ Input validation / التحقق من المدخلات / Επικύρωση εισόδου
- ✅ SQL injection prevention / منع حقن SQL / Πρόληψη SQL injection

## 📱 الاستجابة / Responsive Design / Responsive Design

- ✅ تصميم متجاوب لجميع الأجهزة / Responsive design for all devices / Responsive design για όλες τις συσκευές
- ✅ دعم الهواتف المحمولة / Mobile support / Υποστήριξη κινητών
- ✅ دعم الأجهزة اللوحية / Tablet support / Υποστήριξη tablet

## 🤝 المساهمة / Contributing / Συνεισφορά

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 الترخيص / License / Άδεια

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 الدعم / Support / Υποστήριξη

إذا كان لديك أي أسئلة أو مشاكل، يرجى فتح issue في GitHub.
If you have any questions or issues, please open an issue on GitHub.
Εάν έχετε ερωτήσεις ή προβλήματα, παρακαλώ ανοίξτε ένα issue στο GitHub.

---

Made with ❤️ by Secure Guardian Team