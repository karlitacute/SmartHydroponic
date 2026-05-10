package com.example.smarthydroponic

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.ConnectivityManager.NetworkCallback
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    private lateinit var handler: Handler
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkCallback

    private var isConnected = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom
            )
            insets
        }

        val imgProfile =
            view.findViewById<ImageView>(R.id.imgProfile)

        imgProfile.setOnClickListener {

            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        val imgWifi = view.findViewById<ImageView>(R.id.imgWifi)
        val tvSystemDesc = view.findViewById<TextView>(R.id.tvSystemDesc)
        val bgStatus = view.findViewById<LinearLayout>(R.id.bgStatus)

        val tvTemp = view.findViewById<TextView>(R.id.tvTemperature)
        val tvPH = view.findViewById<TextView>(R.id.tvPH)
        val tvTDS = view.findViewById<TextView>(R.id.tvTDS)
        val tvUV = view.findViewById<TextView>(R.id.tvUV)

        val tvPumpStatus = view.findViewById<TextView>(R.id.tvPumpStatus)
        val switchPump = view.findViewById<Switch>(R.id.switchPump)

        val tvSensor = view.findViewById<TextView>(R.id.tvSensorStatus)
        val tvWater = view.findViewById<TextView>(R.id.tvWaterStatus)
        val tvInternet = view.findViewById<TextView>(R.id.tvInternetStatus)
        val tvWifiTop = view.findViewById<TextView>(R.id.tvStatusAtas)

        if (switchPump.isChecked) {
            tvPumpStatus.text = "ON"
            tvWater.text = "Running"
            tvWater.setTextColor(
                resources.getColor(R.color.green)
            )
        } else {
            tvPumpStatus.text = "OFF"
            tvWater.text = "Stopped"
            tvWater.setTextColor(
                resources.getColor(R.color.brightred)
            )
        }
        connectivityManager =
            requireContext().getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        networkCallback = object : NetworkCallback() {

            override fun onAvailable(network: Network) {

                activity?.runOnUiThread {

                    isConnected = true

                    bgStatus.setBackgroundResource(
                        R.drawable.bg_status_online
                    )

                    tvInternet.text = "Online"

                    tvInternet.setTextColor(
                        resources.getColor(R.color.green)
                    )

                    tvWifiTop.text = "Online"

                    tvWifiTop.setBackgroundResource(
                        R.drawable.btn_online
                    )

                    tvWifiTop.setTextColor(
                        resources.getColor(R.color.green)
                    )

                    imgWifi.setImageResource(
                        R.drawable.wifi
                    )

                    tvSystemDesc.text =
                        "The system runs normally"
                }
            }

            override fun onLost(network: Network) {

                activity?.runOnUiThread {

                    isConnected = false

                    bgStatus.setBackgroundResource(
                        R.drawable.bg_status_offline
                    )

                    tvInternet.text = "Offline"

                    tvInternet.setTextColor(
                        resources.getColor(R.color.brightred)
                    )

                    tvWifiTop.text = "Offline"

                    tvWifiTop.setBackgroundResource(
                        R.drawable.btn_offline
                    )

                    tvWifiTop.setTextColor(
                        resources.getColor(R.color.brightred)
                    )

                    imgWifi.setImageResource(
                        R.drawable.ic_wifioff
                    )

                    tvSystemDesc.text =
                        "No internet connection"
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(
            networkCallback
        )

        switchPump.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                tvPumpStatus.text = "ON"

                tvWater.text = "Running"

                tvWater.setTextColor(
                    resources.getColor(R.color.green)
                )

            } else {

                tvPumpStatus.text = "OFF"

                tvWater.text = "Stopped"

                tvWater.setTextColor(
                    resources.getColor(R.color.brightred)
                )
            }
        }

        handler = Handler(Looper.getMainLooper())

        handler.post(object : Runnable {

            override fun run() {

                if (isConnected) {

                    val temp = (25..32).random()
                    val ph = (5..8).random()
                    val tds = (600..900).random()
                    val uv = (1..5).random()

                    tvTemp.text = "$temp°C"
                    tvPH.text = ph.toString()
                    tvTDS.text = "$tds ppm"
                    tvUV.text = uv.toString()

                    if (temp in 25..30 && ph in 6..7) {

                        tvSensor.text = "Normal"

                        tvSensor.setTextColor(
                            resources.getColor(R.color.green)
                        )

                    } else {

                        tvSensor.text = "Warning"

                        tvSensor.setTextColor(
                            resources.getColor(R.color.brightred)
                        )
                    }
                }

                handler.postDelayed(this, 3000)
            }
        })

        return view
    }

    override fun onDestroyView() {

        super.onDestroyView()

        try {

            connectivityManager.unregisterNetworkCallback(
                networkCallback
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
        handler.removeCallbacksAndMessages(null)
    }
}