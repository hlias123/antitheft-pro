package com.antitheft.pro.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.antitheft.pro.MainActivity;
import com.antitheft.pro.api.ApiService;

public class LocationTrackingService extends Service implements LocationListener {
    
    private static final String CHANNEL_ID = "LocationTrackingChannel";
    private static final int NOTIFICATION_ID = 1001;
    
    private LocationManager locationManager;
    private SharedPreferences prefs;
    private ApiService apiService;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        prefs = getSharedPreferences("AntiTheftPro", MODE_PRIVATE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        apiService = new ApiService(this);
        
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        startLocationTracking();
        return START_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Tracks device location for anti-theft protection");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🛡️ Secure Guardian Active")
            .setContentText("Location tracking is protecting your device")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }
    
    private void startLocationTracking() {
        try {
            if (locationManager != null) {
                // Request location updates
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000, // 10 seconds
                    10,    // 10 meters
                    this
                );
                
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    10000,
                    10,
                    this
                );
            }
        } catch (SecurityException e) {
            // Permission not granted
            stopSelf();
        }
    }
    
    @Override
    public void onLocationChanged(Location location) {
        // Save location to preferences
        prefs.edit()
             .putFloat("last_latitude", (float) location.getLatitude())
             .putFloat("last_longitude", (float) location.getLongitude())
             .putLong("last_location_time", System.currentTimeMillis())
             .putFloat("location_accuracy", location.getAccuracy())
             .apply();
        
        // Update notification with current location
        updateNotification(location);
        
        // Send location to server
        apiService.sendLocationUpdate(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        
        // Check if device has moved significantly (potential theft)
        checkForUnauthorizedMovement(location);
    }
    
    private void updateNotification(Location location) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🛡️ Secure Guardian Active")
            .setContentText(String.format("Location: %.6f, %.6f (±%.0fm)", 
                location.getLatitude(), location.getLongitude(), location.getAccuracy()))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build();
        
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, notification);
    }
    
    private void checkForUnauthorizedMovement(Location newLocation) {
        float lastLat = prefs.getFloat("last_latitude", 0);
        float lastLng = prefs.getFloat("last_longitude", 0);
        
        if (lastLat != 0 && lastLng != 0) {
            float[] distance = new float[1];
            Location.distanceBetween(lastLat, lastLng, 
                newLocation.getLatitude(), newLocation.getLongitude(), distance);
            
            // If moved more than 100 meters, could be theft
            if (distance[0] > 100) {
                boolean emergencyMode = prefs.getBoolean("emergency_mode", false);
                if (emergencyMode) {
                    // Send alert or trigger security measures
                    sendTheftAlert(newLocation, distance[0]);
                }
            }
        }
    }
    
    private void sendTheftAlert(Location location, float distance) {
        // Create theft alert notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 THEFT ALERT")
            .setContentText(String.format("Device moved %.0f meters! Location: %.6f, %.6f", 
                distance, location.getLatitude(), location.getLongitude()))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);
        
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(9999, builder.build());
        
        // Save theft event
        prefs.edit()
             .putBoolean("theft_detected", true)
             .putLong("theft_time", System.currentTimeMillis())
             .putFloat("theft_latitude", (float) location.getLatitude())
             .putFloat("theft_longitude", (float) location.getLongitude())
             .apply();
        
        // Send theft alert to server
        apiService.sendSecurityAlert("theft_detected", 
            String.format("Device moved %.0f meters without authorization", distance));
    }
    
    @Override
    public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}
    
    @Override
    public void onProviderEnabled(String provider) {}
    
    @Override
    public void onProviderDisabled(String provider) {}
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}