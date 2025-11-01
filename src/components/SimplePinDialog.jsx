// src/components/SimplePinDialog.jsx - نسخة بسيطة جداً
import React, { useState } from 'react';
import './PinDialog.css';

const SimplePinDialog = ({ onSuccess, onClose }) => {
  const [pin, setPin] = useState('');
  const [error, setError] = useState('');

  const handleKeyPress = (num) => {
    if (pin.length < 4) {
      const newPin = pin + num;
      console.log('PIN entered:', '*'.repeat(newPin.length));
      setPin(newPin);
      setError('');
      
      // التحقق التلقائي عند 4 أرقام
      if (newPin.length === 4) {
        console.log('Checking PIN...');
        checkPin(newPin);
      }
    }
  };

  const handleDelete = () => {
    setPin(pin.slice(0, -1));
    setError('');
  };

  const checkPin = (pinToCheck) => {
    console.log('━━━━━━━━━━━━━━━━━━━━━━━');
    console.log('🔍 Entered PIN:', pinToCheck);
    console.log('🔑 Correct PIN: 1234');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━');
    
    if (pinToCheck === '1234') {
      console.log('✅ PIN correct!');
      setError('');
      // انتظر قليلاً ثم نفذ onSuccess
      setTimeout(() => {
        onSuccess();
      }, 500);
    } else {
      console.log('❌ PIN wrong!');
      setError('رمز PIN غير صحيح (الصحيح: 1234)');
      setPin('');
    }
  };

  return (
    <div className="pin-overlay">
      <div className="pin-dialog">
        <div className="pin-header">
          <h2>🔐 أدخل رمز PIN</h2>
          <p>رمز PIN المكون من 4 أرقام</p>
          <p style={{ fontSize: '14px', color: '#4CAF50' }}>
            💡 PIN الصحيح: 1234
          </p>
          {onClose && (
            <button 
              className="close-btn" 
              onClick={onClose}
              style={{
                position: 'absolute',
                top: '10px',
                right: '10px',
                background: '#f44336',
                color: 'white',
                border: 'none',
                width: '30px',
                height: '30px',
                borderRadius: '50%',
                cursor: 'pointer',
                fontSize: '18px'
              }}
            >
              ✕
            </button>
          )}
        </div>

        <div className="pin-display">
          {[0, 1, 2, 3].map((i) => (
            <div
              key={i}
              className={`pin-dot ${i < pin.length ? 'filled' : ''}`}
              style={{
                width: '20px',
                height: '20px',
                fontSize: '20px',
                color: i < pin.length ? '#1976d2' : '#ccc',
                transition: 'all 0.3s'
              }}
            >
              {i < pin.length ? '●' : '○'}
            </div>
          ))}
        </div>

        {error && (
          <div 
            className="pin-error"
            style={{
              background: '#ffebee',
              border: '1px solid #f44336',
              color: '#c62828',
              padding: '10px',
              borderRadius: '8px',
              textAlign: 'center',
              marginBottom: '20px',
              fontSize: '14px'
            }}
          >
            ⚠️ {error}
          </div>
        )}

        <div 
          className="pin-keypad"
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(3, 1fr)',
            gap: '15px'
          }}
        >
          {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
            <button
              key={num}
              className="pin-key"
              onClick={() => handleKeyPress(num.toString())}
              style={{
                height: '60px',
                border: '2px solid #e0e0e0',
                borderRadius: '12px',
                background: 'white',
                fontSize: '24px',
                fontWeight: '600',
                cursor: 'pointer',
                transition: 'all 0.2s'
              }}
              onMouseEnter={(e) => {
                e.target.style.background = '#f5f5f5';
                e.target.style.borderColor = '#1976d2';
              }}
              onMouseLeave={(e) => {
                e.target.style.background = 'white';
                e.target.style.borderColor = '#e0e0e0';
              }}
            >
              {num}
            </button>
          ))}
          
          <button 
            className="pin-key empty" 
            disabled
            style={{
              border: 'none',
              background: 'transparent',
              cursor: 'default'
            }}
          ></button>
          
          <button
            className="pin-key"
            onClick={() => handleKeyPress('0')}
            style={{
              height: '60px',
              border: '2px solid #e0e0e0',
              borderRadius: '12px',
              background: 'white',
              fontSize: '24px',
              fontWeight: '600',
              cursor: 'pointer'
            }}
          >
            0
          </button>
          
          <button
            className="pin-key delete"
            onClick={handleDelete}
            style={{
              height: '60px',
              border: '2px solid #e0e0e0',
              borderRadius: '12px',
              background: '#f5f5f5',
              color: '#f44336',
              fontSize: '20px',
              cursor: 'pointer'
            }}
          >
            ⌫
          </button>
        </div>

        {/* زر اختبار سريع */}
        <div style={{ marginTop: '20px', textAlign: 'center' }}>
          <button
            onClick={() => {
              console.log('🧪 Quick test button clicked');
              setPin('1234');
              setTimeout(() => checkPin('1234'), 100);
            }}
            style={{
              padding: '12px 30px',
              background: '#4CAF50',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '16px',
              fontWeight: 'bold'
            }}
          >
            ✅ اضغط هنا للتجربة (1234)
          </button>
        </div>

        {/* معلومات */}
        <div style={{
          marginTop: '20px',
          padding: '15px',
          background: '#e3f2fd',
          borderRadius: '8px',
          fontSize: '13px',
          color: '#1976d2',
          textAlign: 'center'
        }}>
          <div style={{ marginBottom: '5px' }}>
            💡 هذا PIN بسيط للاختبار
          </div>
          <div style={{ fontSize: '12px', opacity: 0.8 }}>
            لا يستخدم Firebase - فقط يقارن مع 1234
          </div>
        </div>
      </div>
    </div>
  );
};

export default SimplePinDialog;
