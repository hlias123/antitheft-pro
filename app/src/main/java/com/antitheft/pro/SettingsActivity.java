package com.antitheft.pro;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // إنشاء layout برمجياً
            createSettingsLayoutProgrammatically();
            
            Toast.makeText(this, "صفحة الإعدادات جاهزة", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }
    
    private void createSettingsLayoutProgrammatically() {
        try {
            // إنشاء LinearLayout رئيسي
            LinearLayout mainLayout = new LinearLayout(this);
            mainLayout.setOrientation(LinearLayout.VERTICAL);
            mainLayout.setPadding(50, 50, 50, 50);
            
            // عنوان
            TextView titleText = new TextView(this);
            titleText.setText("الإعدادات");
            titleText.setTextSize(24);
            titleText.setPadding(0, 0, 0, 30);
            mainLayout.addView(titleText);
            
            // إعدادات الأمان
            TextView securityTitle = new TextView(this);
            securityTitle.setText("إعدادات الأمان");
            securityTitle.setTextSize(18);
            securityTitle.setPadding(0, 0, 0, 20);
            mainLayout.addView(securityTitle);
            
            // خيارات الأمان
            final CheckBox pinLockBox = new CheckBox(this);
            pinLockBox.setText("تفعيل قفل الرقم السري");
            mainLayout.addView(pinLockBox);
            
            final CheckBox intruderBox = new CheckBox(this);
            intruderBox.setText("تفعيل كشف المتطفلين");
            mainLayout.addView(intruderBox);
            
            final CheckBox locationBox = new CheckBox(this);
            locationBox.setText("تفعيل تتبع الموقع");
            mainLayout.addView(locationBox);
            
            // بريد الإشعارات
            TextView emailTitle = new TextView(this);
            emailTitle.setText("بريد الإشعارات");
            emailTitle.setTextSize(18);
            emailTitle.setPadding(0, 30, 0, 10);
            mainLayout.addView(emailTitle);
            
            final EditText emailField = new EditText(this);
            emailField.setHint("أدخل بريدك الإلكتروني");
            emailField.setPadding(20, 20, 20, 20);
            mainLayout.addView(emailField);
            
            // زر الحفظ
            Button saveButton = new Button(this);
            saveButton.setText("حفظ الإعدادات");
            saveButton.setPadding(20, 20, 20, 20);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveSettings(pinLockBox, intruderBox, locationBox, emailField);
                }
            });
            mainLayout.addView(saveButton);
            
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
            Toast.makeText(this, "خطأ في إنشاء واجهة الإعدادات: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void saveSettings(CheckBox pinLockBox, CheckBox intruderBox, CheckBox locationBox, EditText emailField) {
        try {
            // محاكاة حفظ الإعدادات
            String message = "تم حفظ الإعدادات:\n";
            
            if (pinLockBox.isChecked()) {
                message += "- قفل الرقم السري مفعل\n";
            }
            
            if (intruderBox.isChecked()) {
                message += "- كشف المتطفلين مفعل\n";
            }
            
            if (locationBox.isChecked()) {
                message += "- تتبع الموقع مفعل\n";
            }
            
            String email = emailField.getText().toString().trim();
            if (!email.isEmpty()) {
                message += "- بريد الإشعارات: " + email;
            }
            
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في حفظ الإعدادات: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}