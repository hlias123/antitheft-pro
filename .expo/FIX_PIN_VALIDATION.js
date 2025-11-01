// إصلاح مؤقت لمشكلة PIN validation
// شغل هذا الكود في Console المتصفح

// إعادة تعريف دالة verifyPIN الصحيحة
window.verifyPIN = async function() {
    const pin = document.getElementById('pinInput').value;
    const statusDiv = document.getElementById('pinStatus');
    
    console.log('Verifying PIN (FIXED):', pin, 'SessionId:', sessionId);
    
    if (!sessionId) {
        statusDiv.innerHTML = '<p style="color: red;">خطأ: لا توجد جلسة نشطة. يرجى تسجيل الدخول مرة أخرى</p>';
        setTimeout(() => {
            window.location.href = '/login?lang=ar';
        }, 2000);
        return;
    }
    
    if (!pin || pin.length !== 4) {
        statusDiv.innerHTML = '<p style="color: red;">يرجى إدخال PIN مكون من 4 أرقام</p>';
        return;
    }
    
    // الإصلاح هنا: استخدام \\d بدلاً من d
    if (!/^\d{4}$/.test(pin)) {
        statusDiv.innerHTML = '<p style="color: red;">PIN يجب أن يحتوي على أرقام فقط</p>';
        return;
    }
    
    statusDiv.innerHTML = '<p style="color: blue;">جاري التحقق من PIN...</p>';
    
    try {
        const response = await fetch('/api/auth/verify-pin', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                sessionId: sessionId,
                pin: pin
            })
        });
        
        console.log('Response status:', response.status);
        const data = await response.json();
        console.log('Response data:', data);
        
        if (data.success) {
            isPinVerified = true;
            statusDiv.innerHTML = '<p style="color: green;">✅ تم تأكيد PIN بنجاح!</p>';
            document.querySelector('.pin-section').style.background = '#e8f5e8';
            document.getElementById('pinInput').disabled = true;
            document.querySelector('.pin-section button').disabled = true;
            document.querySelector('.pin-section button').textContent = 'تم التأكيد ✅';
        } else {
            statusDiv.innerHTML = `<p style="color: red;">❌ ${data.message}</p>`;
            document.getElementById('pinInput').value = '';
            document.getElementById('pinInput').focus();
        }
    } catch (error) {
        statusDiv.innerHTML = '<p style="color: red;">خطأ في التحقق من PIN</p>';
        console.error('PIN verification error:', error);
    }
}

console.log('✅ تم إصلاح دالة verifyPIN - جرب الآن!');