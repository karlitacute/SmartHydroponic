package com.example.smarthydroponic.utils

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.smarthydroponic.R
import com.example.smarthydroponic.home.NotificationActivity

object SensorNotifier {

    // Menyimpan status terakhir (true = sedang dalam kondisi tidak normal)
    private var phAlertActive = false
    private var tdsAlertActive = false
    private var uvAlertActive = false

    fun checkPH(context: Context, ph: Double) {
        val prefs = context.getSharedPreferences(NotificationActivity.PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(NotificationActivity.KEY_PH, true)) {
            phAlertActive = false
            return
        }

        val isAbnormal = ph < NotificationActivity.PH_MIN || ph > NotificationActivity.PH_MAX

        if (isAbnormal && !phAlertActive) {
            // baru jadi tidak normal -> kirim notif sekali
            sendNotification(
                context, 1, "Peringatan pH Tidak Normal",
                "Nilai pH saat ini $ph (ideal ${NotificationActivity.PH_MIN} - ${NotificationActivity.PH_MAX})"
            )
            phAlertActive = true
        } else if (!isAbnormal) {
            // sudah normal lagi -> reset, supaya kalau abnormal lagi nanti, notif muncul lagi
            phAlertActive = false
        }
    }

    fun checkTDS(context: Context, tds: Int) {
        val prefs = context.getSharedPreferences(NotificationActivity.PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(NotificationActivity.KEY_TDS, true)) {
            tdsAlertActive = false
            return
        }

        val isAbnormal = tds < NotificationActivity.TDS_MIN || tds > NotificationActivity.TDS_MAX

        if (isAbnormal && !tdsAlertActive) {
            sendNotification(
                context, 2, "Peringatan TDS Tidak Normal",
                "Nilai TDS saat ini $tds ppm (ideal ${NotificationActivity.TDS_MIN} - ${NotificationActivity.TDS_MAX} ppm)"
            )
            tdsAlertActive = true
        } else if (!isAbnormal) {
            tdsAlertActive = false
        }
    }

    fun checkUV(context: Context, uv: Double) {
        val prefs = context.getSharedPreferences(NotificationActivity.PREF_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(NotificationActivity.KEY_UV, true)) {
            uvAlertActive = false
            return
        }

        val isAbnormal = uv < NotificationActivity.UV_MIN || uv > NotificationActivity.UV_MAX

        if (isAbnormal && !uvAlertActive) {
            sendNotification(
                context, 3, "Peringatan UV Tidak Normal",
                "Nilai UV saat ini $uv (ideal ${NotificationActivity.UV_MIN} - ${NotificationActivity.UV_MAX})"
            )
            uvAlertActive = true
        } else if (!isAbnormal) {
            uvAlertActive = false
        }
    }

    private fun sendNotification(context: Context, id: Int, title: String, message: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, NotificationActivity.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(id, notification)
    }
}