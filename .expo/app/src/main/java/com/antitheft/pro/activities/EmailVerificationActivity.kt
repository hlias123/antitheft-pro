package com.antitheft.pro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.antitheft.pro.MainActivity
import com.antitheft.pro.R
import com.antitheft.pro.databinding.ActivityEmailVerificationBinding
import com.google.firebase.auth.FirebaseAuth

class EmailVerificationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityEmailVerificationBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        
        val user = auth.currentUser
        if (user == null) {
            finish()
            return
        }
        
        binding.textViewEmail.text = getString(R.string.verification_sent, user.email)
        
        binding.buttonResend.setOnClickListener {
            user.sendEmailVerification()
                .addOnSuccessListener {
                    Toast.makeText(this, R.string.check_email, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
        }
        
        binding.buttonContinue.setOnClickListener {
            user.reload().addOnSuccessListener {
                if (user.isEmailVerified) {
                    Toast.makeText(this, R.string.email_verified, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, R.string.check_email, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
