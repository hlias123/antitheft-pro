package com.antitheft.pro.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.antitheft.pro.api.ApiService;

public class EmailVerificationActivity extends AppCompatActivity {
    
    private TextView titleText;
    private TextView instructionText;
    private EditText verificationCodeInput;
    private Button verifyButton;
    private Button resendButton;
    private Button backButton;
    private TextView statusText;
    private TextView timerText;
    
    private SharedPreferences prefs;
    private ApiService apiService;
    private CountDownTimer resendTimer;
    private String userEmail;
    private String userName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("AntiTheftPro", MODE_PRIVATE);
        apiService = new ApiService(this);
        
        // Get email and name from intent or preferences
        userEmail = getIntent().getStringExtra("email");
        userName = getIntent().getStringExtra("name");
        
        if (userEmail == null) {
            userEmail = prefs.getString("pending_email", "");
        }
        if (userName == null) {
            userName = prefs.getString("pending_name", "");
        }
        
        createVerificationInterface();
        startResendTimer();
    }
    
    private void createVerificationInterface() {
        // Main layout
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 60, 40, 40);
        mainLayout.setBackgroundColor(0xFF1A1A2E);
        
        // Title
        titleText = new TextView(this);
        titleText.setText("📧 تأكيد البريد الإلكتروني");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 20);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        mainLayout.addView(titleText);
        
        // Instruction Text
        instructionText = new TextView(this);
        instructionText.setText("تم إرسال رمز التأكيد إلى:\n" + userEmail + 
                               "\n\nيرجى التحقق من صندوق الوارد أو مجلد الرسائل غير المرغوب فيها");
        instructionText.setTextSize(14);
        instructionText.setTextColor(0xFFB0BEC5);
        instructionText.setGravity(android.view.Gravity.CENTER);
        instructionText.setPadding(20, 0, 20, 30);
        instructionText.setLineSpacing(5, 1.2f);
        mainLayout.addView(instructionText);
        
        // Verification Code Container
        android.widget.LinearLayout codeContainer = new android.widget.LinearLayout(this);
        codeContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        codeContainer.setBackground(createRoundedBackground(0xFF16213E, 20));
        codeContainer.setPadding(30, 25, 30, 25);
        
        android.widget.LinearLayout.LayoutParams containerParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 0, 0, 30);
        codeContainer.setLayoutParams(containerParams);
        
        TextView codeLabel = new TextView(this);
        codeLabel.setText("🔢 رمز التأكيد:");
        codeLabel.setTextSize(16);
        codeLabel.setTextColor(0xFFFFFFFF);
        codeLabel.setPadding(0, 0, 0, 15);
        codeContainer.addView(codeLabel);
        
        verificationCodeInput = new EditText(this);
        verificationCodeInput.setHint("أدخل رمز التأكيد المكون من 6 أرقام");
        verificationCodeInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        verificationCodeInput.setTextColor(0xFFFFFFFF);
        verificationCodeInput.setHintTextColor(0xFF757575);
        verificationCodeInput.setBackground(createRoundedBackground(0xFF0F172A, 12));
        verificationCodeInput.setPadding(20, 18, 20, 18);
        verificationCodeInput.setTextSize(18);
        verificationCodeInput.setGravity(android.view.Gravity.CENTER);
        verificationCodeInput.setTypeface(android.graphics.Typeface.MONOSPACE);
        verificationCodeInput.setMaxLines(1);
        
        // Limit to 6 digits
        verificationCodeInput.setFilters(new android.text.InputFilter[]{
            new android.text.InputFilter.LengthFilter(6)
        });
        
        codeContainer.addView(verificationCodeInput);
        mainLayout.addView(codeContainer);
        
        // Status Text
        statusText = new TextView(this);
        statusText.setText("");
        statusText.setTextSize(14);
        statusText.setTextColor(0xFF4CAF50);
        statusText.setGravity(android.view.Gravity.CENTER);
        statusText.setPadding(20, 0, 20, 20);
        statusText.setVisibility(android.view.View.GONE);
        mainLayout.addView(statusText);
        
        // Verify Button
        verifyButton = createStyledButton("✅ تأكيد الرمز", 0xFF4CAF50);
        verifyButton.setOnClickListener(v -> verifyCode());
        mainLayout.addView(verifyButton);
        
        // Resend Button
        resendButton = createStyledButton("📤 إعادة إرسال الرمز", 0xFF2196F3);
        resendButton.setOnClickListener(v -> resendVerificationCode());
        resendButton.setEnabled(false);
        mainLayout.addView(resendButton);
        
        // Timer Text
        timerText = new TextView(this);
        timerText.setText("يمكنك طلب رمز جديد خلال: 60 ثانية");
        timerText.setTextSize(12);
        timerText.setTextColor(0xFFFF9800);
        timerText.setGravity(android.view.Gravity.CENTER);
        timerText.setPadding(20, 0, 20, 20);
        mainLayout.addView(timerText);
        
        // Back Button
        backButton = createStyledButton("← العودة", 0xFF757575);
        backButton.setOnClickListener(v -> finish());
        mainLayout.addView(backButton);
        
        // Help Box
        android.widget.LinearLayout helpBox = new android.widget.LinearLayout(this);
        helpBox.setOrientation(android.widget.LinearLayout.VERTICAL);
        helpBox.setBackground(createRoundedBackground(0xFF2D1B69, 15));
        helpBox.setPadding(25, 20, 25, 20);
        
        android.widget.LinearLayout.LayoutParams helpParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        helpParams.setMargins(0, 20, 0, 0);
        helpBox.setLayoutParams(helpParams);
        
        TextView helpTitle = new TextView(this);
        helpTitle.setText("💡 لم تستلم الرمز؟");
        helpTitle.setTextSize(16);
        helpTitle.setTextColor(0xFFE1BEE7);
        helpTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        helpTitle.setPadding(0, 0, 0, 10);
        helpBox.addView(helpTitle);
        
        TextView helpText = new TextView(this);
        helpText.setText("• تحقق من مجلد الرسائل غير المرغوب فيها\n" +
                        "• تأكد من صحة عنوان البريد الإلكتروني\n" +
                        "• انتظر دقيقة واحدة ثم اطلب رمز جديد\n" +
                        "• تأكد من اتصالك بالإنترنت");
        helpText.setTextSize(14);
        helpText.setTextColor(0xFFFFFFFF);
        helpText.setLineSpacing(5, 1.3f);
        helpBox.addView(helpText);
        
        mainLayout.addView(helpBox);
        
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
    
    private void verifyCode() {
        String code = verificationCodeInput.getText().toString().trim();
        
        if (code.isEmpty() || code.length() != 6) {
            Toast.makeText(this, "يرجى إدخال رمز التأكيد المكون من 6 أرقام", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        verifyButton.setText("🔄 جاري التحقق...");
        verifyButton.setEnabled(false);
        statusText.setText("جاري التحقق من الرمز...");
        statusText.setTextColor(0xFF2196F3);
        statusText.setVisibility(android.view.View.VISIBLE);
        
        // Verify with server
        apiService.verifyEmail(userEmail, code, new ApiService.EmailVerificationCallback() {
            @Override
            public void onResult(boolean success, String message) {
                if (success) {
                    // Save verification status
                    prefs.edit()
                         .putBoolean("email_verified", true)
                         .putString("verified_email", userEmail)
                         .putString("user_name", userName)
                         .putLong("verification_time", System.currentTimeMillis())
                         .apply();
                    
                    statusText.setText("✅ تم تأكيد البريد الإلكتروني بنجاح!");
                    statusText.setTextColor(0xFF4CAF50);
                    
                    Toast.makeText(EmailVerificationActivity.this, 
                        "تم تأكيد حسابك بنجاح!", Toast.LENGTH_SHORT).show();
                    
                    // Go to PIN setup activity
                    statusText.postDelayed(() -> {
                        Intent intent = new Intent(EmailVerificationActivity.this, PinSetupActivity.class);
                        intent.putExtra("email", userEmail);
                        intent.putExtra("name", userName);
                        startActivity(intent);
                        finish();
                    }, 2000);
                    
                } else {
                    statusText.setText("❌ " + message);
                    statusText.setTextColor(0xFFF44336);
                    
                    // Reset button
                    verifyButton.setText("✅ تأكيد الرمز");
                    verifyButton.setEnabled(true);
                    
                    Toast.makeText(EmailVerificationActivity.this, 
                        "فشل في التحقق: " + message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    
    private void resendVerificationCode() {
        resendButton.setText("🔄 جاري الإرسال...");
        resendButton.setEnabled(false);
        
        apiService.resendVerificationCode(userEmail, new ApiService.ResendCodeCallback() {
            @Override
            public void onResult(boolean success, String message) {
                if (success) {
                    Toast.makeText(EmailVerificationActivity.this, 
                        "تم إرسال رمز جديد إلى بريدك الإلكتروني", Toast.LENGTH_SHORT).show();
                    
                    // Start timer again
                    startResendTimer();
                } else {
                    Toast.makeText(EmailVerificationActivity.this, 
                        "فشل في إرسال الرمز: " + message, Toast.LENGTH_LONG).show();
                    
                    resendButton.setText("📤 إعادة إرسال الرمز");
                    resendButton.setEnabled(true);
                }
            }
        });
    }
    
    private void startResendTimer() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        
        resendTimer = new CountDownTimer(60000, 1000) { // 60 seconds
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                timerText.setText("يمكنك طلب رمز جديد خلال: " + seconds + " ثانية");
                timerText.setTextColor(0xFFFF9800);
            }
            
            @Override
            public void onFinish() {
                timerText.setText("✅ يمكنك الآن طلب رمز جديد");
                timerText.setTextColor(0xFF4CAF50);
                resendButton.setText("📤 إعادة إرسال الرمز");
                resendButton.setEnabled(true);
            }
        };
        
        resendTimer.start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}