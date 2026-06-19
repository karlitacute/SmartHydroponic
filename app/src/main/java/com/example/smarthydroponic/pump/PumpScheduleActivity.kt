package com.example.smarthydroponic.pump

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smarthydroponic.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PumpScheduleActivity : AppCompatActivity() {

    private lateinit var containerSchedule: LinearLayout
    private var currentUserId: String = "default"

    private val dbRef = FirebaseDatabase.getInstance(
        "https://smarthydroponic-303e9-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference.child("schedule")

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

        val userPrefs = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val email = userPrefs.getString("EMAIL", "") ?: ""
        currentUserId = if (email.isNotEmpty()) email else "default"

        Log.d("FB_SCHEDULE", "User: $currentUserId")

        loadSchedules()
        ScheduleWorkManager.rescheduleAll(this, currentUserId)
        requestIgnoreBatteryOptimization()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnAdd).setOnClickListener { showAddScheduleDialog() }
    }

    override fun onResume() {
        super.onResume()
        containerSchedule.removeAllViews()
        loadSchedules()
    }
    private fun requestIgnoreBatteryOptimization() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val pm = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                ).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun pickTime(tv: TextView) {
        val existing = tv.text.toString()
        val parts    = existing.split(":").map { it.toIntOrNull() ?: 0 }
        val initH    = if (parts.size > 0) parts[0] else Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val initM    = if (parts.size > 1) parts[1] else Calendar.getInstance().get(Calendar.MINUTE)
        val initS    = if (parts.size > 2) parts[2] else 0

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null)

        val pickerHour   = dialogView.findViewById<NumberPicker>(R.id.pickerHour)
        val pickerMinute = dialogView.findViewById<NumberPicker>(R.id.pickerMinute)
        val pickerSecond = dialogView.findViewById<NumberPicker>(R.id.pickerSecond)

        pickerHour.minValue   = 0; pickerHour.maxValue   = 23
        pickerMinute.minValue = 0; pickerMinute.maxValue = 59
        pickerSecond.minValue = 0; pickerSecond.maxValue = 59

        pickerHour.displayedValues   = (0..23).map { String.format("%02d", it) }.toTypedArray()
        pickerMinute.displayedValues = (0..59).map { String.format("%02d", it) }.toTypedArray()
        pickerSecond.displayedValues = (0..59).map { String.format("%02d", it) }.toTypedArray()

        pickerHour.value   = initH
        pickerMinute.value = initM
        pickerSecond.value = initS

        AlertDialog.Builder(this)
            .setTitle("Pilih Waktu")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                tv.text = String.format("%02d:%02d:%02d",
                    pickerHour.value, pickerMinute.value, pickerSecond.value)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun nowIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun pushToFirebase(name: String, start: String, end: String, firebaseKey: String, isActive: Boolean) {
        val data = mapOf(
            "name"       to name,
            "start_time" to start,
            "end_time"   to end,
            "is_active"  to isActive,
            "created_at" to nowIso()
        )
        dbRef.child(firebaseKey).setValue(data)
            .addOnSuccessListener { Log.d("FB_SCHEDULE", "Saved: $firebaseKey") }
            .addOnFailureListener { e -> Log.e("FB_SCHEDULE", "Gagal: ${e.message}") }
    }

    private fun updateActiveFirebase(firebaseKey: String, isActive: Boolean) {
        if (firebaseKey.isEmpty()) return
        dbRef.child(firebaseKey).child("is_active").setValue(isActive)
    }

    private fun deleteFromFirebase(firebaseKey: String) {
        if (firebaseKey.isEmpty()) return
        dbRef.child(firebaseKey).removeValue()
    }

    private fun showAddScheduleDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_schedule)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val spinner   = dialog.findViewById<Spinner>(R.id.spinnerName)
        val tvStart   = dialog.findViewById<TextView>(R.id.tvStartTime)
        val tvEnd     = dialog.findViewById<TextView>(R.id.tvEndTime)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave   = dialog.findViewById<MaterialButton>(R.id.btnSave)

        spinner.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Nutrition Pump A", "Nutrition Pump B"))

        tvStart.setOnClickListener { pickTime(tvStart) }
        tvEnd.setOnClickListener   { pickTime(tvEnd)   }
        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name  = spinner.selectedItem.toString()
            val start = tvStart.text.toString()
            val end   = tvEnd.text.toString()

            if (start.length < 5 || end.length < 5) {
                Toast.makeText(this, "Pilih waktu terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val firebaseKey = dbRef.push().key ?: run {
                Toast.makeText(this, "Gagal: tidak ada koneksi Firebase", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val index = getScheduleCount()
            addScheduleCard(index, name, start, end, firebaseKey, isActive = true)
            saveSchedule(index, name, start, end, firebaseKey, isActive = true)
            pushToFirebase(name, start, end, firebaseKey, isActive = true)
            ScheduleWorkManager.rescheduleAll(this, currentUserId)

            Toast.makeText(this, "$name : $start - $end disimpan!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditScheduleDialog(
        index: Int, currentName: String,
        currentStart: String, currentEnd: String, firebaseKey: String
    ) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_schedule)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val items     = listOf("Nutrition Pump A", "Nutrition Pump B")
        val spinner   = dialog.findViewById<Spinner>(R.id.spinnerName)
        val tvStart   = dialog.findViewById<TextView>(R.id.tvStartTime)
        val tvEnd     = dialog.findViewById<TextView>(R.id.tvEndTime)
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave   = dialog.findViewById<MaterialButton>(R.id.btnSave)

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        spinner.setSelection(items.indexOf(currentName).takeIf { it >= 0 } ?: 0)
        tvStart.text = currentStart
        tvEnd.text   = currentEnd
        btnSave.text   = "Update"
        btnCancel.text = "Delete"

        tvStart.setOnClickListener { pickTime(tvStart) }
        tvEnd.setOnClickListener   { pickTime(tvEnd)   }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            showDeleteConfirmation(index, firebaseKey)
        }

        btnSave.setOnClickListener {
            val name  = spinner.selectedItem.toString()
            val start = tvStart.text.toString()
            val end   = tvEnd.text.toString()

            if (start.length < 5 || end.length < 5) {
                Toast.makeText(this, "Pilih waktu terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentActive = getIsActive(index)
            updateSchedule(index, name, start, end, firebaseKey, currentActive)
            pushToFirebase(name, start, end, firebaseKey, currentActive)
            ScheduleWorkManager.rescheduleAll(this, currentUserId)

            Toast.makeText(this, "Jadwal diperbarui!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            containerSchedule.removeAllViews()
            loadSchedules()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(index: Int, firebaseKey: String) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Jadwal")
            .setMessage("Yakin ingin menghapus jadwal ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteSchedule(index)
                deleteFromFirebase(firebaseKey)
                containerSchedule.removeAllViews()
                loadSchedules()
                ScheduleWorkManager.rescheduleAll(this, currentUserId)
                Toast.makeText(this, "Jadwal dihapus.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun addScheduleCard(
        index: Int, name: String, start: String,
        end: String, firebaseKey: String, isActive: Boolean
    ) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_add, containerSchedule, false)

        view.findViewById<TextView>(R.id.txtTitle).text = name
        view.findViewById<TextView>(R.id.txtTime).text  = "$start - $end"

        val switchItem = view.findViewById<Switch>(R.id.switchItem)
        switchItem.isChecked   = isActive
        switchItem.isClickable = true
        switchItem.isFocusable = true

        switchItem.setOnCheckedChangeListener { _, checked ->
            updateIsActive(index, checked)
            updateActiveFirebase(firebaseKey, checked)
            ScheduleWorkManager.rescheduleAll(this, currentUserId)
            Toast.makeText(this, "$name ${if (checked) "aktif" else "nonaktif"}", Toast.LENGTH_SHORT).show()
        }

        view.setOnClickListener {
            showEditScheduleDialog(index, name, start, end, firebaseKey)
        }

        containerSchedule.addView(view)
    }

    private fun scheduleKey() = "schedules_$currentUserId"

    private fun getJsonArray(): JSONArray {
        val raw = getSharedPreferences("schedule_data", MODE_PRIVATE)
            .getString(scheduleKey(), "[]") ?: "[]"
        return try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
    }

    private fun saveJsonArray(array: JSONArray) {
        getSharedPreferences("schedule_data", MODE_PRIVATE)
            .edit().putString(scheduleKey(), array.toString()).apply()
    }

    private fun getScheduleCount() = getJsonArray().length()

    private fun getIsActive(index: Int): Boolean {
        val array = getJsonArray()
        if (index >= array.length()) return true
        return array.getJSONObject(index).optBoolean("is_active", true)
    }

    private fun saveSchedule(index: Int, name: String, start: String, end: String, firebaseKey: String, isActive: Boolean) {
        val array = getJsonArray()
        val obj = JSONObject().apply {
            put("name", name); put("start", start); put("end", end)
            put("firebase_key", firebaseKey); put("is_active", isActive)
        }
        if (index >= array.length()) array.put(obj) else array.put(index, obj)
        saveJsonArray(array)
    }

    private fun updateSchedule(index: Int, name: String, start: String, end: String, firebaseKey: String, isActive: Boolean) {
        val array = getJsonArray()
        if (index < array.length()) {
            array.put(index, JSONObject().apply {
                put("name", name); put("start", start); put("end", end)
                put("firebase_key", firebaseKey); put("is_active", isActive)
            })
            saveJsonArray(array)
        }
    }

    private fun updateIsActive(index: Int, isActive: Boolean) {
        val array = getJsonArray()
        if (index < array.length()) {
            array.getJSONObject(index).put("is_active", isActive)
            array.put(index, array.getJSONObject(index))
            saveJsonArray(array)
        }
    }

    private fun deleteSchedule(index: Int) {
        val array  = getJsonArray()
        val newArr = JSONArray()
        for (i in 0 until array.length()) if (i != index) newArr.put(array.getJSONObject(i))
        saveJsonArray(newArr)
    }

    private fun loadSchedules() {
        val array = getJsonArray()
        Log.d("FB_SCHEDULE", "Load: ${array.length()} jadwal")
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            addScheduleCard(i,
                obj.getString("name"), obj.getString("start"), obj.getString("end"),
                obj.optString("firebase_key", ""), obj.optBoolean("is_active", true))
        }
    }
}