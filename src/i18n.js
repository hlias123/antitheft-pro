import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

import ar from './locales/ar.json';
import en from './locales/en.json';
import el from './locales/el.json';

const resources = { ar, en, el };

i18n
  .use(initReactI18next)
  .init({
    resources,
    lng: 'ar',          // اللغة الافتراضية
    fallbackLng: 'en',
    interpolation: { escapeValue: false },
    react: { useSuspense: false }
  });

export default i18n;