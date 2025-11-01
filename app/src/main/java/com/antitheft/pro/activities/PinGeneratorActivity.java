package com.antitheft.pro.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.antitheft.pro.api.ApiService;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PinGeneratorActivity extends AppCompatActivity {
    
    private TextView pinDisplay;
    private TextView timerDisplay;
    private TextView instructionsText;
    private Button generateButton;
    private Button copyButton;
    private Button backButton;
    
    private String currentPin = "";
    private CountDownTimer pinTimer;
    private SharedPreferences prefs;
    private ApiService apiService;
    private static final long PIN_VALIDITY_DURATION = 5 * 60 * 1000; // 5 minutes
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("AntiTheftPro", MODE_PRIVATE);
        apiService = new ApiService(this);
        
        createPinGeneratorInterface();
        
        // Check if there's an active PIN
        checkExistingPin();
    }
    
    private void createPinGeneratorInterface() {
        // Main layout
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 60, 40, 40);
        mainLayout.setBackgroundColor(0xFF1A1A2E);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("🔑 Web Access PIN Generator");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 30);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        mainLayout.addView(titleText);
        
        // Instructions
        instructionsText = new TextView(this);
        instructionsText.setText("Generate a secure 6-digit PIN to access the web dashboard.\n\n" +
                                "⏰ PIN expires in 5 minutes\n" +
                                "🔒 Each PIN can only be used once\n" +
                                "🌐 Use at: http://your-server.com");
        instructionsText.setTextSize(14);
        instructionsText.setTextColor(0xFFB0BEC5);
        instructionsText.setGravity(android.view.Gravity.CENTER);
        instructionsText.setPadding(20, 0, 20, 30);
        instructionsText.setLineSpacing(5, 1.2f);
        mainLayout.addView(instructionsText);
        
        // PIN Display Container
        android.widget.LinearLayout pinContainer = new android.widget.LinearLayout(this);
        pinContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        pinContainer.setBackgroundColor(0xFF16213E);
        pinContainer.setPadding(40, 30, 40, 30);
        pinContainer.setBackground(createRoundedBackground(0xFF16213E, 20));
        
        // PIN Display
        pinDisplay = new TextView(this);
        pinDisplay.setText("------");
        pinDisplay.setTextSize(48);
        pinDisplay.setTextColor(0xFF4CAF50);
        pinDisplay.setGravity(android.view.Gravity.CENTER);
        pinDisplay.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        pinDisplay.setLetterSpacing(0.3f);
        pinContainer.addView(pinDisplay);
        
        // Timer Display
        timerDisplay = new TextView(this);
        timerDisplay.setText("");
        timerDisplay.setTextSize(16);
        timerDisplay.setTextColor(0xFFFF9800);
        timerDisplay.setGravity(android.view.Gravity.CENTER);
        timerDisplay.setPadding(0, 15, 0, 0);
        timerDisplay.setVisibility(View.GONE);
        pinContainer.addView(timerDisplay);
        
        mainLayout.addView(pinContainer);
        
        // Add spacing
        android.view.View spacer = new android.view.View(this);
        spacer.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 40));
        mainLayout.addView(spacer);
        
        // Generate Button
        generateButton = createStyledButton("🎲 Generate New PIN", 0xFF4CAF50);
        generateButton.setOnClickListener(v -> generateNewPin());
        mainLayout.addView(generateButton);
        
        // Copy Button
        copyButton = createStyledButton("📋 Copy PIN", 0xFF2196F3);
        copyButton.setOnClickListener(v -> copyPinToClipboard());
        copyButton.setEnabled(false);
        mainLayout.addView(copyButton);
        
        // Back Button
        backButton = createStyledButton("← Back to Main", 0xFF757575);
        backButton.setOnClickListener(v -> finish());
        mainLayout.addView(backButton);
        
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
    
    private void checkExistingPin() {
        String existingPin = prefs.getString("current_pin", "");
        long pinExpiry = prefs.getLong("pin_expiry", 0);
        
        if (!existingPin.isEmpty() && System.currentTimeMillis() < pinExpiry) {
            // There's a valid existing PIN
            currentPin = existingPin;
            pinDisplay.setText(formatPin(currentPin));
            copyButton.setEnabled(true);
            
            // Start timer for remaining time
            long remainingTime = pinExpiry - System.currentTimeMillis();
            startPinTimer(remainingTime);
            
            generateButton.setText("🔄 Generate New PIN");
        }
    }
    
    private void generateNewPin() {
        // Generate secure 6-digit PIN
        SecureRandom random = new SecureRandom();
        int pin = 100000 + random.nextInt(900000);
        currentPin = String.valueOf(pin);
        
        // Calculate expiry time
        long expiryTime = System.currentTimeMillis() + PIN_VALIDITY_DURATION;
        
        // Save to preferences
        prefs.edit()
             .putString("current_pin", currentPin)
             .putLong("pin_expiry", expiryTime)
             .putString("pin_created", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()))
             .apply();
        
        // Send PIN to server for validation
        sendPinToServer(currentPin, expiryTime);
        
        // Update UI
        pinDisplay.setText(formatPin(currentPin));
        copyButton.setEnabled(true);
        generateButton.setText("🔄 Generate New PIN");
        
        // Start timer
        startPinTimer(PIN_VALIDITY_DURATION);
        
        // Show success message
        Toast.makeText(this, "✅ New PIN generated successfully!", Toast.LENGTH_SHORT).show();
        
        // Auto-copy to clipboard
        copyPinToClipboard();
    }
    
    private String formatPin(String pin) {
        if (pin.length() == 6) {
            return pin.substring(0, 3) + " " + pin.substring(3);
        }
        return pin;
    }
    
    private void startPinTimer(long duration) {
        if (pinTimer != null) {
            pinTimer.cancel();
        }
        
        timerDisplay.setVisibility(View.VISIBLE);
        
        pinTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                
                timerDisplay.setText(String.format(Locale.getDefault(), 
                    "⏰ Expires in: %02d:%02d", minutes, seconds));
                
                // Change color as time runs out
                if (millisUntilFinished < 60000) { // Last minute
                    timerDisplay.setTextColor(0xFFF44336);
                } else if (millisUntilFinished < 120000) { // Last 2 minutes
                    timerDisplay.setTextColor(0xFFFF9800);
                } else {
                    timerDisplay.setTextColor(0xFF4CAF50);
                }
            }
            
            @Override
            public void onFinish() {
                // PIN expired
                currentPin = "";
                pinDisplay.setText("EXPIRED");
                pinDisplay.setTextColor(0xFFF44336);
                timerDisplay.setText("⚠️ PIN has expired");
                timerDisplay.setTextColor(0xFFF44336);
                copyButton.setEnabled(false);
                generateButton.setText("🎲 Generate New PIN");
                
                // Clear from preferences
                prefs.edit()
                     .remove("current_pin")
                     .remove("pin_expiry")
                     .apply();
                
                Toast.makeText(PinGeneratorActivity.this, "⚠️ PIN has expired. Generate a new one.", Toast.LENGTH_LONG).show();
            }
        };
        
        pinTimer.start();
    }
    
    private void copyPinToClipboard() {
        if (currentPin.isEmpty()) {
            Toast.makeText(this, "No PIN to copy", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Anti-Theft PIN", currentPin);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "📋 PIN copied to clipboard!", Toast.LENGTH_SHORT).show();
        
        // Show usage instructions
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("🔑 PIN Ready for Use")
               .setMessage("Your PIN has been copied to clipboard:\n\n" +
                          "📱 PIN: " + formatPin(currentPin) + "\n\n" +
                          "🌐 Go to your web dashboard\n" +
                          "📧 Enter your email address\n" +
                          "🔑 Paste this PIN\n" +
                          "🚀 Access granted!\n\n" +
                          "⚠️ This PIN expires in 5 minutes and can only be used once.")
               .setPositiveButton("Got it!", null)
               .show();
    }
    
    private void sendPinToServer(String pin, long expiry) {
        // Get registered email
        String email = prefs.getString("registered_email", "");
        if (email.isEmpty()) {
            // Try to get email from user input or device registration
            email = "user@example.com"; // Default for demo
        }
        
        // Send PIN to server via API
        try {
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    org.json.JSONObject pinData = new org.json.JSONObject();
                    pinData.put("email", email);
                    pinData.put("pin", pin);
                    pinData.put("device_id", getDeviceId());
                    pinData.put("expiry", expiry);
                    
                    // Send to server
                    java.net.URL url = new java.net.URL(getServerUrl() + "/api/pin/store");
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    
                    java.io.OutputStream os = conn.getOutputStream();
                    os.write(pinData.toString().getBytes("UTF-8"));
                    os.close();
                    
                    int responseCode = conn.getResponseCode();
                    if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                        android.util.Log.d("PinGenerator", "PIN sent to server successfully");
                    }
                    
                } catch (Exception e) {
                    android.util.Log.e("PinGenerator", "Error sending PIN to server", e);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("PinGenerator", "Error creating executor", e);
        }
    }
    
    private String getDeviceId() {
        String deviceId = prefs.getString("device_id", "");
        if (deviceId.isEmpty()) {
            deviceId = "android_" + android.os.Build.SERIAL + "_" + System.currentTimeMillis();
            prefs.edit().putString("device_id", deviceId).apply();
        }
        return deviceId;
    }
    
    private String getServerUrl() {
        return prefs.getString("server_url", "http://localhost:8080");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pinTimer != null) {
            pinTimer.cancel();
        }
    }
}