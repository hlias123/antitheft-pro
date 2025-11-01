package com.antitheft.pro.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.antitheft.pro.api.ApiService;

public class CreateAccountActivity extends AppCompatActivity {
    
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button createAccountButton;
    private Button backToLoginButton;
    private TextView statusText;
    
    private SharedPreferences prefs;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("AntiTheftPro", MODE_PRIVATE);
        apiService = new ApiService(this);
        
        createAccountInterface();
    }
    
    private void createAccountInterface() {
        // Main layout
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 60, 40, 40);
        mainLayout.setBackgroundColor(0xFF1A1A2E);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("🛡️ إنشاء حساب جديد");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 20);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        mainLayout.addView(titleText);
        
        // Description
        TextView descText = new TextView(this);
        descText.setText("أنشئ حسابك الآمن للوصول إلى جميع ميزات الحماية المتقدمة");
        descText.setTextSize(14);
        descText.setTextColor(0xFFB0BEC5);
        descText.setGravity(android.view.Gravity.CENTER);
        descText.setPadding(20, 0, 20, 30);
        descText.setLineSpacing(5, 1.2f);
        mainLayout.addView(descText);
        
        // Name Input
        TextView nameLabel = new TextView(this);
        nameLabel.setText("👤 الاسم الكامل:");
        nameLabel.setTextSize(16);
        nameLabel.setTextColor(0xFFFFFFFF);
        nameLabel.setPadding(0, 0, 0, 10);
        mainLayout.addView(nameLabel);
        
        nameInput = new EditText(this);
        nameInput.setHint("أدخل اسمك الكامل");
        nameInput.setTextColor(0xFFFFFFFF);
        nameInput.setHintTextColor(0xFF757575);
        nameInput.setBackground(createRoundedBackground(0xFF16213E, 15));
        nameInput.setPadding(25, 20, 25, 20);
        
        android.widget.LinearLayout.LayoutParams nameParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameParams.setMargins(0, 0, 0, 20);
        nameInput.setLayoutParams(nameParams);
        mainLayout.addView(nameInput);
        
        // Email Input
        TextView emailLabel = new TextView(this);
        emailLabel.setText("📧 البريد الإلكتروني:");
        emailLabel.setTextSize(16);
        emailLabel.setTextColor(0xFFFFFFFF);
        emailLabel.setPadding(0, 0, 0, 10);
        mainLayout.addView(emailLabel);
        
        emailInput = new EditText(this);
        emailInput.setHint("أدخل بريدك الإلكتروني");
        emailInput.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailInput.setTextColor(0xFFFFFFFF);
        emailInput.setHintTextColor(0xFF757575);
        emailInput.setBackground(createRoundedBackground(0xFF16213E, 15));
        emailInput.setPadding(25, 20, 25, 20);
        
        android.widget.LinearLayout.LayoutParams emailParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        emailParams.setMargins(0, 0, 0, 20);
        emailInput.setLayoutParams(emailParams);
        mainLayout.addView(emailInput);
        
        // Password Input
        TextView passwordLabel = new TextView(this);
        passwordLabel.setText("🔒 كلمة المرور:");
        passwordLabel.setTextSize(16);
        passwordLabel.setTextColor(0xFFFFFFFF);
        passwordLabel.setPadding(0, 0, 0, 10);
        mainLayout.addView(passwordLabel);
        
        passwordInput = new EditText(this);
        passwordInput.setHint("أدخل كلمة مرور قوية (6 أحرف على الأقل)");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setTextColor(0xFFFFFFFF);
        passwordInput.setHintTextColor(0xFF757575);
        passwordInput.setBackground(createRoundedBackground(0xFF16213E, 15));
        passwordInput.setPadding(25, 20, 25, 20);
        
        android.widget.LinearLayout.LayoutParams passwordParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        passwordParams.setMargins(0, 0, 0, 20);
        passwordInput.setLayoutParams(passwordParams);
        mainLayout.addView(passwordInput);
        
        // Confirm Password Input
        TextView confirmLabel = new TextView(this);
        confirmLabel.setText("🔒 تأكيد كلمة المرور:");
        confirmLabel.setTextSize(16);
        confirmLabel.setTextColor(0xFFFFFFFF);
        confirmLabel.setPadding(0, 0, 0, 10);
        mainLayout.addView(confirmLabel);
        
        confirmPasswordInput = new EditText(this);
        confirmPasswordInput.setHint("أعد إدخال كلمة المرور");
        confirmPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordInput.setTextColor(0xFFFFFFFF);
        confirmPasswordInput.setHintTextColor(0xFF757575);
        confirmPasswordInput.setBackground(createRoundedBackground(0xFF16213E, 15));
        confirmPasswordInput.setPadding(25, 20, 25, 20);
        
        android.widget.LinearLayout.LayoutParams confirmParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        confirmParams.setMargins(0, 0, 0, 30);
        confirmPasswordInput.setLayoutParams(confirmParams);
        mainLayout.addView(confirmPasswordInput);
        
        // Status Text
        statusText = new TextView(this);
        statusText.setText("");
        statusText.setTextSize(14);
        statusText.setTextColor(0xFF4CAF50);
        statusText.setGravity(android.view.Gravity.CENTER);
        statusText.setPadding(20, 0, 20, 20);
        statusText.setVisibility(android.view.View.GONE);
        mainLayout.addView(statusText);
        
        // Create Account Button
        createAccountButton = createStyledButton("🚀 إنشاء الحساب", 0xFF4CAF50);
        createAccountButton.setOnClickListener(v -> createAccount());
        mainLayout.addView(createAccountButton);
        
        // Back to Login Button
        backToLoginButton = createStyledButton("← العودة لتسجيل الدخول", 0xFF757575);
        backToLoginButton.setOnClickListener(v -> finish());
        mainLayout.addView(backToLoginButton);
        
        // Info Box
        android.widget.LinearLayout infoBox = new android.widget.LinearLayout(this);
        infoBox.setOrientation(android.widget.LinearLayout.VERTICAL);
        infoBox.setBackground(createRoundedBackground(0xFF2D1B69, 15));
        infoBox.setPadding(25, 20, 25, 20);
        
        android.widget.LinearLayout.LayoutParams infoParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        infoParams.setMargins(0, 20, 0, 0);
        infoBox.setLayoutParams(infoParams);
        
        TextView infoTitle = new TextView(this);
        infoTitle.setText("ℹ️ خطوات إنشاء الحساب:");
        infoTitle.setTextSize(16);
        infoTitle.setTextColor(0xFFE1BEE7);
        infoTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        infoTitle.setPadding(0, 0, 0, 10);
        infoBox.addView(infoTitle);
        
        TextView infoText = new TextView(this);
        infoText.setText("1️⃣ أدخل بياناتك الشخصية\n" +
                        "2️⃣ سيتم إرسال رسالة تأكيد للإيميل\n" +
                        "3️⃣ اضغط على رابط التأكيد\n" +
                        "4️⃣ أدخل PIN للوصول للموقع\n" +
                        "5️⃣ ابدأ استخدام النظام الآمن");
        infoText.setTextSize(14);
        infoText.setTextColor(0xFFFFFFFF);
        infoText.setLineSpacing(5, 1.3f);
        infoBox.addView(infoText);
        
        mainLayout.addView(infoBox);
        
        setContentView(mainLayout);
    }
    
    private Button createStyledButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setTextColor(0xFFFFFFFF);
        button.setBackground(createRoundedBackground(color, 15));
        button.setPadding(30, 25, 30, 25);
        
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 15);
        button.setLayoutParams(params);
        
        return button;
    }
    
    private android.graphics.drawable.GradientDrawable createRoundedBackground(int color, int radius) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius * getResources().getDisplayMetrics().density);
        return drawable;
    }
    
    private void createAccount() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();
        
        // Validation
        if (name.isEmpty() || name.length() < 2) {
            Toast.makeText(this, "يرجى إدخال اسم صحيح (حرفين على الأقل)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            Toast.makeText(this, "يرجى إدخال بريد إلكتروني صحيح", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (password.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "كلمة المرور يجب أن تكون 6 أحرف على الأقل", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "كلمة المرور وتأكيدها غير متطابقين", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        createAccountButton.setText("🔄 جاري إنشاء الحساب...");
        createAccountButton.setEnabled(false);
        statusText.setText("جاري إرسال البيانات للخادم...");
        statusText.setTextColor(0xFF2196F3);
        statusText.setVisibility(android.view.View.VISIBLE);
        
        // Create account with server
        apiService.createAccount(name, email, password, new ApiService.AccountCreationCallback() {
            @Override
            public void onResult(boolean success, String message, String verificationToken) {
                if (success) {
                    // Save account info
                    prefs.edit()
                         .putString("pending_name", name)
                         .putString("pending_email", email)
                         .putString("verification_token", verificationToken)
                         .putBoolean("account_created", true)
                         .putBoolean("email_verified", false)
                         .apply();
                    
                    statusText.setText("✅ تم إنشاء الحساب! تحقق من بريدك الإلكتروني");
                    statusText.setTextColor(0xFF4CAF50);
                    
                    Toast.makeText(CreateAccountActivity.this, 
                        "تم إرسال رسالة التأكيد إلى بريدك الإلكتروني", Toast.LENGTH_LONG).show();
                    
                    // Go to email verification activity
                    statusText.postDelayed(() -> {
                        Intent intent = new Intent(CreateAccountActivity.this, EmailVerificationActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("name", name);
                        startActivity(intent);
                        finish();
                    }, 2000);
                    
                } else {
                    statusText.setText("❌ " + message);
                    statusText.setTextColor(0xFFF44336);
                    
                    // Reset button
                    createAccountButton.setText("🚀 إنشاء الحساب");
                    createAccountButton.setEnabled(true);
                    
                    Toast.makeText(CreateAccountActivity.this, 
                        "فشل في إنشاء الحساب: " + message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}