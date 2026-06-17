package com.example.smarthydroponic.chart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthydroponic.R
import com.example.smarthydroponic.setting.SettingFragment
import com.example.smarthydroponic.home.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class ChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, HomeFragment())
            .commit()

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, HomeFragment())
                        .commit()
                    true
                }

                R.id.nav_chart -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, ChartFragment())
                        .commit()
                    true
                }

                R.id.nav_setting -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_container, SettingFragment())
                        .commit()
                    true
                }

                else -> false
            }
        }
    }
}