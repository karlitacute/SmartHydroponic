package com.example.smarthydroponic.pump

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class PumpAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pumpKey = intent.getStringExtra("pump_key") ?: return
        val turnOn  = intent.getBooleanExtra("turn_on", false)

        Log.d("PumpAlarmReceiver", "Alarm triggered: $pumpKey = $turnOn")

        val dbRef = FirebaseDatabase.getInstance(
            "https://smarthydroponic-303e9-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference.child("relay")

        dbRef.child(pumpKey).setValue(turnOn)
            .addOnSuccessListener {
                Log.d("PumpAlarmReceiver", "Firebase updated: $pumpKey = $turnOn")
            }
            .addOnFailureListener { e ->
                Log.e("PumpAlarmReceiver", "Gagal update Firebase: ${e.message}")
            }
    }
}