package com.example.smarthydroponic

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.net.Network
import android.net.ConnectivityManager.NetworkCallback

import android.content.Intent

class HomeActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvTemp = findViewById<TextView>(R.id.tvTemperature)
        val tvPH = findViewById<TextView>(R.id.tvPH)
        val tvTDS = findViewById<TextView>(R.id.tvTDS)
        val tvUV = findViewById<TextView>(R.id.tvUV)

        val tvPumpStatus = findViewById<TextView>(R.id.tvPumpStatus)
        val switchPump = findViewById<Switch>(R.id.switchPump)

        val tvSensor = findViewById<TextView>(R.id.tvSensorStatus)
        val tvWater = findViewById<TextView>(R.id.tvWaterStatus)
        val tvInternet = findViewById<TextView>(R.id.tvInternetStatus)
        val tvWifiTop = findViewById<TextView>(R.id.tvStatusAtas)

        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> true

                R.id.nav_chart -> {
                    startActivity(Intent(this, ChartActivity::class.java))
                    true
                }

                R.id.nav_setting -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                    true
                }

                else -> false
            }
        }

        // REALTIME INTERNET LISTENER
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : NetworkCallback() {

            override fun onAvailable(network: Network) {
                runOnUiThread {
                    tvInternet.text = "Online"
                    tvInternet.setTextColor(getColor(R.color.green))

                    tvWifiTop.text = "Online"
                    tvWifiTop.setBackgroundResource(R.drawable.btn_online)
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    tvInternet.text = "Offline"
                    tvInternet.setTextColor(getColor(R.color.brightred))

                    tvWifiTop.text = "Offline"
                    tvWifiTop.setBackgroundResource(R.drawable.btn_offline)
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        // SWITCH PUMP
        switchPump.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvPumpStatus.text = "ON"
                tvWater.text = "Running"
            } else {
                tvPumpStatus.text = "OFF"
                tvWater.text = "Stopped"
            }
        }

        // MONITORING AUTO
        handler = Handler(Looper.getMainLooper())

        handler.post(object : Runnable {
            override fun run() {

                val temp = (25..32).random()
                val ph = (5..8).random()
                val tds = (600..900).random()
                val uv = (1..5).random()

                tvTemp.text = "$temp°C"
                tvPH.text = ph.toString()
                tvTDS.text = "$tds ppm"
                tvUV.text = uv.toString()

                // STATUS SYSTEM
                if (temp in 25..30 && ph in 6..7) {
                    tvSensor.text = "Normal"
                    tvSensor.setTextColor(getColor(R.color.green))
                } else {
                    tvSensor.text = "Warning"
                    tvSensor.setTextColor(getColor(R.color.brightred))
                }

                handler.postDelayed(this, 3000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}