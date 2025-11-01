package com.antitheft.pro.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    
    private Switch locationSwitch;
    private Switch intruderSwitch;
    private Switch cameraSwitch;
    private Switch emergencySwitch;
    private TextView serverUrlDisplay;
    private Button changeServerButton;
    private Button resetButton;
    private Button backButton;
    
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("AntiTheftPro", MODE_PRIVATE);
        
        createSettingsInterface();
        loadCurrentSettings();
    }
    
    private void createSettingsInterface() {
        // Main layout
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 60, 40, 40);
        mainLayout.setBackgroundColor(0xFF1A1A2E);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("⚙️ Settings");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 30);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        mainLayout.addView(titleText);
        
        // Security Settings Section
        TextView securityLabel = new TextView(this);
        securityLabel.setText("🛡️ Security Features");
        securityLabel.setTextSize(18);
        securityLabel.setTextColor(0xFF4CAF50);
        securityLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        securityLabel.setPadding(0, 0, 0, 20);
        mainLayout.addView(securityLabel);
        
        // Location Tracking Switch
        locationSwitch = createSettingSwitch("📍 Location Tracking", 
            "Track device location continuously");
        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("location_tracking", isChecked).apply();
            Toast.makeText(this, isChecked ? "Location tracking enabled" : "Location tracking disabled", 
                Toast.LENGTH_SHORT).show();
        });
        mainLayout.addView(locationSwitch);
        
        // Intruder Detection Switch
        intruderSwitch = createSettingSwitch("👁️ Intruder Detection", 
            "Monitor unauthorized access attempts");
        intruderSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("intruder_detection", isChecked).apply();
            Toast.makeText(this, isChecked ? "Intruder detection enabled" : "Intruder detection disabled", 
                Toast.LENGTH_SHORT).show();
        });
        mainLayout.addView(intruderSwitch);
        
        // Camera Monitoring Switch
        cameraSwitch = createSettingSwitch("📷 Camera Monitoring", 
            "Automatic photo capture on suspicious activity");
        cameraSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("camera_monitoring", isChecked).apply();
            Toast.makeText(this, isChecked ? "Camera monitoring enabled" : "Camera monitoring disabled", 
                Toast.LENGTH_SHORT).show();
        });
        mainLayout.addView(cameraSwitch);
        
        // Emergency Mode Switch
        emergencySwitch = createSettingSwitch("🚨 Emergency Mode", 
            "Activate all security features immediately");
        emergencySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("emergency_mode", isChecked).apply();
            if (isChecked) {
                // Enable all other features
                locationSwitch.setChecked(true);
                intruderSwitch.setChecked(true);
                cameraSwitch.setChecked(true);
                prefs.edit()
                     .putBoolean("location_tracking", true)
                     .putBoolean("intruder_detection", true)
                     .putBoolean("camera_monitoring", true)
                     .apply();
                Toast.makeText(this, "🚨 Emergency mode activated - All features enabled", 
                    Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Emergency mode deactivated", Toast.LENGTH_SHORT).show();
            }
        });
        mainLayout.addView(emergencySwitch);
        
        // Add spacing
        android.view.View spacer1 = new android.view.View(this);
        spacer1.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 30));
        mainLayout.addView(spacer1);
        
        // Server Settings Section
        TextView serverLabel = new TextView(this);
        serverLabel.setText("🌐 Server Configuration");
        serverLabel.setTextSize(18);
        serverLabel.setTextColor(0xFF2196F3);
        serverLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        serverLabel.setPadding(0, 0, 0, 20);
        mainLayout.addView(serverLabel);
        
        // Server URL Container
        android.widget.LinearLayout serverContainer = new android.widget.LinearLayout(this);
        serverContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        serverContainer.setBackgroundColor(0xFF16213E);
        serverContainer.setPadding(25, 20, 25, 20);
        serverContainer.setBackground(createRoundedBackground(0xFF16213E, 15));
        
        TextView serverUrlLabel = new TextView(this);
        serverUrlLabel.setText("🔗 Web Dashboard URL:");
        serverUrlLabel.setTextSize(14);
        serverUrlLabel.setTextColor(0xFFB0BEC5);
        serverUrlLabel.setPadding(0, 0, 0, 10);
        serverContainer.addView(serverUrlLabel);
        
        serverUrlDisplay = new TextView(this);
        serverUrlDisplay.setText("http://localhost:8080");
        serverUrlDisplay.setTextSize(14);
        serverUrlDisplay.setTextColor(0xFF4CAF50);
        serverUrlDisplay.setTypeface(android.graphics.Typeface.MONOSPACE);
        serverContainer.addView(serverUrlDisplay);
        
        mainLayout.addView(serverContainer);
        
        // Add spacing
        android.view.View spacer2 = new android.view.View(this);
        spacer2.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 20));
        mainLayout.addView(spacer2);
        
        // Change Server Button
        changeServerButton = createStyledButton("🔧 Change Server URL", 0xFF2196F3);
        changeServerButton.setOnClickListener(v -> changeServerUrl());
        mainLayout.addView(changeServerButton);
        
        // Reset Settings Button
        resetButton = createStyledButton("🔄 Reset All Settings", 0xFFFF9800);
        resetButton.setOnClickListener(v -> resetAllSettings());
        mainLayout.addView(resetButton);
        
        // Back Button
        backButton = createStyledButton("← Back to Main", 0xFF757575);
        backButton.setOnClickListener(v -> finish());
        mainLayout.addView(backButton);
        
        setContentView(mainLayout);
    }
    
    private Switch createSettingSwitch(String title, String description) {
        // Container for switch item
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        container.setBackgroundColor(0xFF16213E);
        container.setPadding(25, 20, 25, 20);
        container.setBackground(createRoundedBackground(0xFF16213E, 15));
        
        android.widget.LinearLayout.LayoutParams containerParams = new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 0, 0, 15);
        container.setLayoutParams(containerParams);
        
        // Title and switch row
        android.widget.LinearLayout titleRow = new android.widget.LinearLayout(this);
        titleRow.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        titleRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        
        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setTextSize(16);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        
        android.widget.LinearLayout.LayoutParams titleParams = new android.widget.LinearLayout.LayoutParams(
            0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        titleText.setLayoutParams(titleParams);
        
        Switch switchView = new Switch(this);
        switchView.setTextColor(0xFFFFFFFF);
        
        titleRow.addView(titleText);
        titleRow.addView(switchView);
        container.addView(titleRow);
        
        // Description
        TextView descText = new TextView(this);
        descText.setText(description);
        descText.setTextSize(12);
        descText.setTextColor(0xFFB0BEC5);
        descText.setPadding(0, 8, 0, 0);
        container.addView(descText);
        
        // Add container to main layout (this will be done by caller)
        return switchView;
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
    
    private void loadCurrentSettings() {
        locationSwitch.setChecked(prefs.getBoolean("location_tracking", false));
        intruderSwitch.setChecked(prefs.getBoolean("intruder_detection", false));
        cameraSwitch.setChecked(prefs.getBoolean("camera_monitoring", false));
        emergencySwitch.setChecked(prefs.getBoolean("emergency_mode", false));
        
        String serverUrl = prefs.getString("server_url", "http://localhost:8080");
        serverUrlDisplay.setText(serverUrl);
    }
    
    private void changeServerUrl() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(prefs.getString("server_url", "http://localhost:8080"));
        input.setHint("Enter server URL (e.g., https://yourserver.com)");
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("🔧 Change Server URL")
               .setMessage("Enter the URL of your web dashboard server:")
               .setView(input)
               .setPositiveButton("Save", (dialog, which) -> {
                   String newUrl = input.getText().toString().trim();
                   if (!newUrl.isEmpty()) {
                       if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
                           newUrl = "http://" + newUrl;
                       }
                       prefs.edit().putString("server_url", newUrl).apply();
                       serverUrlDisplay.setText(newUrl);
                       Toast.makeText(this, "✅ Server URL updated", Toast.LENGTH_SHORT).show();
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private void resetAllSettings() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("🔄 Reset All Settings")
               .setMessage("Are you sure you want to reset all settings to default?\n\n" +
                          "This will:\n" +
                          "• Disable all security features\n" +
                          "• Reset server URL\n" +
                          "• Clear all preferences\n\n" +
                          "This action cannot be undone.")
               .setPositiveButton("Reset", (dialog, which) -> {
                   // Clear all preferences
                   prefs.edit().clear().apply();
                   
                   // Reload settings
                   loadCurrentSettings();
                   
                   Toast.makeText(this, "🔄 All settings reset to default", Toast.LENGTH_LONG).show();
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
}