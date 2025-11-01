package com.antitheft.pro.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.widget.Toast;

public class SecurityManager {
    
    private Context context;
    private SharedPreferences prefs;
    
    public SecurityManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("AntiTheftPro", Context.MODE_PRIVATE);
    }
    
    public void initializeSecurity() {
        // Initialize security features
        prefs.edit()
             .putBoolean("security_initialized", true)
             .putLong("security_init_time", System.currentTimeMillis())
             .apply();
        
        // Enable location tracking
        enableLocationTracking();
        
        // Enable intruder detection
        enableIntruderDetection();
    }
    
    public void activateEmergencyMode() {
        prefs.edit()
             .putBoolean("emergency_mode", true)
             .putLong("emergency_activated", System.currentTimeMillis())
             .apply();
        
        // Activate all security features
        enableLocationTracking();
        enableIntruderDetection();
        enableCameraMonitoring();
        
        Toast.makeText(context, "🚨 Emergency mode activated!", Toast.LENGTH_LONG).show();
    }
    
    public void deactivateEmergencyMode() {
        prefs.edit()
             .putBoolean("emergency_mode", false)
             .apply();
        
        Toast.makeText(context, "Emergency mode deactivated", Toast.LENGTH_SHORT).show();
    }
    
    public boolean isEmergencyModeActive() {
        return prefs.getBoolean("emergency_mode", false);
    }
    
    private void enableLocationTracking() {
        prefs.edit()
             .putBoolean("location_tracking", true)
             .apply();
    }
    
    private void enableIntruderDetection() {
        prefs.edit()
             .putBoolean("intruder_detection", true)
             .apply();
    }
    
    private void enableCameraMonitoring() {
        prefs.edit()
             .putBoolean("camera_monitoring", true)
             .apply();
    }
    
    public boolean isLocationTrackingEnabled() {
        return prefs.getBoolean("location_tracking", false);
    }
    
    public boolean isIntruderDetectionEnabled() {
        return prefs.getBoolean("intruder_detection", false);
    }
    
    public boolean isCameraMonitoringEnabled() {
        return prefs.getBoolean("camera_monitoring", false);
    }
    
    public String getSecurityStatus() {
        if (isEmergencyModeActive()) {
            return "🚨 Emergency Mode Active";
        } else if (isLocationTrackingEnabled() && isIntruderDetectionEnabled()) {
            return "🛡️ Full Protection";
        } else if (isLocationTrackingEnabled() || isIntruderDetectionEnabled()) {
            return "⚠️ Partial Protection";
        } else {
            return "❌ Protection Disabled";
        }
    }
}