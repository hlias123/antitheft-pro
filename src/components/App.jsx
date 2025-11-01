import SimplePinDialog from './components/SimplePinDialog';
import { useState } from 'react';
function App() {
  const [pinVerified, setPinVerified] = useState(false);

  if (!pinVerified) {
    return (
      <SimplePinDialog 
        onSuccess={() => {
          console.log('✅ PIN verified!');
          setPinVerified(true);
        }}
      />
    );
  }

  // باقي كود App العادي...
  return (
    // ... المحتوى الطبيعي
  );
}