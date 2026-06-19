package com.example.smarthydroponic.pump

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class PumpAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pumpKey = intent.getStringExtra("pump_key") ?: run {
            Log.e("PumpAlarmReceiver", "pump_key null, skip")
            return
        }
        val turnOn     = intent.getBooleanExtra("turn_on", false)
        val firebaseId = intent.getStringExtra("firebase_id") ?: ""

        Log.d("PumpAlarmReceiver", "Alarm triggered: relay/$pumpKey = $turnOn, scheduleId=$firebaseId")

        val db = FirebaseDatabase.getInstance(
            "https://smarthydroponic-303e9-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference

        db.child("relay").child(pumpKey).setValue(turnOn)
            .addOnSuccessListener {
                Log.d("PumpAlarmReceiver", "Relay updated: relay/$pumpKey = $turnOn")
            }
            .addOnFailureListener { e ->
                Log.e("PumpAlarmReceiver", "Gagal update relay: ${e.message}")
            }

        if (firebaseId.isNotEmpty()) {
            db.child("schedule").child(firebaseId).child("is_active").setValue(turnOn)
                .addOnSuccessListener {
                    Log.d("PumpAlarmReceiver", "is_active updated: schedule/$firebaseId/is_active = $turnOn")
                }
                .addOnFailureListener { e ->
                    Log.e("PumpAlarmReceiver", "Gagal update is_active: ${e.message}")
                }
        } else {
            Log.w("PumpAlarmReceiver", "firebase_id kosong, skip update is_active")
        }

        val userPrefs = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
        val email     = userPrefs.getString("EMAIL", "") ?: ""
        val userId    = if (email.isNotEmpty()) email else "default"
        ScheduleWorkManager.rescheduleAll(context, userId)

        Log.d("PumpAlarmReceiver", "Rescheduled untuk besok")
    }
}