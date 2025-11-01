package com.antitheft.pro.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "anti_theft_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_PIN = "pin"
        private const val KEY_PIN_ENABLED = "pin_enabled"
        private const val KEY_PIN_VERIFIED = "pin_verified"
        private const val KEY_INTRUDER_DETECTION = "intruder_detection"
        private const val KEY_LOCATION_TRACKING = "location_tracking"
        private const val KEY_WIFI_SCANNER = "wifi_scanner"
        private const val KEY_NOTIFICATION_EMAIL = "notification_email"
    }
    
    // PIN Management
    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }
    
    fun getPin(): String {
        return prefs.getString(KEY_PIN, "") ?: ""
    }
    
    fun isPinEnabled(): Boolean {
        return prefs.getBoolean(KEY_PIN_ENABLED, false)
    }
    
    fun setPinEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_ENABLED, enabled).apply()
    }
    
    fun isPinVerified(): Boolean {
        return prefs.getBoolean(KEY_PIN_VERIFIED, false)
    }
    
    fun setPinVerified(verified: Boolean) {
        prefs.edit().putBoolean(KEY_PIN_VERIFIED, verified).apply()
    }
    
    // Feature Settings
    fun isIntruderDetectionEnabled(): Boolean {
        return prefs.getBoolean(KEY_INTRUDER_DETECTION, true)
    }
    
    fun setIntruderDetectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_INTRUDER_DETECTION, enabled).apply()
    }
    
    fun isLocationTrackingEnabled(): Boolean {
        return prefs.getBoolean(KEY_LOCATION_TRACKING, true)
    }
    
    fun setLocationTrackingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCATION_TRACKING, enabled).apply()
    }
    
    fun isWifiScannerEnabled(): Boolean {
        return prefs.getBoolean(KEY_WIFI_SCANNER, true)
    }
    
    fun setWifiScannerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WIFI_SCANNER, enabled).apply()
    }
    
    fun getNotificationEmail(): String {
        return prefs.getString(KEY_NOTIFICATION_EMAIL, "") ?: ""
    }
    
    fun setNotificationEmail(email: String) {
        prefs.edit().putString(KEY_NOTIFICATION_EMAIL, email).apply()
    }
}
