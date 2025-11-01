package com.antitheft.pro;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.antitheft.pro.activities.PinGeneratorActivity;
import com.antitheft.pro.activities.TrackingActivity;
import com.antitheft.pro.activities.SettingsActivity;
import com.antitheft.pro.activities.IntrudersActivity;
import com.antitheft.pro.activities.DeviceRegistrationActivity;
import com.antitheft.pro.services.LocationTrackingService;
import com.antitheft.pro.utils.SecurityManager;
import com.antitheft.pro.utils.PermissionManager;
import com.antitheft.pro.api.ApiService;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private SecurityManager securityManager;
    private PermissionManager permissionManager;
    private SharedPreferences prefs;
    private ApiService apiService;
    
    // UI Elements
    private TextView statusText;
    private TextView locationStatus;
    private TextView deviceStatus;
    private Button pinGeneratorButton;
    private Button trackingButton;
    private Button intrudersButton;
    private Button settingsButton;
    private Button emergencyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            // Initialize managers
            securityManager = new SecurityManager(this);
            permissionManager = new PermissionManager(this);
            prefs = getSharedPreferences("AntiTheftPro", MODE_PRIVATE);
            apiService = new ApiService(this);
            
            // Check if device is registered
            if (!prefs.getBoolean("device_registered", false) && 
                !prefs.getBoolean("registration_skipped", false)) {
                // Go to registration activity
                Intent registrationIntent = new Intent(this, DeviceRegistrationActivity.class);
                startActivity(registrationIntent);
                finish();
                return;
            }
            
            // Create UI programmatically for better control
            createMainInterface();
            
            // Check permissions
            checkAndRequestPermissions();
            
            // Initialize security features
            initializeSecurity();
            
            // Update status
            updateDeviceStatus();
            
            // Start periodic sync with server
            apiService.startPeriodicSync();
            
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في تحميل التطبيق: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }
    
    private void createMainInterface() {
        // Create main layout programmatically with modern design
        android.widget.LinearLayout mainLayout = new android.widget.LinearLayout(this);
        mainLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 60, 40, 40);
        mainLayout.setBackgroundColor(0xFF1A1A2E);
        
        // App Title
        TextView titleText = new TextView(this);
        titleText.setText("🛡️ Secure Guardian Pro");
        titleText.setTextSize(28);
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 40);
        titleText.setTypeface(null, android.graphics.Typeface.BOLD);
        mainLayout.addView(titleText);
        
        // Status Section
        android.widget.LinearLayout statusLayout = new android.widget.LinearLayout(this);
        statusLayout.setOrientation(android.widget.LinearLayout.VERTICAL);
        statusLayout.setBackgroundColor(0xFF16213E);
        statusLayout.setPadding(30, 25, 30, 25);
        statusLayout.setBackground(createRoundedBackground(0xFF16213E, 20));
        
        statusText = new TextView(this);
        statusText.setText("📱 Device Status: Protected");
        statusText.setTextSize(16);
        statusText.setTextColor(0xFF4CAF50);
        statusText.setPadding(0, 0, 0, 10);
        statusLayout.addView(statusText);
        
        locationStatus = new TextView(this);
        locationStatus.setText("📍 Location: Tracking Active");
        locationStatus.setTextSize(14);
        locationStatus.setTextColor(0xFF2196F3);
        locationStatus.setPadding(0, 0, 0, 10);
        statusLayout.addView(locationStatus);
        
        deviceStatus = new TextView(this);
        deviceStatus.setText("🔒 Security: All Systems Online");
        deviceStatus.setTextSize(14);
        deviceStatus.setTextColor(0xFF4CAF50);
        statusLayout.addView(deviceStatus);
        
        mainLayout.addView(statusLayout);
        
        // Add spacing
        android.view.View spacer1 = new android.view.View(this);
        spacer1.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 30));
        mainLayout.addView(spacer1);
        
        // PIN Generator Button (Main Feature)
        pinGeneratorButton = createStyledButton("🔑 Generate Web PIN", 0xFF4CAF50);
        pinGeneratorButton.setOnClickListener(v -> openPinGenerator());
        mainLayout.addView(pinGeneratorButton);
        
        // Tracking Button
        trackingButton = createStyledButton("📍 Live Tracking", 0xFF2196F3);
        trackingButton.setOnClickListener(v -> openTracking());
        mainLayout.addView(trackingButton);
        
        // Intruders Button
        intrudersButton = createStyledButton("👁️ Intruder Detection", 0xFFFF9800);
        intrudersButton.setOnClickListener(v -> openIntruders());
        mainLayout.addView(intrudersButton);
        
        // Settings Button
        settingsButton = createStyledButton("⚙️ Settings", 0xFF9C27B0);
        settingsButton.setOnClickListener(v -> openSettings());
        mainLayout.addView(settingsButton);
        
        // Emergency Button
        emergencyButton = createStyledButton("🚨 EMERGENCY MODE", 0xFFF44336);
        emergencyButton.setOnClickListener(v -> activateEmergencyMode());
        mainLayout.addView(emergencyButton);
        
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
        params.setMargins(0, 0, 0, 20);
        button.setLayoutParams(params);
        
        return button;
    }
    
    private android.graphics.drawable.GradientDrawable createRoundedBackground(int color, int radius) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius * getResources().getDisplayMetrics().density);
        return drawable;
    }
    
    private void checkAndRequestPermissions() {
        String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.FOREGROUND_SERVICE
        };
        
        if (!permissionManager.hasAllPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }
    
    private void initializeSecurity() {
        // Start location tracking service
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        startForegroundService(serviceIntent);
        
        // Initialize security features
        securityManager.initializeSecurity();
        
        Toast.makeText(this, "🛡️ Security systems activated", Toast.LENGTH_SHORT).show();
    }
    
    private void updateDeviceStatus() {
        // Update status indicators
        boolean isLocationEnabled = permissionManager.isLocationPermissionGranted();
        boolean isCameraEnabled = permissionManager.isCameraPermissionGranted();
        
        if (isLocationEnabled && isCameraEnabled) {
            statusText.setText("📱 Device Status: Fully Protected");
            statusText.setTextColor(0xFF4CAF50);
        } else {
            statusText.setText("📱 Device Status: Partial Protection");
            statusText.setTextColor(0xFFFF9800);
        }
        
        locationStatus.setText(isLocationEnabled ? 
            "📍 Location: Active" : "📍 Location: Permission Required");
        locationStatus.setTextColor(isLocationEnabled ? 0xFF4CAF50 : 0xFFF44336);
    }
    
    private void openPinGenerator() {
        Intent intent = new Intent(this, PinGeneratorActivity.class);
        startActivity(intent);
    }
    
    private void openTracking() {
        if (permissionManager.isLocationPermissionGranted()) {
            Intent intent = new Intent(this, TrackingActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Location permission required for tracking", Toast.LENGTH_SHORT).show();
            checkAndRequestPermissions();
        }
    }
    
    private void openIntruders() {
        Intent intent = new Intent(this, IntrudersActivity.class);
        startActivity(intent);
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    
    private void activateEmergencyMode() {
        // Activate emergency features
        securityManager.activateEmergencyMode();
        
        // Show confirmation
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("🚨 Emergency Mode Activated")
               .setMessage("All security features are now active:\n\n" +
                          "• Location tracking enabled\n" +
                          "• Camera monitoring active\n" +
                          "• Intruder detection on\n" +
                          "• Remote wipe ready")
               .setPositiveButton("OK", null)
               .show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            updateDeviceStatus();
            
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Toast.makeText(this, "✅ All permissions granted", Toast.LENGTH_SHORT).show();
                initializeSecurity();
            } else {
                Toast.makeText(this, "⚠️ Some permissions denied. App functionality may be limited.", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateDeviceStatus();
    }
}