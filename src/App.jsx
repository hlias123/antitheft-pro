import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { getAuth, onAuthStateChanged, signInAnonymously } from 'firebase/auth';
import { doc, getDoc } from 'firebase/firestore';
import { db } from './firebaseConfig';
import './App.css'; // يمكن تركه فارغاً أو حذفه

export default function App() {
  const { t, i18n } = useTranslation();
  const [pin, setPin] = useState('');
  const [status, setStatus] = useState('');
  const [correctPin, setCorrectPin] = useState(null);
  const [user, setUser] = useState(null);

  // تسجيل دخول تجريبي (للاختبار فقط)
  useEffect(() => {
    const auth = getAuth();
    onAuthStateChanged(auth, async (u) => {
      if (u) {
        setUser(u);
        const snap = await getDoc(doc(db, 'users', u.uid));
        if (snap.exists()) setCorrectPin(snap.data().pin);
        else setStatus(t('errors.user_not_found'));
      } else {
        // دخول مجهول مؤقت
        await signInAnonymously(auth);
      }
    });
  }, []);

  const handleConfirm = async () => {
    if (!/^\d{4}$/.test(pin)) {
      setStatus(t('pin.invalid_format'));
      return;
    }
    if (pin === correctPin) {
      setStatus(t('pin.pin_verified'));
    } else {
      setStatus(t('pin.wrong_pin'));
      setPin('');
    }
  };

  return (
    <div className="pin-section">
      <h2>{t('pin.pin_confirmation')}</h2>
      <p>{t('pin.enter_pin')}</p>

      <input
        type="password"
        inputMode="numeric"
        maxLength={4}
        value={pin}
        onChange={(e) => setPin(e.target.value)}
        placeholder="••••"
      />

      {status && <p style={{ color: status.includes('❌') ? 'red' : 'green' }}>{status}</p>}

      <button onClick={handleConfirm}>{t('common.confirm')}</button>

      <div style={{ marginTop: 10 }}>
        <button onClick={() => i18n.changeLanguage('ar')}>عربي</button>
        <button onClick={() => i18n.changeLanguage('en')}>English</button>
        <button onClick={() => i18n.changeLanguage('el')}>Ελληνικά</button>
      </div>
    </div>
  );
}