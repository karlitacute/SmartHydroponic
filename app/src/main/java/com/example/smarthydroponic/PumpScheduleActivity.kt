package com.example.smarthydroponic

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class PumpScheduleActivity : AppCompatActivity() {

    private lateinit var containerSchedule: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pump_schedule)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        containerSchedule = findViewById(R.id.containerSchedule)

        val isLoggedIn = checkLoginStatus()

        val btnAdd = findViewById<MaterialButton>(R.id.btnAdd)

        btnAdd.visibility = android.view.View.VISIBLE

        if (isLoggedIn) {
            loadSchedules()
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        btnAdd.setOnClickListener {
            showAddScheduleDialog()
        }
    }

    private fun checkLoginStatus(): Boolean {
        val prefs = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_logged_in", false)
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

        val items = listOf("Water Pump", "Nutrition Pump")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        tvStart.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                tvStart.text = String.format("%02d:%02d", hour, minute)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        tvEnd.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                tvEnd.text = String.format("%02d:%02d", hour, minute)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = spinner.selectedItem.toString()
            val start = tvStart.text.toString()
            val end = tvEnd.text.toString()

            addScheduleCard(name, start, end)
            saveSchedule(name, start, end)

            Toast.makeText(this, "$name : $start - $end saved!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addScheduleCard(name: String, start: String, end: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_add, containerSchedule, false)

        view.findViewById<TextView>(R.id.txtTitle).text = name
        view.findViewById<TextView>(R.id.txtTime).text = "$start - $end"
        view.findViewById<Switch>(R.id.switchItem).isChecked = true

        containerSchedule.addView(view)
    }

    private fun saveSchedule(name: String, start: String, end: String) {
        val prefs = getSharedPreferences("schedule_data", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("schedules", "[]"))

        val obj = JSONObject()
        obj.put("name", name)
        obj.put("start", start)
        obj.put("end", end)
        jsonArray.put(obj)

        prefs.edit().putString("schedules", jsonArray.toString()).apply()
    }

    private fun loadSchedules() {
        val prefs = getSharedPreferences("schedule_data", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("schedules", "[]"))

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            addScheduleCard(obj.getString("name"), obj.getString("start"), obj.getString("end"))
        }
    }
}