package com.antitheft.pro.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.antitheft.pro.R
import com.antitheft.pro.databinding.ActivitySettingsBinding
import com.antitheft.pro.utils.LanguageManager
import com.antitheft.pro.utils.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefManager: PreferenceManager
    private lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefManager = PreferenceManager(this)
        languageManager = LanguageManager(this)
        
        setupUI()
    }
    
    private fun setupUI() {
        // عنوان الصفحة
        supportActionBar?.title = getString(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // تحميل الإعدادات الحالية
        binding.apply {
            switchPinLock.isChecked = prefManager.isPinEnabled()
            switchIntruderDetection.isChecked = prefManager.isIntruderDetectionEnabled()
            switchLocationTracking.isChecked = prefManager.isLocationTrackingEnabled()
            switchWifiScanner.isChecked = prefManager.isWifiScannerEnabled()
            
            val currentEmail = prefManager.getNotificationEmail()
            editTextNotificationEmail.setText(currentEmail)
            
            val currentLanguage = languageManager.getCurrentLanguage()
            textViewCurrentLanguage.text = getLanguageName(currentLanguage)
            
            // PIN Lock
            switchPinLock.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && prefManager.getPin().isEmpty()) {
                    // إنشاء PIN جديد
                    startActivity(Intent(this@SettingsActivity, SetPinActivity::class.java))
                } else {
                    prefManager.setPinEnabled(isChecked)
                }
            }
            
            buttonChangePin.setOnClickListener {
                if (prefManager.isPinEnabled()) {
                    startActivity(Intent(this@SettingsActivity, SetPinActivity::class.java))
                }
            }
            
            // Intruder Detection
            switchIntruderDetection.setOnCheckedChangeListener { _, isChecked ->
                prefManager.setIntruderDetectionEnabled(isChecked)
            }
            
            // Location Tracking
            switchLocationTracking.setOnCheckedChangeListener { _, isChecked ->
                prefManager.setLocationTrackingEnabled(isChecked)
            }
            
            // WiFi Scanner
            switchWifiScanner.setOnCheckedChangeListener { _, isChecked ->
                prefManager.setWifiScannerEnabled(isChecked)
            }
            
            // حفظ بريد الإشعارات
            buttonSaveEmail.setOnClickListener {
                val email = editTextNotificationEmail.text.toString().trim()
                prefManager.setNotificationEmail(email)
                android.widget.Toast.makeText(
                    this@SettingsActivity,
                    R.string.success,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            
            // تغيير اللغة
            buttonChangeLanguage.setOnClickListener {
                showLanguageDialog()
            }
        }
    }
    
    private fun showLanguageDialog() {
        val languages = arrayOf(
            "العربية" to "ar",
            "English" to "en",
            "Ελληνικά" to "el",
            "Deutsch" to "de",
            "Français" to "fr",
            "Español" to "es",
            "Italiano" to "it",
            "Português" to "pt",
            "Nederlands" to "nl",
            "Polski" to "pl",
            "Română" to "ro",
            "Čeština" to "cs",
            "Svenska" to "sv",
            "Magyar" to "hu",
            "Dansk" to "da",
            "Suomi" to "fi",
            "Slovenčina" to "sk",
            "Български" to "bg",
            "Hrvatski" to "hr",
            "Slovenščina" to "sl",
            "Lietuvių" to "lt",
            "Latviešu" to "lv",
            "Eesti" to "et",
            "Malti" to "mt",
            "Gaeilge" to "ga"
        )
        
        val languageNames = languages.map { it.first }.toTypedArray()
        val currentLanguage = languageManager.getCurrentLanguage()
        val currentIndex = languages.indexOfFirst { it.second == currentLanguage }
        
        AlertDialog.Builder(this)
            .setTitle(R.string.language)
            .setSingleChoiceItems(languageNames, currentIndex) { dialog, which ->
                val selectedLanguage = languages[which].second
                languageManager.setLanguage(selectedLanguage)
                
                // إعادة تشغيل Activity
                recreate()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "ar" -> "العربية"
            "en" -> "English"
            "el" -> "Ελληνικά"
            "de" -> "Deutsch"
            "fr" -> "Français"
            "es" -> "Español"
            "it" -> "Italiano"
            "pt" -> "Português"
            "nl" -> "Nederlands"
            "pl" -> "Polski"
            "ro" -> "Română"
            "cs" -> "Čeština"
            "sv" -> "Svenska"
            "hu" -> "Magyar"
            "da" -> "Dansk"
            "fi" -> "Suomi"
            "sk" -> "Slovenčina"
            "bg" -> "Български"
            "hr" -> "Hrvatski"
            "sl" -> "Slovenščina"
            "lt" -> "Lietuvių"
            "lv" -> "Latviešu"
            "et" -> "Eesti"
            "mt" -> "Malti"
            "ga" -> "Gaeilge"
            else -> "English"
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
