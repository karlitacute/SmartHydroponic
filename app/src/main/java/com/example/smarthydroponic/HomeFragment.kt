package com.example.smarthydroponic

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.ConnectivityManager.NetworkCallback
import android.os.Bundle
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
import com.google.firebase.database.*
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: NetworkCallback
    private lateinit var database: DatabaseReference
    private var pumpNutrisiListener: ValueEventListener? = null
    private var pumpAirListener: ValueEventListener? = null
    private var tempListener: ValueEventListener? = null
    private var phListener: ValueEventListener? = null
    private var tdsListener: ValueEventListener? = null
    private var uvListener: ValueEventListener? = null
    private var isProgrammaticChange = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)
        val imgWifi = view.findViewById<ImageView>(R.id.imgWifi)
        val tvSystemDesc = view.findViewById<TextView>(R.id.tvSystemDesc)
        val bgStatus = view.findViewById<LinearLayout>(R.id.bgStatus)

        val tvTemp = view.findViewById<TextView>(R.id.tvTemperature)
        val tvPH = view.findViewById<TextView>(R.id.tvPH)
        val tvTDS = view.findViewById<TextView>(R.id.tvTDS)
        val tvUV = view.findViewById<TextView>(R.id.tvUV)

        val tvPumpStatus    = view.findViewById<TextView>(R.id.tvPumpStatus)
        val switchPump      = view.findViewById<Switch>(R.id.switchPump)

        val tvPumpAirStatus = view.findViewById<TextView>(R.id.tvPumpAirStatus)
        val switchPumpAir   = view.findViewById<Switch>(R.id.switchPumpAir)

        val tvSensor       = view.findViewById<TextView>(R.id.tvSensorStatus)
        val tvWater        = view.findViewById<TextView>(R.id.tvWaterStatus)
        val tvNutrisi      = view.findViewById<TextView>(R.id.tvNutrisiStatus)
        val tvInternet     = view.findViewById<TextView>(R.id.tvInternetStatus)
        val tvWifiTop      = view.findViewById<TextView>(R.id.tvStatusAtas)

        imgProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        database = FirebaseDatabase.getInstance(
            "https://smarthydroponic-303e9-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference

        updatePumpNutrisiUI(switchPump.isChecked, tvPumpStatus, tvWater)
        updatePumpAirUI(switchPumpAir.isChecked, tvPumpAirStatus, tvNutrisi)

        connectivityManager = requireContext().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                activity?.runOnUiThread {
                    bgStatus.setBackgroundResource(R.drawable.bg_status_online)
                    tvInternet.text = "Online"
                    tvInternet.setTextColor(resources.getColor(R.color.green))
                    tvWifiTop.text = "Online"
                    tvWifiTop.setBackgroundResource(R.drawable.btn_online)
                    tvWifiTop.setTextColor(resources.getColor(R.color.green))
                    imgWifi.setImageResource(R.drawable.wifi)
                    tvSystemDesc.text = "The system runs normally"
                }
            }

            override fun onLost(network: Network) {
                activity?.runOnUiThread {
                    bgStatus.setBackgroundResource(R.drawable.bg_status_offline)
                    tvInternet.text = "Offline"
                    tvInternet.setTextColor(resources.getColor(R.color.brightred))
                    tvWifiTop.text = "Offline"
                    tvWifiTop.setBackgroundResource(R.drawable.btn_offline)
                    tvWifiTop.setTextColor(resources.getColor(R.color.brightred))
                    imgWifi.setImageResource(R.drawable.ic_wifioff)
                    tvSystemDesc.text = "No internet connection"
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        pumpNutrisiListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pumpOn = snapshot.getValue(Boolean::class.java) ?: false
                isProgrammaticChange = true
                switchPump.isChecked = pumpOn
                isProgrammaticChange = false
                updatePumpNutrisiUI(pumpOn, tvPumpStatus, tvWater)
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.child("relay/pompa_nutrisi").addValueEventListener(pumpNutrisiListener!!)

        switchPump.setOnCheckedChangeListener { _, isChecked ->
            if (!isProgrammaticChange) {
                database.child("relay/pompa_nutrisi").setValue(isChecked)
            }
        }

        pumpAirListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pumpOn = snapshot.getValue(Boolean::class.java) ?: false
                isProgrammaticChange = true
                switchPumpAir.isChecked = pumpOn
                isProgrammaticChange = false
                updatePumpAirUI(pumpOn, tvPumpAirStatus, tvNutrisi)
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.child("relay/pompa_air").addValueEventListener(pumpAirListener!!)

        switchPumpAir.setOnCheckedChangeListener { _, isChecked ->
            if (!isProgrammaticChange) {
                database.child("relay/pompa_air").setValue(isChecked)
            }
        }

        tempListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temp = snapshot.getValue(Float::class.java) ?: return
                tvTemp.text = String.format(Locale.US, "%.1f°C", temp)
                if (temp in 25.0..30.0) {
                    tvSensor.text = "Normal"
                    tvSensor.setTextColor(resources.getColor(R.color.green))
                } else {
                    tvSensor.text = "Warning"
                    tvSensor.setTextColor(resources.getColor(R.color.brightred))
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.child("sensors/temperature").addValueEventListener(tempListener!!)

        phListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ph = snapshot.getValue(Float::class.java) ?: return
                tvPH.text = String.format(Locale.US, "%.1f", ph)
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.child("sensors/ph").addValueEventListener(phListener!!)

        tdsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tds = snapshot.getValue(Float::class.java) ?: return
                tvTDS.text = String.format(Locale.US, "%.1f ppm", tds)
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.child("sensors/tds").addValueEventListener(tdsListener!!)

        uvListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uv = snapshot.getValue(Float::class.java) ?: return
                tvUV.text = String.format(Locale.US, "%.1f", uv)
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        database.child("sensors/uv").addValueEventListener(uvListener!!)

        return view
    }
    private fun updatePumpNutrisiUI(isOn: Boolean, tvStatus: TextView, tvWater: TextView) {
        if (isOn) {
            tvStatus.text = "ON"
            tvStatus.setTextColor(resources.getColor(R.color.green))
            tvWater.text = "Running"
            tvWater.setTextColor(resources.getColor(R.color.green))
        } else {
            tvStatus.text = "OFF"
            tvStatus.setTextColor(resources.getColor(R.color.brightred))
            tvWater.text = "Stopped"
            tvWater.setTextColor(resources.getColor(R.color.brightred))
        }
    }
    private fun updatePumpAirUI(isOn: Boolean, tvStatus: TextView, tvNutrisi: TextView) {
        if (isOn) {
            tvStatus.text = "ON"
            tvStatus.setTextColor(resources.getColor(R.color.green))
            tvNutrisi.text = "Running"
            tvNutrisi.setTextColor(resources.getColor(R.color.green))
        } else {
            tvStatus.text = "OFF"
            tvStatus.setTextColor(resources.getColor(R.color.brightred))
            tvNutrisi.text = "Stopped"
            tvNutrisi.setTextColor(resources.getColor(R.color.brightred))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        pumpNutrisiListener?.let { database.child("relay/pompa_nutrisi").removeEventListener(it) }
        pumpAirListener?.let { database.child("relay/pompa_air").removeEventListener(it) }
        tempListener?.let { database.child("sensors/temperature").removeEventListener(it) }
        phListener?.let { database.child("sensors/ph").removeEventListener(it) }
        tdsListener?.let { database.child("sensors/tds").removeEventListener(it) }
        uvListener?.let { database.child("sensors/uv").removeEventListener(it) }

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}