package com.example.smarthydroponic

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val itemPump = findViewById<View>(R.id.itemPump)
        val itemSchedule = findViewById<View>(R.id.itemSchedule)
        val itemNotif = findViewById<View>(R.id.itemNotif)
        val itemSensor = findViewById<View>(R.id.itemSensor)
        val itemWifi = findViewById<View>(R.id.itemWifi)
        val itemLogout = findViewById<View>(R.id.itemLogout)

        itemPump.setOnClickListener {
            Toast.makeText(this, "Pump Control", Toast.LENGTH_SHORT).show()
        }

        itemSchedule.setOnClickListener {
            Toast.makeText(this, "Schedule", Toast.LENGTH_SHORT).show()
        }

        itemNotif.setOnClickListener {
            Toast.makeText(this, "Notification", Toast.LENGTH_SHORT).show()
        }

        itemSensor.setOnClickListener {
            Toast.makeText(this, "Sensor Setting", Toast.LENGTH_SHORT).show()
        }

        itemWifi.setOnClickListener {
            Toast.makeText(this, "WiFi Setting", Toast.LENGTH_SHORT).show()
        }

        itemLogout.setOnClickListener {
            logoutUser()
        }

        setupBottomNav()
    }

    private fun logoutUser() {
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setupBottomNav() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_chart -> {
                    startActivity(Intent(this, ChartActivity::class.java))
                    true
                }
                R.id.nav_setting -> true
                else -> false
            }
        }
    }
}