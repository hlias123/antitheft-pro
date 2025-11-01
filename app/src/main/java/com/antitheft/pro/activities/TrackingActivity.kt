package com.antitheft.pro.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.antitheft.pro.R
import com.antitheft.pro.databinding.ActivityTrackingBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {
    
    private lateinit var binding: ActivityTrackingBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        supportActionBar?.title = getString(R.string.tracking)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.buttonRefreshLocation.setOnClickListener {
            getCurrentLocation()
        }
        
        binding.buttonLocationHistory.setOnClickListener {
            loadLocationHistory()
        }
    }
    
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Enable location if permission granted
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true
            getCurrentLocation()
        }
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, R.string.permission_location, Toast.LENGTH_SHORT).show()
            return
        }
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                
                // Update map
                googleMap?.apply {
                    clear()
                    addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title(getString(R.string.current_location))
                    )
                    animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
                
                // Update UI
                binding.textViewLatitude.text = "Lat: ${it.latitude}"
                binding.textViewLongitude.text = "Lng: ${it.longitude}"
                binding.textViewAccuracy.text = "Accuracy: ${it.accuracy}m"
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                binding.textViewLastUpdate.text = getString(
                    R.string.last_seen,
                    dateFormat.format(Date())
                )
                
                // Save to Firestore
                saveLocationToFirestore(it)
            } ?: run {
                Toast.makeText(this, R.string.error_location, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun saveLocationToFirestore(location: Location) {
        val userId = auth.currentUser?.uid ?: return
        
        val locationData = hashMapOf(
            "userId" to userId,
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "accuracy" to location.accuracy,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        
        db.collection("locations")
            .add(locationData)
            .addOnSuccessListener {
                // Success
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun loadLocationHistory() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("locations")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                googleMap?.clear()
                
                var count = 0
                for (document in documents) {
                    val lat = document.getDouble("latitude") ?: continue
                    val lng = document.getDouble("longitude") ?: continue
                    val timestamp = document.getTimestamp("timestamp")
                    
                    val position = LatLng(lat, lng)
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val timeStr = timestamp?.toDate()?.let { dateFormat.format(it) } ?: ""
                    
                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title("Location ${++count}")
                            .snippet(timeStr)
                    )
                    
                    // Move camera to most recent location
                    if (count == 1) {
                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 12f))
                    }
                }
                
                Toast.makeText(
                    this,
                    "Loaded $count locations",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
