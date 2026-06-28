package com.example.smarthydroponic.home

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.smarthydroponic.R
import com.example.smarthydroponic.profile.ProfileActivity
import com.example.smarthydroponic.utils.SensorNotifier
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var database: DatabaseReference
    private var pumpNutritionAListener: ValueEventListener? = null
    private var pumpNutritionBListener: ValueEventListener? = null
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

        setupBackPress()

        val imgProfile   = view.findViewById<ImageView>(R.id.imgProfile)
        val imgWifi      = view.findViewById<ImageView>(R.id.imgWifi)
        val tvSystemDesc = view.findViewById<TextView>(R.id.tvSystemDesc)
        val bgStatus     = view.findViewById<LinearLayout>(R.id.bgStatus)

        val tvPH  = view.findViewById<TextView>(R.id.tvPH)
        val tvTDS = view.findViewById<TextView>(R.id.tvTDS)
        val tvUV  = view.findViewById<TextView>(R.id.tvUV)

        val tvNutritionAStatus   = view.findViewById<TextView>(R.id.tvNutritionAStatus)
        val switchPumpNutritionA = view.findViewById<Switch>(R.id.switchPumpNutritionA)

        val tvNutritionBStatus   = view.findViewById<TextView>(R.id.tvNutritionBStatus)
        val switchPumpNutritionB = view.findViewById<Switch>(R.id.switchPumpNutritionB)

        val tvSensor      = view.findViewById<TextView>(R.id.tvSensor)
        val tvNutritionA  = view.findViewById<TextView>(R.id.tvNutritionA)
        val tvNutritionB  = view.findViewById<TextView>(R.id.tvNutritionB)
        val tvInternet    = view.findViewById<TextView>(R.id.tvInternetStatus)
        val tvWifiTop     = view.findViewById<TextView>(R.id.tvStatusAtas)

        imgProfile.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        database = FirebaseDatabase.getInstance(
            "https://smarthydroponic-303e9-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference

        updatePumpAUI(switchPumpNutritionA.isChecked, tvNutritionAStatus, tvNutritionA)
        updatePumpBUI(switchPumpNutritionB.isChecked, tvNutritionBStatus, tvNutritionB)

        connectivityManager = requireContext().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                activity?.runOnUiThread {
                    bgStatus.setBackgroundResource(R.drawable.bg_status_online)
                    tvInternet.text = "Online"
                    tvInternet.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    tvWifiTop.text = "Online"
                    tvWifiTop.setBackgroundResource(R.drawable.btn_online)
                    tvWifiTop.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    imgWifi.setImageResource(R.drawable.wifi)
                    tvSystemDesc.text = "The system runs normally"
                }
            }
            override fun onLost(network: Network) {
                activity?.runOnUiThread {
                    bgStatus.setBackgroundResource(R.drawable.bg_status_offline)
                    tvInternet.text = "Offline"
                    tvInternet.setTextColor(ContextCompat.getColor(requireContext(), R.color.brightred))
                    tvWifiTop.text = "Offline"
                    tvWifiTop.setBackgroundResource(R.drawable.btn_offline)
                    tvWifiTop.setTextColor(ContextCompat.getColor(requireContext(), R.color.brightred))
                    imgWifi.setImageResource(R.drawable.ic_wifioff)
                    tvSystemDesc.text = "No internet connection"
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)

        pumpNutritionAListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pumpOn = parsePumpValue(snapshot.value)
                isProgrammaticChange = true
                switchPumpNutritionA.isChecked = pumpOn
                isProgrammaticChange = false
                updatePumpAUI(pumpOn, tvNutritionAStatus, tvNutritionA)
                tvSensor.text = "Normal"
                tvSensor.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("relay/pompa_nutritionA").addValueEventListener(pumpNutritionAListener!!)

        switchPumpNutritionA.setOnCheckedChangeListener { _, isChecked ->
            if (!isProgrammaticChange) {
                database.child("relay/pompa_nutritionA").setValue(isChecked)
            }
        }

        pumpNutritionBListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pumpOn = parsePumpValue(snapshot.value)
                isProgrammaticChange = true
                switchPumpNutritionB.isChecked = pumpOn
                isProgrammaticChange = false
                updatePumpBUI(pumpOn, tvNutritionBStatus, tvNutritionB)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("relay/pompa_nutritionB").addValueEventListener(pumpNutritionBListener!!)

        switchPumpNutritionB.setOnCheckedChangeListener { _, isChecked ->
            if (!isProgrammaticChange) {
                database.child("relay/pompa_nutritionB").setValue(isChecked)
            }
        }

        phListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ph = snapshot.getValue(Double::class.java)?.toFloat() ?: return
                tvPH.text = String.format(Locale.US, "%.2f", ph)
                SensorNotifier.checkPH(requireContext(), ph.toDouble())
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("sensors/ph").addValueEventListener(phListener!!)

        tdsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tds = snapshot.getValue(Double::class.java)?.toFloat() ?: return
                tvTDS.text = String.format(Locale.US, "%.0f ppm", tds)
                SensorNotifier.checkTDS(requireContext(), tds.toInt())
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("sensors/tds").addValueEventListener(tdsListener!!)

        uvListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uv = snapshot.getValue(Double::class.java)?.toFloat() ?: return
                tvUV.text = String.format(Locale.US, "%.2f", uv)
                SensorNotifier.checkUV(requireContext(), uv.toDouble())
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("sensors/uv").addValueEventListener(uvListener!!)

        return view
    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                }
            }
        )
    }

    private fun parsePumpValue(raw: Any?): Boolean = when (raw) {
        is Boolean -> raw
        is String  -> raw.equals("true", ignoreCase = true) || raw.equals("ON", ignoreCase = true)
        else       -> false
    }

    private fun updatePumpAUI(isOn: Boolean, tvStatus: TextView, tvNutritionA: TextView) {
        val color = if (isOn) R.color.green else R.color.brightred
        tvStatus.text = if (isOn) "ON" else "OFF"
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), color))
        tvNutritionA.text = if (isOn) "Running" else "Stopped"
        tvNutritionA.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun updatePumpBUI(isOn: Boolean, tvStatus: TextView, tvNutritionB: TextView) {
        val color = if (isOn) R.color.green else R.color.brightred
        tvStatus.text = if (isOn) "ON" else "OFF"
        tvStatus.setTextColor(ContextCompat.getColor(requireContext(), color))
        tvNutritionB.text = if (isOn) "Running" else "Stopped"
        tvNutritionB.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pumpNutritionAListener?.let { database.child("relay/pompa_nutritionA").removeEventListener(it) }
        pumpNutritionBListener?.let { database.child("relay/pompa_nutritionB").removeEventListener(it) }
        phListener?.let  { database.child("sensors/ph").removeEventListener(it) }
        tdsListener?.let { database.child("sensors/tds").removeEventListener(it) }
        uvListener?.let  { database.child("sensors/uv").removeEventListener(it) }

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}