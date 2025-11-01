package com.antitheft.pro.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.antitheft.pro.activities.PinLockActivity
import com.antitheft.pro.utils.PreferenceManager

class PowerButtonReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                val prefManager = PreferenceManager(context)
                if (prefManager.isPinEnabled()) {
                    // Reset PIN verification status
                    prefManager.setPinVerified(false)
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                val prefManager = PreferenceManager(context)
                if (prefManager.isPinEnabled() && !prefManager.isPinVerified()) {
                    // Show PIN lock screen
                    val pinIntent = Intent(context, PinLockActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(pinIntent)
                }
            }
        }
    }
}
