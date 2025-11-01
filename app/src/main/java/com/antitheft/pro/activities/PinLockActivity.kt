package com.antitheft.pro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.antitheft.pro.MainActivity
import com.antitheft.pro.R
import com.antitheft.pro.databinding.ActivityPinLockBinding
import com.antitheft.pro.services.CameraService
import com.antitheft.pro.utils.BiometricHelper
import com.antitheft.pro.utils.PreferenceManager

class PinLockActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPinLockBinding
    private lateinit var prefManager: PreferenceManager
    private lateinit var biometricHelper: BiometricHelper
    private var enteredPin = ""
    private var attempts = 0
    private val maxAttempts = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinLockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefManager = PreferenceManager(this)
        biometricHelper = BiometricHelper(this)
        
        setupPinPad()
        setupBiometric()
    }
    
    private fun setupBiometric() {
        // إذا كانت البصمة متاحة، اعرض زر البصمة
        if (biometricHelper.isBiometricAvailable()) {
            binding.buttonFingerprint.visibility = android.view.View.VISIBLE
            binding.buttonFingerprint.setOnClickListener {
                authenticateWithBiometric()
            }
            
            // عرض البصمة تلقائياً عند فتح الشاشة
            authenticateWithBiometric()
        } else {
            binding.buttonFingerprint.visibility = android.view.View.GONE
        }
    }
    
    private fun authenticateWithBiometric() {
        biometricHelper.authenticate(
            onSuccess = {
                // البصمة صحيحة
                prefManager.setPinVerified(true)
                Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            },
            onError = { error ->
                // خطأ في البصمة
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    private fun setupPinPad() {
        val buttons = listOf(
            binding.button0, binding.button1, binding.button2,
            binding.button3, binding.button4, binding.button5,
            binding.button6, binding.button7, binding.button8,
            binding.button9
        )
        
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                onNumberClick(index.toString())
            }
        }
        
        binding.buttonClear.setOnClickListener {
            enteredPin = ""
            updatePinDisplay()
        }
        
        binding.buttonDelete.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
                updatePinDisplay()
            }
        }
    }
    
    private fun onNumberClick(number: String) {
        if (enteredPin.length < 4) {
            enteredPin += number
            updatePinDisplay()
            
            if (enteredPin.length == 4) {
                verifyPin()
            }
        }
    }
    
    private fun updatePinDisplay() {
        binding.textViewPin.text = "●".repeat(enteredPin.length) + "○".repeat(4 - enteredPin.length)
    }
    
    private fun verifyPin() {
        val savedPin = prefManager.getPin()
        
        if (enteredPin == savedPin) {
            // PIN صحيح
            prefManager.setPinVerified(true)
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // PIN خاطئ
            attempts++
            val remaining = maxAttempts - attempts
            
            if (remaining > 0) {
                Toast.makeText(
                    this,
                    getString(R.string.wrong_pin, remaining),
                    Toast.LENGTH_SHORT
                ).show()
                enteredPin = ""
                updatePinDisplay()
            } else {
                // 3 محاولات فاشلة - تفعيل الكاميرا وشاشة سوداء
                activateIntruderMode()
            }
        }
    }
    
    private fun activateIntruderMode() {
        Toast.makeText(this, R.string.too_many_attempts, Toast.LENGTH_LONG).show()
        
        // تحويل الشاشة للأسود
        window.decorView.setBackgroundColor(android.graphics.Color.BLACK)
        binding.root.alpha = 0f
        
        // بدء خدمة الكاميرا
        Intent(this, CameraService::class.java).also { intent ->
            startService(intent)
        }
        
        // يمكن إضافة منطق إضافي هنا مثل إرسال موقع فوري
    }
    
    override fun onBackPressed() {
        // منع الرجوع للخلف
        // لا تفعل شيء
    }
}
