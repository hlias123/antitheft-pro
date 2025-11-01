package com.antitheft.pro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.antitheft.pro.R
import com.antitheft.pro.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.apply {
            buttonRegister.setOnClickListener {
                val email = editTextEmail.text.toString().trim()
                val password = editTextPassword.text.toString().trim()
                val confirmPassword = editTextConfirmPassword.text.toString().trim()
                
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.error,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                
                if (password != confirmPassword) {
                    Toast.makeText(
                        this@RegisterActivity,
                        R.string.pin_mismatch,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                
                if (password.length < 6) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                
                registerUser(email, password)
            }
            
            textViewLogin.setOnClickListener {
                finish()
            }
        }
    }
    
    private fun registerUser(email: String, password: String) {
        binding.buttonRegister.isEnabled = false
        
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    // إرسال بريد التحقق
                    user.sendEmailVerification()
                        .addOnSuccessListener {
                            // حفظ بيانات المستخدم في Firestore
                            saveUserToFirestore(user.uid, email)
                            
                            Toast.makeText(
                                this,
                                getString(R.string.verification_sent, email),
                                Toast.LENGTH_LONG
                            ).show()
                            
                            // الانتقال لصفحة التحقق
                            startActivity(Intent(this, EmailVerificationActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                binding.buttonRegister.isEnabled = true
            }
    }
    
    private fun saveUserToFirestore(uid: String, email: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "emailVerified" to false
        )
        
        db.collection("users").document(uid)
            .set(userMap)
            .addOnFailureListener { e ->
                // Log error
            }
    }
}
