package com.antitheft.pro.activities;

import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrackingActivity extends AppCompatActivity {
    
    private TextView locationDisplay;
    private TextView accuracyDisplay;
    private TextView lastUpdateDisplay;
    private TextView statusDisplay;
    private Button refreshButton;
    private Button backButton;
    
    private SharedPreferences prefs;
    private Handler updateHandler;
    private Runnable updateRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("AntiTheftPro", MODE_PRIVATE);
        
        createTrackingInterface();
        
        // Start auto-refresh
        startAutoRefresh();
    }
    
    private void createTrackingInterface() {
        // Main layout
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 60, 40, 40);
        mainLayout.setBackgroundColor(0xFF1A1A2E);
        
        // Title
        TextView titleText = new TextView(this);
        titleText.setText("📍 Live Location Tracking");
        titleText.setTextSize(24);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 30);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        mainLayout.addView(titleText);
        
        // Status Display
        statusDisplay = new TextView(this);
        statusDisplay.setText("🔄 Initializing...");
        statusDisplay.setTextSize(16);
        statusDisplay.setTextColor(0xFF4CAF50);
        statusDisplay.setGravity(android.view.Gravity.CENTER);
        statusDisplay.setPadding(20, 10, 20, 20);
        mainLayout.addView(statusDisplay);
        
        // Location Container
        android.widget.LinearLayout locationContainer = new android.widget.LinearLayout(this);
        locationContainer.setOrientation(android.widget.LinearLayout.VERTICAL);
        locationContainer.setBackgroundColor(0xFF16213E);
        locationContainer.setPadding(30, 25, 30, 25);
        locationContainer.setBackground(createRoundedBackground(0xFF16213E, 20));
        
        // Location Display
        TextView locationLabel = new TextView(this);
        locationLabel.setText("📍 Current Location:");
        locationLabel.setTextSize(16);
        locationLabel.setTextColor(0xFFB0BEC5);
        locationLabel.setPadding(0, 0, 0, 10);
        locationContainer.addView(locationLabel);
        
        locationDisplay = new TextView(this);
        locationDisplay.setText("Waiting for GPS...");
        locationDisplay.setTextSize(14);
        locationDisplay.setTextColor(0xFFFFFFFF);
        locationDisplay.setTypeface(android.graphics.Typeface.MONOSPACE);
        locationDisplay.setPadding(0, 0, 0, 15);
        locationContainer.addView(locationDisplay);
        
        // Accuracy Display
        TextView accuracyLabel = new TextView(this);
        accuracyLabel.setText("🎯 Accuracy:");
        accuracyLabel.setTextSize(16);
        accuracyLabel.setTextColor(0xFFB0BEC5);
        accuracyLabel.setPadding(0, 0, 0, 10);
        locationContainer.addView(accuracyLabel);
        
        accuracyDisplay = new TextView(this);
        accuracyDisplay.setText("Unknown");
        accuracyDisplay.setTextSize(14);
        accuracyDisplay.setTextColor(0xFF4CAF50);
        accuracyDisplay.setPadding(0, 0, 0, 15);
        locationContainer.addView(accuracyDisplay);
        
        // Last Update Display
        TextView updateLabel = new TextView(this);
        updateLabel.setText("⏰ Last Update:");
        updateLabel.setTextSize(16);
        updateLabel.setTextColor(0xFFB0BEC5);
        updateLabel.setPadding(0, 0, 0, 10);
        locationContainer.addView(updateLabel);
        
        lastUpdateDisplay = new TextView(this);
        lastUpdateDisplay.setText("Never");
        lastUpdateDisplay.setTextSize(14);
        lastUpdateDisplay.setTextColor(0xFFFF9800);
        locationContainer.addView(lastUpdateDisplay);
        
        mainLayout.addView(locationContainer);
        
        // Add spacing
        android.view.View spacer = new android.view.View(this);
        spacer.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 30));
        mainLayout.addView(spacer);
        
        // Refresh Button
        refreshButton = createStyledButton("🔄 Refresh Location", 0xFF4CAF50);
        refreshButton.setOnClickListener(v -> refreshLocation());
        mainLayout.addView(refreshButton);
        
        // Back Button
        backButton = createStyledButton("← Back to Main", 0xFF757575);
        backButton.setOnClickListener(v -> finish());
        mainLayout.addView(backButton);
        
        setContentView(mainLayout);
        
        // Initial load
        refreshLocation();
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
    
    private void refreshLocation() {
        float latitude = prefs.getFloat("last_latitude", 0);
        float longitude = prefs.getFloat("last_longitude", 0);
        float accuracy = prefs.getFloat("location_accuracy", 0);
        long lastUpdate = prefs.getLong("last_location_time", 0);
        
        if (latitude != 0 && longitude != 0) {
            // Display location
            locationDisplay.setText(String.format(Locale.getDefault(), 
                "%.6f°N, %.6f°E", latitude, longitude));
            locationDisplay.setTextColor(0xFFFFFFFF);
            
            // Display accuracy
            if (accuracy > 0) {
                accuracyDisplay.setText(String.format(Locale.getDefault(), "±%.0f meters", accuracy));
                
                // Color code accuracy
                if (accuracy <= 10) {
                    accuracyDisplay.setTextColor(0xFF4CAF50); // Excellent
                } else if (accuracy <= 50) {
                    accuracyDisplay.setTextColor(0xFFFF9800); // Good
                } else {
                    accuracyDisplay.setTextColor(0xFFF44336); // Poor
                }
            } else {
                accuracyDisplay.setText("Unknown");
                accuracyDisplay.setTextColor(0xFF757575);
            }
            
            // Display last update time
            if (lastUpdate > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault());
                lastUpdateDisplay.setText(sdf.format(new Date(lastUpdate)));
                
                // Check if location is recent
                long timeDiff = System.currentTimeMillis() - lastUpdate;
                if (timeDiff < 60000) { // Less than 1 minute
                    lastUpdateDisplay.setTextColor(0xFF4CAF50);
                    statusDisplay.setText("🟢 Live Tracking Active");
                    statusDisplay.setTextColor(0xFF4CAF50);
                } else if (timeDiff < 300000) { // Less than 5 minutes
                    lastUpdateDisplay.setTextColor(0xFFFF9800);
                    statusDisplay.setText("🟡 Recent Location Available");
                    statusDisplay.setTextColor(0xFFFF9800);
                } else {
                    lastUpdateDisplay.setTextColor(0xFFF44336);
                    statusDisplay.setText("🔴 Location Data Outdated");
                    statusDisplay.setTextColor(0xFFF44336);
                }
            } else {
                lastUpdateDisplay.setText("Never");
                lastUpdateDisplay.setTextColor(0xFF757575);
                statusDisplay.setText("⚠️ No Location Data");
                statusDisplay.setTextColor(0xFFF44336);
            }
            
            // Check for theft detection
            boolean theftDetected = prefs.getBoolean("theft_detected", false);
            if (theftDetected) {
                statusDisplay.setText("🚨 THEFT DETECTED!");
                statusDisplay.setTextColor(0xFFF44336);
                
                // Show theft alert
                showTheftAlert();
            }
            
        } else {
            locationDisplay.setText("No location data available");
            locationDisplay.setTextColor(0xFF757575);
            accuracyDisplay.setText("Unknown");
            accuracyDisplay.setTextColor(0xFF757575);
            lastUpdateDisplay.setText("Never");
            lastUpdateDisplay.setTextColor(0xFF757575);
            statusDisplay.setText("❌ Location Service Inactive");
            statusDisplay.setTextColor(0xFFF44336);
        }
    }
    
    private void showTheftAlert() {
        long theftTime = prefs.getLong("theft_time", 0);
        float theftLat = prefs.getFloat("theft_latitude", 0);
        float theftLng = prefs.getFloat("theft_longitude", 0);
        
        if (theftTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault());
            String theftTimeStr = sdf.format(new Date(theftTime));
            
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("🚨 THEFT ALERT")
                   .setMessage("Unauthorized movement detected!\n\n" +
                              "Time: " + theftTimeStr + "\n" +
                              "Location: " + String.format(Locale.getDefault(), 
                                  "%.6f°N, %.6f°E", theftLat, theftLng) + "\n\n" +
                              "Your device has been moved without authorization. " +
                              "All security features are now active.")
                   .setPositiveButton("Acknowledge", (dialog, which) -> {
                       // Clear theft flag
                       prefs.edit().putBoolean("theft_detected", false).apply();
                       refreshLocation();
                   })
                   .setCancelable(false)
                   .show();
        }
    }
    
    private void startAutoRefresh() {
        updateHandler = new Handler();
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                refreshLocation();
                updateHandler.postDelayed(this, 5000); // Refresh every 5 seconds
            }
        };
        updateHandler.post(updateRunnable);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }
}