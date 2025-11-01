package com.antitheft.pro.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.antitheft.pro.MainActivity
import com.antitheft.pro.R
import com.antitheft.pro.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Toast.makeText(this, getString(R.string.error) + ": ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        // إعداد Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.apply {
            // تسجيل الدخول بالإيميل
            buttonLogin.setOnClickListener {
                val email = editTextEmail.text.toString().trim()
                val password = editTextPassword.text.toString().trim()
                
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.error,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                
                loginWithEmail(email, password)
            }
            
            // تسجيل الدخول بـ Google
            buttonGoogleSignIn.setOnClickListener {
                signInWithGoogle()
            }
            
            // إنشاء حساب جديد
            textViewRegister.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
            
            // نسيت كلمة المرور
            textViewForgotPassword.setOnClickListener {
                val email = editTextEmail.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.email,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                
                resetPassword(email)
            }
        }
    }
    
    private fun loginWithEmail(email: String, password: String) {
        binding.buttonLogin.isEnabled = false
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null && user.isEmailVerified) {
                    // تحديث آخر تسجيل دخول في Firestore
                    updateLastLogin(user.uid)
                    navigateToMain()
                } else {
                    Toast.makeText(
                        this,
                        R.string.verify_email,
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this, EmailVerificationActivity::class.java))
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                binding.buttonLogin.isEnabled = true
            }
    }
    
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
    
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    // حفظ بيانات المستخدم في Firestore
                    saveUserToFirestore(user.uid, user.email ?: "", user.displayName ?: "")
                    navigateToMain()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.check_email),
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun saveUserToFirestore(uid: String, email: String, name: String) {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "name" to name,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "lastLogin" to com.google.firebase.Timestamp.now()
        )
        
        db.collection("users").document(uid)
            .set(userMap)
            .addOnFailureListener { e ->
                // Log error but don't block user
            }
    }
    
    private fun updateLastLogin(uid: String) {
        db.collection("users").document(uid)
            .update("lastLogin", com.google.firebase.Timestamp.now())
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
