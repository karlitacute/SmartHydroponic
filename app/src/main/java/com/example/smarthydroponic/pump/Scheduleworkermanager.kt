package com.example.smarthydroponic.pump

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import org.json.JSONArray
import java.util.Calendar
import java.util.TimeZone

object ScheduleWorkManager {

    fun rescheduleAll(context: Context, userId: String) {
        val prefs = context.getSharedPreferences("schedule_data", Context.MODE_PRIVATE)
        val raw   = prefs.getString("schedules_$userId", "[]") ?: "[]"
        val array = try { JSONArray(raw) } catch (e: Exception) { JSONArray() }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (i in 0..99) {
            cancelAlarm(context, alarmManager, i * 2)
            cancelAlarm(context, alarmManager, i * 2 + 1)
        }

        for (i in 0 until array.length()) {
            val obj        = array.getJSONObject(i)
            val isActive   = obj.optBoolean("is_active", true)
            if (!isActive) continue

            val name       = obj.getString("name")
            val start      = obj.getString("start")
            val end        = obj.getString("end")
            val firebaseId = obj.optString("firebase_key", "")

            val pumpKey = when (name) {
                "Nutrition Pump A" -> "pompa_nutritionA"
                "Nutrition Pump B" -> "pompa_nutritionB"
                else -> continue
            }

            setAlarmClock(
                context, alarmManager,
                requestCode = i * 2,
                timeStr = start,
                pumpKey = pumpKey,
                turnOn = true,
                firebaseId = firebaseId
            )

            setAlarmClock(
                context, alarmManager,
                requestCode = i * 2 + 1,
                timeStr = end,
                pumpKey = pumpKey,
                turnOn = false,
                firebaseId = firebaseId
            )

            Log.d("ScheduleWorkManager", "Alarm set: $name ON=$start OFF=$end firebaseId=$firebaseId")
        }
    }

    private fun setAlarmClock(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        timeStr: String,
        pumpKey: String,
        turnOn: Boolean,
        firebaseId: String
    ) {
        val parts = timeStr.trim().split(":")
        if (parts.size < 2) return

        val hour   = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        val second = if (parts.size >= 3) parts[2].toIntOrNull() ?: 0 else 0

        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, PumpAlarmReceiver::class.java).apply {
            putExtra("pump_key", pumpKey)
            putExtra("turn_on", turnOn)
            putExtra("firebase_id", firebaseId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(cal.timeInMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)

        Log.d("ScheduleWorkManager", "AlarmClock set: $pumpKey turnOn=$turnOn at $timeStr (next: ${cal.time})")
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, PumpAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pi)
        pi.cancel()
    }
}