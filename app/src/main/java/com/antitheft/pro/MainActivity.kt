package com.antitheft.pro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.antitheft.pro.activities.LoginActivity
import com.antitheft.pro.activities.PinLockActivity
import com.antitheft.pro.databinding.ActivityMainBinding
import com.antitheft.pro.utils.PreferenceManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var prefManager: PreferenceManager
    
    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            setupMainScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        prefManager = PreferenceManager(this)
        
        checkAuthAndPin()
    }
    
    private fun checkAuthAndPin() {
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            // المستخدم غير مسجل دخول - انتقل لصفحة تسجيل الدخول
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        if (!currentUser.isEmailVerified) {
            // البريد غير محقق - انتقل لصفحة التحقق
            startActivity(Intent(this, com.antitheft.pro.activities.EmailVerificationActivity::class.java))
            finish()
            return
        }
        
        // التحقق من PIN - إذا لم يتم إنشاء PIN بعد
        if (!prefManager.isPinEnabled()) {
            // أول مرة - إنشاء PIN
            startActivity(Intent(this, com.antitheft.pro.activities.SetPinActivity::class.java))
            finish()
            return
        }
        
        // التحقق من PIN
        if (prefManager.isPinEnabled() && !prefManager.isPinVerified()) {
            startActivity(Intent(this, PinLockActivity::class.java))
            finish()
            return
        }
        
        // التحقق من الصلاحيات
        checkPermissions()
    }
    
    private fun checkPermissions() {
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            setupMainScreen()
        }
    }
    
    private fun setupMainScreen() {
        binding.apply {
            // إعداد الواجهة الرئيسية
            textViewWelcome.text = getString(R.string.welcome, auth.currentUser?.displayName ?: auth.currentUser?.email ?: "")
            
            // زر التتبع
            buttonTracking.setOnClickListener {
                startActivity(Intent(this@MainActivity, com.antitheft.pro.activities.TrackingActivity::class.java))
            }
            
            // زر المتسللين
            buttonIntruders.setOnClickListener {
                startActivity(Intent(this@MainActivity, com.antitheft.pro.activities.IntrudersActivity::class.java))
            }
            
            // زر الإعدادات
            buttonSettings.setOnClickListener {
                startActivity(Intent(this@MainActivity, com.antitheft.pro.activities.SettingsActivity::class.java))
            }
            
            // زر تسجيل الخروج
            buttonLogout.setOnClickListener {
                auth.signOut()
                prefManager.setPinVerified(false)
                startActivity(Intent(this@MainActivity, com.antitheft.pro.activities.LoginActivity::class.java))
                finish()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // إعادة التحقق من PIN عند العودة للتطبيق
        if (prefManager.isPinEnabled() && !prefManager.isPinVerified()) {
            startActivity(Intent(this, PinLockActivity::class.java))
            finish()
        }
    }
}
