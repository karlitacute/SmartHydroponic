package com.example.smarthydroponic

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Switch

class PumpScheduleActivity : AppCompatActivity() {

    private lateinit var switchWater: Switch
    private lateinit var switchNutrition: Switch
    private lateinit var btnAdd: Button
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pump_schedule)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnBack = findViewById(R.id.btnBack)
        switchWater = findViewById(R.id.switchWater)
        switchNutrition = findViewById(R.id.switchNutrition)
        btnAdd = findViewById(R.id.btnAdd)

        btnBack.setOnClickListener {
            finish()
        }

        switchWater.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "ON" else "OFF"
            Toast.makeText(this, "Water Pump $status", Toast.LENGTH_SHORT).show()
        }

        switchNutrition.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "ON" else "OFF"
            Toast.makeText(this, "Nutrition Pump $status", Toast.LENGTH_SHORT).show()
        }

        btnAdd.setOnClickListener {
            Toast.makeText(this, "Tambah Jadwal", Toast.LENGTH_SHORT).show()
        }
    }
}