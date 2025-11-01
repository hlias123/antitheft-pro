package com.antitheft.pro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.antitheft.pro.MainActivity
import com.antitheft.pro.R
import com.antitheft.pro.databinding.ActivitySetPinBinding
import com.antitheft.pro.utils.PreferenceManager

class SetPinActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySetPinBinding
    private lateinit var prefManager: PreferenceManager
    private var enteredPin = ""
    private var confirmingPin = false
    private var firstPin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetPinBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        prefManager = PreferenceManager(this)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.textViewTitle.text = getString(R.string.set_pin)
        binding.textViewSubtitle.text = getString(R.string.enter_pin)
        
        setupPinPad()
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
        
        binding.buttonSkip.setOnClickListener {
            // تخطي إنشاء PIN
            navigateToMain()
        }
    }
    
    private fun onNumberClick(number: String) {
        if (enteredPin.length < 4) {
            enteredPin += number
            updatePinDisplay()
            
            if (enteredPin.length == 4) {
                handlePinComplete()
            }
        }
    }
    
    private fun updatePinDisplay() {
        binding.textViewPin.text = "●".repeat(enteredPin.length) + "○".repeat(4 - enteredPin.length)
    }
    
    private fun handlePinComplete() {
        if (!confirmingPin) {
            // المرة الأولى - حفظ PIN
            firstPin = enteredPin
            confirmingPin = true
            
            binding.textViewSubtitle.text = getString(R.string.confirm_pin)
            enteredPin = ""
            updatePinDisplay()
        } else {
            // المرة الثانية - تأكيد PIN
            if (enteredPin == firstPin) {
                // PIN متطابق - حفظه
                prefManager.setPin(firstPin)
                prefManager.setPinEnabled(true)
                prefManager.setPinVerified(true)
                
                Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show()
                navigateToMain()
            } else {
                // PIN غير متطابق
                Toast.makeText(this, R.string.pin_mismatch, Toast.LENGTH_SHORT).show()
                
                // إعادة البدء
                confirmingPin = false
                firstPin = ""
                enteredPin = ""
                binding.textViewSubtitle.text = getString(R.string.enter_pin)
                updatePinDisplay()
            }
        }
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    
    override fun onBackPressed() {
        // يمكن الرجوع للخلف
        super.onBackPressed()
    }
}
