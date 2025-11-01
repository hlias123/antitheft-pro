package com.antitheft.pro.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.antitheft.pro.MainActivity;
import com.antitheft.pro.api.ApiService;

public class DeviceRegistrationActivity extends AppCompatActivity {
    
    private EditText emailInput;
    private EditText deviceNameInput;
    private Button registerButton;
    private Button skipButton;
    private TextView statusText;
    
    private SharedPreferences prefs;
    private ApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("AntiTheftPro", MODE_PRIVATE);
        apiService = new ApiService(this);
        
        // Check if already registered
        if (prefs.getBoolean("device_registered", false)) {
            goToMainActivity();
            return;
        }
        
        createRegistrationInterface();
    }
    
    private void createRegistrationInterface() {
        // Main layout
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 60, 40, 40);
        mainLayout.setBackgroundColor(0xFF1A1A2E);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("🛡️ Device Registration");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 20);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        mainLayout.addView(titleText);
        
        // Description
        TextView descText = new TextView(this);
        descText.setText("Register your device to enable web dashboard access and remote monitoring features.");
        descText.setTextSize(14);
        descText.setTextColor(0xFFB0BEC5);
        descText.setGravity(android.view.Gravity.CENTER);
        descText.setPadding(20, 0, 20, 30);
        descText.setLineSpacing(5, 1.2f);
        mainLayout.addView(descText);
        
        // Email Input
        TextView emailLabel = new TextView(this);
        emailLabel.setText("📧 Email Address:");
        emailLabel.setTextSize(16);
        emailLabel.setTextColor(0xFFFFFFFF);
        emailLabel.setPadding(0, 0, 0, 10);
        mainLayout.addView(emailLabel);
        
        emailInput = new EditText(this);
        emailInput.setHint("Enter your email address");
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
        
        // Device Name Input
        TextView deviceLabel = new TextView(this);
        deviceLabel.setText("📱 Device Name (Optional):");
        deviceLabel.setTextSize(16);
        deviceLabel.setTextColor(0xFFFFFFFF);
        deviceLabel.setPadding(0, 0, 0, 10);
        mainLayout.addView(deviceLabel);
        
        deviceNameInput = new EditText(this);
        deviceNameInput.setHint("My Phone");
        deviceNameInput.setText(android.os.Build.MODEL); // Default to device model
        deviceNameInput.setTextColor(0xFFFFFFFF);
        deviceNameInput.setHintTextColor(0xFF757575);
        deviceNameInput.setBackground(createRoundedBackground(0xFF16213E, 15));
        deviceNameInput.setPadding(25, 20, 25, 20);
        
        android.widget.LinearLayout.LayoutParams deviceParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        deviceParams.setMargins(0, 0, 0, 30);
        deviceNameInput.setLayoutParams(deviceParams);
        mainLayout.addView(deviceNameInput);
        
        // Status Text
        statusText = new TextView(this);
        statusText.setText("");
        statusText.setTextSize(14);
        statusText.setTextColor(0xFF4CAF50);
        statusText.setGravity(android.view.Gravity.CENTER);
        statusText.setPadding(20, 0, 20, 20);
        statusText.setVisibility(android.view.View.GONE);
        mainLayout.addView(statusText);
        
        // Register Button
        registerButton = createStyledButton("🚀 Register Device", 0xFF4CAF50);
        registerButton.setOnClickListener(v -> registerDevice());
        mainLayout.addView(registerButton);
        
        // Skip Button
        skipButton = createStyledButton("⏭️ Skip Registration", 0xFF757575);
        skipButton.setOnClickListener(v -> skipRegistration());
        mainLayout.addView(skipButton);
        
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
        infoTitle.setText("ℹ️ Registration Benefits:");
        infoTitle.setTextSize(16);
        infoTitle.setTextColor(0xFFE1BEE7);
        infoTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        infoTitle.setPadding(0, 0, 0, 10);
        infoBox.addView(infoTitle);
        
        TextView infoText = new TextView(this);
        infoText.setText("• Web dashboard access with PIN\n" +
                        "• Real-time location tracking\n" +
                        "• Remote security controls\n" +
                        "• Theft alerts and notifications\n" +
                        "• Device status monitoring");
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
    
    private void registerDevice() {
        String email = emailInput.getText().toString().trim();
        String deviceName = deviceNameInput.getText().toString().trim();
        
        // Validation
        if (email.isEmpty() || !email.contains("@")) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (deviceName.isEmpty()) {
            deviceName = android.os.Build.MODEL;
        }
        
        // Show loading
        registerButton.setText("🔄 Registering...");
        registerButton.setEnabled(false);
        statusText.setText("Connecting to server...");
        statusText.setTextColor(0xFF2196F3);
        statusText.setVisibility(android.view.View.VISIBLE);
        
        // Register with server
        apiService.registerDevice(email, new ApiService.DeviceRegistrationCallback() {
            @Override
            public void onResult(boolean success, String message) {
                if (success) {
                    // Save device name
                    prefs.edit()
                         .putString("device_name", deviceName)
                         .apply();
                    
                    statusText.setText("✅ Registration successful!");
                    statusText.setTextColor(0xFF4CAF50);
                    
                    Toast.makeText(DeviceRegistrationActivity.this, 
                        "Device registered successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Go to main activity after delay
                    statusText.postDelayed(() -> goToMainActivity(), 2000);
                    
                } else {
                    statusText.setText("❌ " + message);
                    statusText.setTextColor(0xFFF44336);
                    
                    // Reset button
                    registerButton.setText("🚀 Register Device");
                    registerButton.setEnabled(true);
                    
                    Toast.makeText(DeviceRegistrationActivity.this, 
                        "Registration failed: " + message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    
    private void skipRegistration() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Skip Registration?")
               .setMessage("Without registration, you won't be able to:\n\n" +
                          "• Access web dashboard\n" +
                          "• Use PIN authentication\n" +
                          "• Receive remote alerts\n" +
                          "• Monitor device remotely\n\n" +
                          "You can register later in Settings.")
               .setPositiveButton("Skip Anyway", (dialog, which) -> {
                   // Mark as skipped
                   prefs.edit()
                        .putBoolean("registration_skipped", true)
                        .apply();
                   
                   goToMainActivity();
               })
               .setNegativeButton("Register Now", null)
               .show();
    }
    
    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}