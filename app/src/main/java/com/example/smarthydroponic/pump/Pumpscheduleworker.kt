package com.example.smarthydroponic.pump

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class PumpScheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val pumpKey = inputData.getString("pump_key") ?: return Result.failure()
        val turnOn  = inputData.getBoolean("turn_on", false)

        Log.d("PumpScheduleWorker", "Worker triggered: relay/$pumpKey = $turnOn")

        return try {
            val dbRef = FirebaseDatabase.getInstance(
                "https://smarthydroponic-303e9-default-rtdb.asia-southeast1.firebasedatabase.app"
            ).reference.child("relay")

            dbRef.child(pumpKey).setValue(turnOn).await()
            Log.d("PumpScheduleWorker", "Firebase updated: relay/$pumpKey = $turnOn")

            val userPrefs = applicationContext.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE)
            val email = userPrefs.getString("EMAIL", "") ?: ""
            val userId = if (email.isNotEmpty()) email else "default"
            ScheduleWorkManager.rescheduleAll(applicationContext, userId)
            Log.d("PumpScheduleWorker", "Reschedule done untuk besok")

            Result.success()
        } catch (e: Exception) {
            Log.e("PumpScheduleWorker", "Gagal: ${e.message}")
            Result.retry()
        }
    }
}