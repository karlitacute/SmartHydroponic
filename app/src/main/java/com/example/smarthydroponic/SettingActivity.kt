package com.example.smarthydroponic

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
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

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)

        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val itemPump = findViewById<View>(R.id.itemPump)
        val itemSchedule = findViewById<View>(R.id.itemSchedule)
        val itemNotif = findViewById<View>(R.id.itemNotif)
        val itemLogout = findViewById<View>(R.id.itemLogout)

        itemPump.setOnClickListener {
            startActivity(Intent(this, PumpControlActivity::class.java))
        }

        itemSchedule.setOnClickListener {
            startActivity(Intent(this, PumpScheduleActivity::class.java))
        }

        itemNotif.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        itemLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}