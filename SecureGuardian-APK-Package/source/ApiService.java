package com.antitheft.pro.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiService {
    
    private static final String TAG = "ApiService";
    private Context context;
    private SharedPreferences prefs;
    private ExecutorService executor;
    private String serverUrl;
    
    public ApiService(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("AntiTheftPro", Context.MODE_PRIVATE);
        this.executor = Executors.newFixedThreadPool(3);
        this.serverUrl = prefs.getString("server_url", "http://localhost:8080");
    }
    
    // Sync device status with web dashboard
    public void syncDeviceStatus() {
        executor.execute(() -> {
            try {
                JSONObject deviceData = new JSONObject();
                
                // Battery info
                float batteryLevel = getBatteryLevel();
                deviceData.put("battery_level", batteryLevel);
                deviceData.put("battery_status", batteryLevel > 20 ? "good" : "low");
                
                // Location info
                float latitude = prefs.getFloat("last_latitude", 0);
                float longitude = prefs.getFloat("last_longitude", 0);
                float accuracy = prefs.getFloat("location_accuracy", 0);
                
                if (latitude != 0 && longitude != 0) {
                    JSONObject location = new JSONObject();
                    location.put("latitude", latitude);
                    location.put("longitude", longitude);
                    location.put("accuracy", accuracy);
                    location.put("timestamp", System.currentTimeMillis());
                    deviceData.put("location", location);
                }
                
                // Security status
                deviceData.put("security_status", getSecurityStatus());
                deviceData.put("tracking_active", prefs.getBoolean("location_tracking", false));
                deviceData.put("intruder_detection", prefs.getBoolean("intruder_detection", false));
                deviceData.put("emergency_mode", prefs.getBoolean("emergency_mode", false));
                
                // Device info
                deviceData.put("device_id", getDeviceId());
                deviceData.put("last_sync", System.currentTimeMillis());
                
                // Send to server
                sendPostRequest("/api/device/sync", deviceData);
                
                Log.d(TAG, "Device status synced successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Error syncing device status", e);
            }
        });
    }
    
    // Validate PIN with server
    public void validatePin(String email, String pin, PinValidationCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject pinData = new JSONObject();
                pinData.put("email", email);
                pinData.put("pin", pin);
                pinData.put("device_id", getDeviceId());
                pinData.put("timestamp", System.currentTimeMillis());
                
                String response = sendPostRequest("/api/pin/validate", pinData);
                
                if (response != null) {
                    JSONObject result = new JSONObject(response);
                    boolean isValid = result.optBoolean("valid", false);
                    String message = result.optString("message", "");
                    
                    // Run callback on main thread
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> 
                            callback.onResult(isValid, message));
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error validating PIN", e);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> 
                        callback.onResult(false, "Network error"));
                }
            }
        });
    }
    
    // Send location update to server
    public void sendLocationUpdate(double latitude, double longitude, float accuracy) {
        executor.execute(() -> {
            try {
                JSONObject locationData = new JSONObject();
                locationData.put("device_id", getDeviceId());
                locationData.put("latitude", latitude);
                locationData.put("longitude", longitude);
                locationData.put("accuracy", accuracy);
                locationData.put("timestamp", System.currentTimeMillis());
                locationData.put("battery_level", getBatteryLevel());
                
                sendPostRequest("/api/location/update", locationData);
                
                Log.d(TAG, "Location updated: " + latitude + ", " + longitude);
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending location update", e);
            }
        });
    }
    
    // Send security alert to server
    public void sendSecurityAlert(String alertType, String message) {
        executor.execute(() -> {
            try {
                JSONObject alertData = new JSONObject();
                alertData.put("device_id", getDeviceId());
                alertData.put("alert_type", alertType);
                alertData.put("message", message);
                alertData.put("timestamp", System.currentTimeMillis());
                
                // Add location if available
                float latitude = prefs.getFloat("last_latitude", 0);
                float longitude = prefs.getFloat("last_longitude", 0);
                if (latitude != 0 && longitude != 0) {
                    alertData.put("latitude", latitude);
                    alertData.put("longitude", longitude);
                }
                
                sendPostRequest("/api/security/alert", alertData);
                
                Log.d(TAG, "Security alert sent: " + alertType);
                
            } catch (Exception e) {
                Log.e(TAG, "Error sending security alert", e);
            }
        });
    }
    
    // Register device with server
    public void registerDevice(String email, DeviceRegistrationCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject deviceData = new JSONObject();
                deviceData.put("device_id", getDeviceId());
                deviceData.put("email", email);
                deviceData.put("device_name", android.os.Build.MODEL);
                deviceData.put("android_version", android.os.Build.VERSION.RELEASE);
                deviceData.put("app_version", "1.0.0");
                deviceData.put("registration_time", System.currentTimeMillis());
                
                String response = sendPostRequest("/api/device/register", deviceData);
                
                if (response != null) {
                    JSONObject result = new JSONObject(response);
                    boolean success = result.optBoolean("success", false);
                    String message = result.optString("message", "");
                    
                    if (success) {
                        // Save registration info
                        prefs.edit()
                             .putString("registered_email", email)
                             .putBoolean("device_registered", true)
                             .putLong("registration_time", System.currentTimeMillis())
                             .apply();
                    }
                    
                    // Run callback on main thread
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> 
                            callback.onResult(success, message));
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error registering device", e);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> 
                        callback.onResult(false, "Registration failed"));
                }
            }
        });
    }
    
    // Generic POST request method
    private String sendPostRequest(String endpoint, JSONObject data) {
        try {
            URL url = new URL(serverUrl + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "SecureGuardian-Android/1.0");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000); // 10 seconds
            conn.setReadTimeout(15000); // 15 seconds
            
            // Send data
            OutputStream os = conn.getOutputStream();
            os.write(data.toString().getBytes("UTF-8"));
            os.close();
            
            // Read response
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                return response.toString();
            } else {
                Log.w(TAG, "Server returned code: " + responseCode);
                return null;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Network request failed", e);
            return null;
        }
    }
    
    // Get battery level
    private float getBatteryLevel() {
        try {
            android.content.IntentFilter ifilter = new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED);
            android.content.Intent batteryStatus = context.registerReceiver(null, ifilter);
            
            int level = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1);
            
            return (level / (float) scale) * 100;
        } catch (Exception e) {
            return 50.0f; // Default value
        }
    }
    
    // Get security status
    private String getSecurityStatus() {
        boolean emergencyMode = prefs.getBoolean("emergency_mode", false);
        boolean locationTracking = prefs.getBoolean("location_tracking", false);
        boolean intruderDetection = prefs.getBoolean("intruder_detection", false);
        
        if (emergencyMode) {
            return "emergency";
        } else if (locationTracking && intruderDetection) {
            return "full_protection";
        } else if (locationTracking || intruderDetection) {
            return "partial_protection";
        } else {
            return "disabled";
        }
    }
    
    // Get unique device ID
    private String getDeviceId() {
        String deviceId = prefs.getString("device_id", "");
        if (deviceId.isEmpty()) {
            deviceId = "android_" + android.os.Build.SERIAL + "_" + System.currentTimeMillis();
            prefs.edit().putString("device_id", deviceId).apply();
        }
        return deviceId;
    }
    
    // Update server URL
    public void updateServerUrl(String newUrl) {
        this.serverUrl = newUrl;
        prefs.edit().putString("server_url", newUrl).apply();
    }
    
    // Callback interfaces
    public interface PinValidationCallback {
        void onResult(boolean isValid, String message);
    }
    
    public interface DeviceRegistrationCallback {
        void onResult(boolean success, String message);
    }
    
    // Start periodic sync
    public void startPeriodicSync() {
        // Sync every 30 seconds
        executor.execute(() -> {
            while (true) {
                try {
                    syncDeviceStatus();
                    Thread.sleep(30000); // 30 seconds
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "Error in periodic sync", e);
                }
            }
        });
    }
    
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}