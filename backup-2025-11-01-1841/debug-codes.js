// أداة تشخيص رموز التحقق
const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const dbPath = path.join(__dirname, 'data', 'guardian.db');
const db = new sqlite3.Database(dbPath);

console.log('🔍 فحص رموز التحقق في قاعدة البيانات...\n');

// عرض جميع رموز التحقق
db.all(`SELECT 
    vc.id,
    vc.user_id,
    vc.code,
    vc.type,
    vc.expires_at,
    vc.used,
    vc.created_at,
    u.email,
    u.name,
    datetime('now') as current_time,
    CASE 
        WHEN vc.expires_at > datetime('now') THEN 'صالح'
        ELSE 'منتهي'
    END as status
FROM verification_codes vc 
LEFT JOIN users u ON vc.user_id = u.id 
ORDER BY vc.created_at DESC`, [], (err, rows) => {
    if (err) {
        console.error('❌ خطأ في قراءة قاعدة البيانات:', err);
        db.close();
        return;
    }

    if (rows.length === 0) {
        console.log('📭 لا توجد رموز تحقق في قاعدة البيانات');
    } else {
        console.log(`📊 عدد رموز التحقق: ${rows.length}\n`);
        
        rows.forEach((row, index) => {
            console.log(`--- رمز ${index + 1} ---`);
            console.log(`🆔 معرف الرمز: ${row.id}`);
            console.log(`👤 المستخدم: ${row.email} (${row.name})`);
            console.log(`🔢 الرمز: ${row.code}`);
            console.log(`📝 النوع: ${row.type}`);
            console.log(`⏰ تاريخ الإنشاء: ${row.created_at}`);
            console.log(`⏳ تاريخ الانتهاء: ${row.expires_at}`);
            console.log(`🕐 التوقيت الحالي: ${row.current_time}`);
            console.log(`✅ مستخدم: ${row.used ? 'نعم' : 'لا'}`);
            console.log(`🚦 الحالة: ${row.status}`);
            
            // حساب الوقت المتبقي
            const expiresAt = new Date(row.expires_at);
            const currentTime = new Date(row.current_time);
            const timeDiff = expiresAt - currentTime;
            const minutesLeft = Math.floor(timeDiff / (1000 * 60));
            const secondsLeft = Math.floor((timeDiff % (1000 * 60)) / 1000);
            
            if (timeDiff > 0) {
                console.log(`⏱️ الوقت المتبقي: ${minutesLeft} دقيقة و ${secondsLeft} ثانية`);
            } else {
                console.log(`⏱️ انتهى منذ: ${Math.abs(minutesLeft)} دقيقة و ${Math.abs(secondsLeft)} ثانية`);
            }
            console.log('');
        });
    }
    
    // عرض المستخدمين
    console.log('👥 المستخدمون في النظام:');
    db.all('SELECT id, email, name, is_verified FROM users ORDER BY created_at DESC', [], (err, users) => {
        if (err) {
            console.error('❌ خطأ في قراءة المستخدمين:', err);
        } else {
            users.forEach(user => {
                console.log(`  - ${user.email} (${user.name}) - موثق: ${user.is_verified ? 'نعم' : 'لا'} - ID: ${user.id}`);
            });
        }
        
        console.log('\n🔧 لاختبار رمز معين، استخدم:');
        console.log('node debug-codes.js test <user_id> <code>');
        
        db.close();
    });
});

// إذا تم تمرير معاملات للاختبار
if (process.argv[2] === 'test' && process.argv[3] && process.argv[4]) {
    const userId = process.argv[3];
    const code = process.argv[4];
    
    console.log(`\n🧪 اختبار الرمز ${code} للمستخدم ${userId}...\n`);
    
    // نفس الاستعلام المستخدم في الخادم
    db.get(`SELECT vc.*, u.name, u.email, u.is_verified 
            FROM verification_codes vc 
            JOIN users u ON vc.user_id = u.id 
            WHERE vc.user_id = ? AND vc.code = ? AND vc.used = 0 AND vc.expires_at > datetime('now')`,
        [userId, code], (err, row) => {
            if (err) {
                console.error('❌ خطأ في الاستعلام:', err);
            } else if (row) {
                console.log('✅ الرمز صالح ومقبول!');
                console.log('📧 البريد:', row.email);
                console.log('👤 الاسم:', row.name);
            } else {
                console.log('❌ الرمز غير صالح أو منتهي الصلاحية');
                
                // فحص إضافي
                db.get('SELECT * FROM verification_codes WHERE user_id = ? AND code = ? AND used = 0', 
                    [userId, code], (err, expiredRow) => {
                        if (expiredRow) {
                            console.log('⏰ الرمز موجود لكنه منتهي الصلاحية');
                            console.log('📅 انتهى في:', expiredRow.expires_at);
                        } else {
                            console.log('🔍 الرمز غير موجود أو مستخدم بالفعل');
                        }
                        db.close();
                    });
                return;
            }
            db.close();
        });
}