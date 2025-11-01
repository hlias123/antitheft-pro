package com.antitheft.pro.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IntrudersActivity extends AppCompatActivity {
    
    private TextView statusDisplay;
    private TextView attemptsDisplay;
    private TextView lastAttemptDisplay;
    private TextView intruderInfoDisplay;
    private Button enableButton;
    private Button clearButton;
    private Button backButton;
    
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("AntiTheftPro", MODE_PRIVATE);
        
        createIntrudersInterface();
        updateIntruderInfo();
    }
    
    private void createIntrudersInterface() {
        // Main layout
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 60, 40, 40);
        mainLayout.setBackgroundColor(0xFF1A1A2E);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("👁️ Intruder Detection");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 30);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        mainLayout.addView(titleText);
        
        // Description
        TextView descText = new TextView(this);
        descText.setText("Monitor unauthorized access attempts and capture intruder photos automatically.");
        descText.setTextSize(14);
        descText.setTextColor(0xFFB0BEC5);
        descText.setGravity(android.view.Gravity.CENTER);
        descText.setPadding(20, 0, 20, 30);
        descText.setLineSpacing(5, 1.2f);
        mainLayout.addView(descText);
        
        // Status Container
        android.widget.LinearLayout statusContainer = new android.widget.LinearLayout(this);
        statusContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        statusContainer.setBackgroundColor(0xFF16213E);
        statusContainer.setPadding(30, 25, 30, 25);
        statusContainer.setBackground(createRoundedBackground(0xFF16213E, 20));
        
        // Status Display
        TextView statusLabel = new TextView(this);
        statusLabel.setText("🛡️ Detection Status:");
        statusLabel.setTextSize(16);
        statusLabel.setTextColor(0xFFB0BEC5);
        statusLabel.setPadding(0, 0, 0, 10);
        statusContainer.addView(statusLabel);
        
        statusDisplay = new TextView(this);
        statusDisplay.setText("Disabled");
        statusDisplay.setTextSize(18);
        statusDisplay.setTextColor(0xFFF44336);
        statusDisplay.setTypeface(null, android.graphics.Typeface.BOLD);
        statusDisplay.setPadding(0, 0, 0, 20);
        statusContainer.addView(statusDisplay);
        
        // Attempts Display
        TextView attemptsLabel = new TextView(this);
        attemptsLabel.setText("🚨 Failed Attempts:");
        attemptsLabel.setTextSize(16);
        attemptsLabel.setTextColor(0xFFB0BEC5);
        attemptsLabel.setPadding(0, 0, 0, 10);
        statusContainer.addView(attemptsLabel);
        
        attemptsDisplay = new TextView(this);
        attemptsDisplay.setText("0");
        attemptsDisplay.setTextSize(24);
        attemptsDisplay.setTextColor(0xFF4CAF50);
        attemptsDisplay.setTypeface(null, android.graphics.Typeface.BOLD);
        attemptsDisplay.setPadding(0, 0, 0, 20);
        statusContainer.addView(attemptsDisplay);
        
        // Last Attempt Display
        TextView lastAttemptLabel = new TextView(this);
        lastAttemptLabel.setText("⏰ Last Attempt:");
        lastAttemptLabel.setTextSize(16);
        lastAttemptLabel.setTextColor(0xFFB0BEC5);
        lastAttemptLabel.setPadding(0, 0, 0, 10);
        statusContainer.addView(lastAttemptLabel);
        
        lastAttemptDisplay = new TextView(this);
        lastAttemptDisplay.setText("Never");
        lastAttemptDisplay.setTextSize(14);
        lastAttemptDisplay.setTextColor(0xFF757575);
        statusContainer.addView(lastAttemptDisplay);
        
        mainLayout.addView(statusContainer);
        
        // Add spacing
        android.view.View spacer1 = new android.view.View(this);
        spacer1.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 30));
        mainLayout.addView(spacer1);
        
        // Intruder Info Container
        android.widget.LinearLayout infoContainer = new android.widget.LinearLayout(this);
        infoContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        infoContainer.setBackgroundColor(0xFF2D1B69);
        infoContainer.setPadding(25, 20, 25, 20);
        infoContainer.setBackground(createRoundedBackground(0xFF2D1B69, 15));
        
        TextView infoLabel = new TextView(this);
        infoLabel.setText("📋 Detection Features:");
        infoLabel.setTextSize(16);
        infoLabel.setTextColor(0xFFE1BEE7);
        infoLabel.setPadding(0, 0, 0, 10);
        infoContainer.addView(infoLabel);
        
        intruderInfoDisplay = new TextView(this);
        intruderInfoDisplay.setText("• Wrong PIN attempts monitoring\n" +
                                   "• Automatic photo capture\n" +
                                   "• Location logging\n" +
                                   "• Silent alarm activation\n" +
                                   "• Real-time notifications");
        intruderInfoDisplay.setTextSize(14);
        intruderInfoDisplay.setTextColor(0xFFFFFFFF);
        intruderInfoDisplay.setLineSpacing(5, 1.3f);
        infoContainer.addView(intruderInfoDisplay);
        
        mainLayout.addView(infoContainer);
        
        // Add spacing
        android.view.View spacer2 = new android.view.View(this);
        spacer2.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 30));
        mainLayout.addView(spacer2);
        
        // Enable/Disable Button
        enableButton = createStyledButton("🔴 Enable Detection", 0xFF4CAF50);
        enableButton.setOnClickListener(v -> toggleIntruderDetection());
        mainLayout.addView(enableButton);
        
        // Clear History Button
        clearButton = createStyledButton("🗑️ Clear History", 0xFFFF9800);
        clearButton.setOnClickListener(v -> clearIntruderHistory());
        mainLayout.addView(clearButton);
        
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
    
    private void updateIntruderInfo() {
        boolean detectionEnabled = prefs.getBoolean("intruder_detection", false);
        int failedAttempts = prefs.getInt("failed_attempts", 0);
        long lastAttemptTime = prefs.getLong("last_attempt_time", 0);
        
        // Update status
        if (detectionEnabled) {
            statusDisplay.setText("🟢 Active");
            statusDisplay.setTextColor(0xFF4CAF50);
            enableButton.setText("🟢 Disable Detection");
            enableButton.setBackground(createRoundedBackground(0xFFF44336, 15));
        } else {
            statusDisplay.setText("🔴 Disabled");
            statusDisplay.setTextColor(0xFFF44336);
            enableButton.setText("🔴 Enable Detection");
            enableButton.setBackground(createRoundedBackground(0xFF4CAF50, 15));
        }
        
        // Update attempts count
        attemptsDisplay.setText(String.valueOf(failedAttempts));
        if (failedAttempts == 0) {
            attemptsDisplay.setTextColor(0xFF4CAF50);
        } else if (failedAttempts < 5) {
            attemptsDisplay.setTextColor(0xFFFF9800);
        } else {
            attemptsDisplay.setTextColor(0xFFF44336);
        }
        
        // Update last attempt time
        if (lastAttemptTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault());
            lastAttemptDisplay.setText(sdf.format(new Date(lastAttemptTime)));
            lastAttemptDisplay.setTextColor(0xFFFF9800);
        } else {
            lastAttemptDisplay.setText("Never");
            lastAttemptDisplay.setTextColor(0xFF757575);
        }
    }
    
    private void toggleIntruderDetection() {
        boolean currentState = prefs.getBoolean("intruder_detection", false);
        boolean newState = !currentState;
        
        prefs.edit()
             .putBoolean("intruder_detection", newState)
             .apply();
        
        if (newState) {
            Toast.makeText(this, "👁️ Intruder detection enabled", Toast.LENGTH_SHORT).show();
            
            // Show activation dialog
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("🛡️ Intruder Detection Activated")
                   .setMessage("The following features are now active:\n\n" +
                              "• Failed PIN attempt monitoring\n" +
                              "• Automatic front camera capture\n" +
                              "• Location logging on attempts\n" +
                              "• Silent notifications\n\n" +
                              "Your device is now protected against unauthorized access.")
                   .setPositiveButton("Got it!", null)
                   .show();
        } else {
            Toast.makeText(this, "Intruder detection disabled", Toast.LENGTH_SHORT).show();
        }
        
        updateIntruderInfo();
    }
    
    private void clearIntruderHistory() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Clear Intruder History")
               .setMessage("Are you sure you want to clear all intruder detection history?\n\n" +
                          "This will remove:\n" +
                          "• Failed attempt records\n" +
                          "• Captured photos\n" +
                          "• Location logs\n\n" +
                          "This action cannot be undone.")
               .setPositiveButton("Clear", (dialog, which) -> {
                   // Clear all intruder data
                   prefs.edit()
                        .remove("failed_attempts")
                        .remove("last_attempt_time")
                        .remove("intruder_photos")
                        .remove("attempt_locations")
                        .apply();
                   
                   updateIntruderInfo();
                   Toast.makeText(this, "🗑️ Intruder history cleared", Toast.LENGTH_SHORT).show();
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    // Simulate an intruder attempt (for testing)
    public void simulateIntruderAttempt() {
        int currentAttempts = prefs.getInt("failed_attempts", 0);
        prefs.edit()
             .putInt("failed_attempts", currentAttempts + 1)
             .putLong("last_attempt_time", System.currentTimeMillis())
             .apply();
        
        updateIntruderInfo();
        
        Toast.makeText(this, "🚨 Intruder attempt detected!", Toast.LENGTH_SHORT).show();
    }
}