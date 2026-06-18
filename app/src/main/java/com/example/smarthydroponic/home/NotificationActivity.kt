package com.example.smarthydroponic.home

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smarthydroponic.R

class NotificationActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var switchPH: Switch
    private lateinit var switchTDS: Switch
    private lateinit var switchUV: Switch

    companion object {
        const val PREF_NAME = "notif_settings"
        const val KEY_PH = "switch_ph"
        const val KEY_TDS = "switch_tds"
        const val KEY_UV = "switch_uv"

        const val CHANNEL_ID = "hydroponic_alerts"

        const val PH_MIN = 6.0
        const val PH_MAX = 7.0

        const val TDS_MIN = 560
        const val TDS_MAX = 840

        const val UV_MIN = 2.0
        const val UV_MAX = 4.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createNotificationChannel()

        btnBack = findViewById(R.id.btnBack)
        switchPH = findViewById(R.id.switchPH)
        switchTDS = findViewById(R.id.switchTDS)
        switchUV = findViewById(R.id.switchUV)

        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        switchPH.isChecked = prefs.getBoolean(KEY_PH, true)
        switchTDS.isChecked = prefs.getBoolean(KEY_TDS, true)
        switchUV.isChecked = prefs.getBoolean(KEY_UV, true)

        btnBack.setOnClickListener {
            finish()
        }

        switchPH.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean(KEY_PH, isChecked) }
            showToast("pH", isChecked)
        }

        switchTDS.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean(KEY_TDS, isChecked) }
            showToast("TDS", isChecked)
        }

        switchUV.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit { putBoolean(KEY_UV, isChecked) }
            showToast("UV", isChecked)
        }
    }

    private fun showToast(name: String, isChecked: Boolean) {
        val status = if (isChecked) "ON" else "OFF"
        Toast.makeText(this, "$name Notification $status", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Hydroponic Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi peringatan sensor hidroponik"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}