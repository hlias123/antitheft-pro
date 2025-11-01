const express = require('express');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());
app.use(express.static(__dirname));

// قاعدة بيانات مؤقتة
let userSessions = {};
let registeredUsers = {
    'hlia.hlias123@gmail.com': {
        password: 'demo123',
        pin: '1234',
        createdAt: new Date(),
        isActive: true
    }
};

// الصفحة الرئيسية
app.get('/', (req, res) => {
    res.send(`
<!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AntiTheft Pro - Fixed</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 30px; }
        .pin-section { background: #f8f9fa; padding: 20px; border-radius: 10px; margin: 20px 0; text-align: center; }
        .pin-input { padding: 15px; font-size: 24px; text-align: center; letter-spacing: 10px; border: 2px solid #ddd; border-radius: 10px; margin: 10px; width: 200px; }
        .btn { padding: 15px 30px; background: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer; font-size: 16px; margin: 10px; }
        .btn:hover { background: #0056b3; }
        .status { margin: 15px 0; padding: 10px; border-radius: 5px; }
        .success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .info { background: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🛡️ AntiTheft Pro - إصدار مُصحح</h1>
            <p>نظام مكافحة السرقة المتقدم</p>
        </div>

        <div class="pin-section">
            <h3>🔐 تأكيد هويتك بـ PIN</h3>
            <p>أدخل PIN المكون من 4 أرقام لتأكيد أنك صاحب التطبيق</p>
            <input type="password" class="pin-input" id="pinInput" placeholder="****" maxlength="4" 
                   onkeypress="if(event.key==='Enter') verifyPIN()" 
                   oninput="this.value = this.value.replace(/[^0-9]/g, '')">
            <br>
            <button class="btn" onclick="verifyPIN()">تأكيد PIN</button>
            <div id="pinStatus"></div>
        </div>

        <div class="info status">
            <strong>🧪 بيانات الاختبار:</strong><br>
            البريد الإلكتروني: hlia.hlias123@gmail.com<br>
            كلمة المرور: demo123<br>
            PIN الصحيح: 1234
        </div>

        <div class="info status">
            <strong>🔍 اختبر الآن:</strong><br>
            • جرب PIN: 1234 ← يجب أن يعمل ✅<br>
            • جرب PIN: abcd ← يجب أن يظهر خطأ ❌<br>
            • جرب PIN: 12ab ← يجب أن يظهر خطأ ❌
        </div>
    </div>

    <script>
        let sessionId = 'demo-session-' + Date.now();

        // دالة تأكيد PIN المُصححة
        async function verifyPIN() {
            const pin = document.getElementById('pinInput').value;
            const statusDiv = document.getElementById('pinStatus');
            
            console.log('Testing PIN:', pin);
            
            // تنظيف الرسائل السابقة
            statusDiv.innerHTML = '';
            
            if (!pin || pin.length !== 4) {
                statusDiv.innerHTML = '<div class="error status">يرجى إدخال PIN مكون من 4 أرقام</div>';
                return;
            }
            
            // الكود المُصحح: استخدام \\d بدلاً من d
            if (!/^\\d{4}$/.test(pin)) {
                statusDiv.innerHTML = '<div class="error status">❌ PIN يجب أن يحتوي على أرقام فقط</div>';
                return;
            }
            
            statusDiv.innerHTML = '<div class="info status">جاري التحقق من PIN...</div>';
            
            try {
                const response = await fetch('/api/auth/verify-pin', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        sessionId: sessionId,
                        pin: pin
                    })
                });
                
                const data = await response.json();
                
                if (data.success) {
                    statusDiv.innerHTML = '<div class="success status">✅ تم تأكيد PIN بنجاح!</div>';
                    document.querySelector('.pin-section').style.background = '#e8f5e8';
                    document.getElementById('pinInput').disabled = true;
                    document.querySelector('.pin-section button').disabled = true;
                    document.querySelector('.pin-section button').textContent = 'تم التأكيد ✅';
                } else {
                    statusDiv.innerHTML = \`<div class="error status">❌ \${data.message}</div>\`;
                    document.getElementById('pinInput').value = '';
                    document.getElementById('pinInput').focus();
                }
            } catch (error) {
                statusDiv.innerHTML = '<div class="error status">خطأ في التحقق من PIN</div>';
                console.error('PIN verification error:', error);
            }
        }

        // تركيز على حقل PIN عند تحميل الصفحة
        document.getElementById('pinInput').focus();
    </script>
</body>
</html>
    `);
});

// API لتأكيد PIN
app.post('/api/auth/verify-pin', (req, res) => {
    const { sessionId, pin } = req.body;
    
    console.log('PIN verification request:', { sessionId, pin });
    
    if (!sessionId) {
        return res.status(401).json({
            success: false,
            message: 'جلسة غير صالحة'
        });
    }
    
    // التحقق من PIN الصحيح (1234)
    if (pin && pin.length === 4 && /^\\d{4}$/.test(pin)) {
        if (pin === '1234') {
            res.json({
                success: true,
                message: 'تم تأكيد PIN بنجاح'
            });
        } else {
            res.status(400).json({
                success: false,
                message: 'PIN غير صحيح'
            });
        }
    } else {
        res.status(400).json({
            success: false,
            message: 'PIN يجب أن يكون 4 أرقام'
        });
    }
});

app.listen(PORT, () => {
    console.log(\`🚀 الخادم المُصحح يعمل على المنفذ \${PORT}\`);
    console.log(\`🌐 الموقع متاح على: http://localhost:\${PORT}\`);
    console.log('✅ تم إصلاح مشكلة PIN validation');
});