package com.example.smarthydroponic

import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NotificationActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var switchPH: Switch
    private lateinit var switchTDS: Switch
    private lateinit var switchUV: Switch
    private lateinit var switchInternet: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById(R.id.btnBack)
        switchPH = findViewById(R.id.switchPH)
        switchTDS = findViewById(R.id.switchTDS)
        switchUV = findViewById(R.id.switchUV)
        switchInternet = findViewById(R.id.switchInternet)

        btnBack.setOnClickListener {
            finish()
        }

        switchPH.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) switchPH.isChecked = false
            showToast("pH", isChecked)
        }

        switchTDS.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) switchTDS.isChecked = false
            showToast("TDS", isChecked)
        }

        switchUV.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) switchUV.isChecked = false
            showToast("UV", isChecked)
        }

        switchInternet.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) switchInternet.isChecked = false
            showToast("Internet", isChecked)
        }
    }

    private fun showToast(name: String, isChecked: Boolean) {
        val status = if (isChecked) "ON" else "OFF"
        Toast.makeText(this, "$name Notification $status", Toast.LENGTH_SHORT).show()
    }
}