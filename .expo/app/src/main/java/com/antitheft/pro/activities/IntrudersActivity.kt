package com.antitheft.pro.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.antitheft.pro.R
import com.antitheft.pro.databinding.ActivityIntrudersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class IntrudersActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityIntrudersBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntrudersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        supportActionBar?.title = getString(R.string.intruders)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        setupRecyclerView()
        loadIntruderPhotos()
    }
    
    private fun setupRecyclerView() {
        binding.recyclerViewIntruders.layoutManager = GridLayoutManager(this, 2)
    }
    
    private fun loadIntruderPhotos() {
        val userId = auth.currentUser?.uid ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        binding.textViewNoIntruders.visibility = View.GONE
        
        db.collection("intruders")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                
                if (documents.isEmpty) {
                    binding.textViewNoIntruders.visibility = View.VISIBLE
                    binding.recyclerViewIntruders.visibility = View.GONE
                } else {
                    binding.textViewNoIntruders.visibility = View.GONE
                    binding.recyclerViewIntruders.visibility = View.VISIBLE
                    
                    val intruders = mutableListOf<IntruderItem>()
                    
                    for (document in documents) {
                        val imageUrl = document.getString("imageUrl") ?: continue
                        val timestamp = document.getTimestamp("timestamp")
                        val location = document.get("location") as? Map<*, *>
                        val latitude = location?.get("latitude") as? Double
                        val longitude = location?.get("longitude") as? Double
                        
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val timeStr = timestamp?.toDate()?.let { dateFormat.format(it) } ?: ""
                        
                        intruders.add(
                            IntruderItem(
                                imageUrl = imageUrl,
                                timestamp = timeStr,
                                latitude = latitude,
                                longitude = longitude
                            )
                        )
                    }
                    
                    // TODO: Setup adapter with intruders list
                    // For now, show count
                    Toast.makeText(
                        this,
                        "Found ${intruders.size} intruder photos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.textViewNoIntruders.visibility = View.VISIBLE
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    data class IntruderItem(
        val imageUrl: String,
        val timestamp: String,
        val latitude: Double?,
        val longitude: Double?
    )
}
