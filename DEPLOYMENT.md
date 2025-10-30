# 🚀 دليل النشر / Deployment Guide / Οδηγός Ανάπτυξης

## 🌐 النشر على منصات مختلفة / Deploy to Different Platforms / Ανάπτυξη σε Διαφορετικές Πλατφόρμες

### 1. Vercel (الأسرع والأسهل / Fastest & Easiest / Πιο Γρήγορο & Εύκολο)

#### الطريقة الأولى: من GitHub / Method 1: From GitHub / Μέθοδος 1: Από GitHub
1. ارفع المشروع على GitHub / Push project to GitHub / Ανεβάστε το έργο στο GitHub
2. اذهب إلى / Go to / Πηγαίνετε στο: https://vercel.com/new
3. اختر المستودع / Select repository / Επιλέξτε αποθετήριο: `secure-guardian-pro`
4. اضغط "Deploy" / Click "Deploy" / Κάντε κλικ στο "Deploy"
5. انتظر دقيقتين / Wait 2 minutes / Περιμένετε 2 λεπτά

#### الطريقة الثانية: رابط مباشر / Method 2: Direct Link / Μέθοδος 2: Άμεσος Σύνδεσμος
```
https://vercel.com/new/git/external?repository-url=https://github.com/yourusername/secure-guardian-pro
```

### 2. Railway

1. اذهب إلى / Go to / Πηγαίνετε στο: https://railway.app/new
2. اختر "Deploy from GitHub repo" / Select "Deploy from GitHub repo" / Επιλέξτε "Deploy from GitHub repo"
3. اختر المستودع / Select repository / Επιλέξτε αποθετήριο: `secure-guardian-pro`
4. أضف متغيرات البيئة / Add environment variables / Προσθέστε μεταβλητές περιβάλλοντος:
   ```
   JWT_SECRET=secure-guardian-pro-secret-2024
   PORT=3001
   NODE_ENV=production
   ```

### 3. Render

1. اذهب إلى / Go to / Πηγαίνετε στο: https://render.com
2. اختر "New Web Service" / Select "New Web Service" / Επιλέξτε "New Web Service"
3. اربط GitHub / Connect GitHub / Συνδέστε το GitHub: `secure-guardian-pro`
4. الإعدادات / Settings / Ρυθμίσεις:
   - **Build Command**: `npm install`
   - **Start Command**: `npm start`
   - **Environment**: Node

### 4. Heroku

```bash
# Install Heroku CLI
npm install -g heroku

# Login to Heroku
heroku login

# Create app
heroku create secure-guardian-pro

# Set environment variables
heroku config:set JWT_SECRET=secure-guardian-pro-secret-2024
heroku config:set NODE_ENV=production

# Deploy
git push heroku main
```

### 5. DigitalOcean App Platform

1. اذهب إلى / Go to / Πηγαίνετε στο: https://cloud.digitalocean.com/apps
2. اختر "Create App" / Select "Create App" / Επιλέξτε "Create App"
3. اربط GitHub / Connect GitHub / Συνδέστε το GitHub
4. اختر المستودع / Select repository / Επιλέξτε αποθετήριο

## 🔧 متغيرات البيئة المطلوبة / Required Environment Variables / Απαιτούμενες Μεταβλητές Περιβάλλοντος

### الأساسية / Essential / Βασικές
```env
JWT_SECRET=your-super-secret-key
PORT=3001
NODE_ENV=production
```

### اختيارية / Optional / Προαιρετικές
```env
EMAIL_USER=your-gmail@gmail.com
EMAIL_PASS=your-app-password
GOOGLE_CLIENT_ID=your-google-client-id
DATABASE_PATH=./data/guardian.db
ALLOWED_ORIGINS=https://yourdomain.com
```

## 📋 قائمة التحقق قبل النشر / Pre-Deployment Checklist / Λίστα Ελέγχου Προ-Ανάπτυξης

- ✅ تم تحديث `package.json` / Updated `package.json` / Ενημερώθηκε το `package.json`
- ✅ تم إنشاء `.env.example` / Created `.env.example` / Δημιουργήθηκε το `.env.example`
- ✅ تم إنشاء `.gitignore` / Created `.gitignore` / Δημιουργήθηκε το `.gitignore`
- ✅ تم اختبار المشروع محلياً / Tested project locally / Δοκιμάστηκε το έργο τοπικά
- ✅ تم رفع الكود على GitHub / Pushed code to GitHub / Ανέβηκε ο κώδικας στο GitHub

## 🔍 استكشاف الأخطاء / Troubleshooting / Αντιμετώπιση Προβλημάτων

### مشكلة: الموقع لا يعمل / Issue: Site not working / Πρόβλημα: Ο ιστότοπος δεν λειτουργεί
**الحل / Solution / Λύση:**
1. تحقق من متغيرات البيئة / Check environment variables / Ελέγξτε τις μεταβλητές περιβάλλοντος
2. تحقق من logs / Check logs / Ελέγξτε τα logs
3. تأكد من PORT / Ensure PORT is set / Βεβαιωθείτε ότι το PORT είναι ορισμένο

### مشكلة: قاعدة البيانات / Issue: Database / Πρόβλημα: Βάση δεδομένων
**الحل / Solution / Λύση:**
- قاعدة البيانات SQLite تُنشأ تلقائياً / SQLite database is created automatically / Η βάση δεδομένων SQLite δημιουργείται αυτόματα
- لا حاجة لإعداد إضافي / No additional setup needed / Δεν χρειάζεται επιπλέον ρύθμιση

## 🎯 نصائح للأداء / Performance Tips / Συμβουλές Απόδοσης

1. **استخدم CDN** / **Use CDN** / **Χρησιμοποιήστε CDN**
2. **فعل ضغط gzip** / **Enable gzip compression** / **Ενεργοποιήστε τη συμπίεση gzip**
3. **استخدم HTTPS** / **Use HTTPS** / **Χρησιμοποιήστε HTTPS**
4. **راقب الأداء** / **Monitor performance** / **Παρακολουθήστε την απόδοση**

## 📞 الدعم / Support / Υποστήριξη

إذا واجهت مشاكل في النشر، افتح issue في GitHub مع التفاصيل التالية:
If you encounter deployment issues, open a GitHub issue with the following details:
Εάν αντιμετωπίσετε προβλήματα ανάπτυξης, ανοίξτε ένα GitHub issue με τις ακόλουθες λεπτομέρειες:

- المنصة المستخدمة / Platform used / Πλατφόρμα που χρησιμοποιείται
- رسالة الخطأ / Error message / Μήνυμα σφάλματος
- خطوات إعادة الإنتاج / Steps to reproduce / Βήματα αναπαραγωγής