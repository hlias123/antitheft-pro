package com.antitheft.pro.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class LanguageManager(private val context: Context) {
    
    private val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_LANGUAGE = "selected_language"
        private const val DEFAULT_LANGUAGE = "en"
        
        val SUPPORTED_LANGUAGES = listOf(
            "ar", "en", "el", "de", "fr", "es", "it", "pt", "nl", "pl",
            "ro", "cs", "sv", "hu", "da", "fi", "sk", "bg", "hr", "sl",
            "lt", "lv", "et", "mt", "ga"
        )
    }
    
    fun setLanguage(languageCode: String) {
        if (languageCode !in SUPPORTED_LANGUAGES) {
            return
        }
        
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        applyLanguage(languageCode)
    }
    
    fun getCurrentLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
    
    fun applyLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    fun initializeLanguage() {
        val savedLanguage = getCurrentLanguage()
        applyLanguage(savedLanguage)
    }
}
