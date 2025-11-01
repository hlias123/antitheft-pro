import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: "AIzaSyByJSLzNMEkMpdwfLy23q0fL74XdZ6qaO8",
  authDomain: "antitheftpro-e9c0e.firebaseapp.com",
  projectId: "antitheftpro-e9c0e",
  storageBucket: "antitheftpro-e9c0e.appspot.com",
  messagingSenderId: "714591912736",
  appId: "1:714591912736:android:539f6eee2c469cdd594d45"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);