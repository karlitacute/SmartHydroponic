package com.example.smarthydroponic

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PumpControlActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnAuto: LinearLayout
    private lateinit var btnManual: LinearLayout
    private lateinit var radioAuto: RadioButton
    private lateinit var radioManual: RadioButton
    private lateinit var seekPump: SeekBar
    private lateinit var txtPercent: TextView

    private var isAuto = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pump_control)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById(R.id.btnBack)
        btnAuto = findViewById(R.id.btnAuto)
        btnManual = findViewById(R.id.btnManual)
        radioAuto = findViewById(R.id.radioAuto)
        radioManual = findViewById(R.id.radioManual)
        seekPump = findViewById(R.id.seekPump)
        txtPercent = findViewById(R.id.txtPercent)

        setMode(true)

        btnBack.setOnClickListener { finish() }

        btnAuto.setOnClickListener { setMode(true) }
        btnManual.setOnClickListener { setMode(false) }

        seekPump.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                txtPercent.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setMode(auto: Boolean) {
        isAuto = auto

        if (auto) {
            radioAuto.isChecked = true
            radioManual.isChecked = false

            btnAuto.setBackgroundResource(R.drawable.bg_mode_active)
            btnManual.setBackgroundResource(R.drawable.bg_mode_inactive)

            seekPump.isEnabled = false
            seekPump.alpha = 0.4f

        } else {
            radioAuto.isChecked = false
            radioManual.isChecked = true

            btnAuto.setBackgroundResource(R.drawable.bg_mode_inactive)
            btnManual.setBackgroundResource(R.drawable.bg_mode_active)

            seekPump.isEnabled = true
            seekPump.alpha = 1f
        }
    }
}