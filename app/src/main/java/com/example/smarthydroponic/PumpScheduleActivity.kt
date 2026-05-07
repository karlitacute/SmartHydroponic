package com.example.smarthydroponic

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import java.util.Calendar

class PumpScheduleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pump_schedule)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnAdd).setOnClickListener {
            showAddScheduleDialog()
        }
    }

    private fun showAddScheduleDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_schedule)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val spinner = dialog.findViewById<Spinner>(R.id.spinnerName)
        val tvStart = dialog.findViewById<TextView>(R.id.tvStartTime)
        val tvEnd = dialog.findViewById<TextView>(R.id.tvEndTime)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btnSave)

        val items = listOf("Water Pump", "Nutrition Pump", "Custom")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        tvStart.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                tvStart.text = String.format("%02d:%02d", h, m)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        tvEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, h, m ->
                tvEnd.text = String.format("%02d:%02d", h, m)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = spinner.selectedItem.toString()
            val start = tvStart.text.toString()
            val end = tvEnd.text.toString()
            Toast.makeText(this, "$name: $start - $end saved!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}