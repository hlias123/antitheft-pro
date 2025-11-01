package com.antitheft.pro;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // إنشاء layout برمجياً
            createLoginLayoutProgrammatically();
            
            Toast.makeText(this, "صفحة تسجيل الدخول جاهزة", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }
    
    private void createLoginLayoutProgrammatically() {
        try {
            // إنشاء LinearLayout رئيسي
            LinearLayout mainLayout = new LinearLayout(this);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setPadding(50, 100, 50, 50);
            
            // عنوان
            TextView titleText = new TextView(this);
            titleText.setText("تسجيل الدخول");
            titleText.setTextSize(24);
            titleText.setPadding(0, 0, 0, 50);
            mainLayout.addView(titleText);
            
            // حقل البريد الإلكتروني
            final EditText emailField = new EditText(this);
            emailField.setHint("البريد الإلكتروني");
            emailField.setPadding(20, 20, 20, 20);
            mainLayout.addView(emailField);
            
            // حقل كلمة المرور
            final EditText passwordField = new EditText(this);
            passwordField.setHint("كلمة المرور");
            passwordField.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordField.setPadding(20, 20, 20, 20);
            mainLayout.addView(passwordField);
            
            // زر تسجيل الدخول
            Button loginButton = new Button(this);
            loginButton.setText("دخول");
            loginButton.setPadding(20, 20, 20, 20);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performLogin(emailField, passwordField);
                }
            });
            mainLayout.addView(loginButton);
            
            // زر العودة
            Button backButton = new Button(this);
            backButton.setText("عودة");
            backButton.setPadding(20, 20, 20, 20);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            mainLayout.addView(backButton);
            
            // تعيين Layout
            setContentView(mainLayout);
            
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في إنشاء واجهة تسجيل الدخول: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void performLogin(EditText emailField, EditText passwordField) {
        try {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال البريد الإلكتروني وكلمة المرور", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // محاكاة تسجيل دخول بسيط
            if (email.equals("admin@test.com") && password.equals("123456")) {
                Toast.makeText(this, "تم تسجيل الدخول بنجاح!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "البريد الإلكتروني أو كلمة المرور غير صحيحة", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في عملية تسجيل الدخول: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}