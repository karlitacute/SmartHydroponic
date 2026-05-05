package com.example.smarthydroponic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.Description
import android.widget.TextView
import android.widget.LinearLayout

class ChartActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart

    private lateinit var tvChartTitle: TextView

    private lateinit var btn1H: TextView
    private lateinit var btn6H: TextView
    private lateinit var btn12H: TextView
    private lateinit var btn1D: TextView
    private lateinit var btn7D: TextView

    private lateinit var btnTemp: LinearLayout
    private lateinit var btnPH: LinearLayout
    private lateinit var btnTDS: LinearLayout
    private lateinit var btnUV: LinearLayout

    private var selectedType = "TEMP"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        lineChart = findViewById(R.id.lineChart)
        tvChartTitle = findViewById(R.id.tvChartTitle)

        btn1H = findViewById(R.id.btn1H)
        btn6H = findViewById(R.id.btn6H)
        btn12H = findViewById(R.id.btn12H)
        btn1D = findViewById(R.id.btn1D)
        btn7D = findViewById(R.id.btn7D)

        btnTemp = findViewById(R.id.btnTemp)
        btnPH = findViewById(R.id.btnPH)
        btnTDS = findViewById(R.id.btnTDS)
        btnUV = findViewById(R.id.btnUV)

        setupChart()

        selectedType = "TEMP"
        updateChartTitle()
        loadDataByTime("6H")
        setActiveButton(btn6H, btn1H, btn12H, btn1D, btn7D)
        setActiveFilter(btnTemp)

        btnTemp.setOnClickListener {
            selectedType = "TEMP"
            updateChartTitle()
            loadDataByTime("6H")
            setActiveFilter(btnTemp)
        }

        btnPH.setOnClickListener {
            selectedType = "PH"
            updateChartTitle()
            loadDataByTime("6H")
            setActiveFilter(btnPH)
        }

        btnTDS.setOnClickListener {
            selectedType = "TDS"
            updateChartTitle()
            loadDataByTime("6H")
            setActiveFilter(btnTDS)
        }

        btnUV.setOnClickListener {
            selectedType = "UV"
            updateChartTitle()
            loadDataByTime("6H")
            setActiveFilter(btnUV)
        }

        btn1H.setOnClickListener {
            loadDataByTime("1H")
            setActiveButton(btn1H, btn6H, btn12H, btn1D, btn7D)
        }

        btn6H.setOnClickListener {
            loadDataByTime("6H")
            setActiveButton(btn6H, btn1H, btn12H, btn1D, btn7D)
        }

        btn12H.setOnClickListener {
            loadDataByTime("12H")
            setActiveButton(btn12H, btn1H, btn6H, btn1D, btn7D)
        }

        btn1D.setOnClickListener {
            loadDataByTime("1D")
            setActiveButton(btn1D, btn1H, btn6H, btn12H, btn7D)
        }

        btn7D.setOnClickListener {
            loadDataByTime("7D")
            setActiveButton(btn7D, btn1H, btn6H, btn12H, btn1D)
        }
    }
    private fun updateChartTitle() {
        tvChartTitle.text = when (selectedType) {
            "TEMP" -> "Temperature Chart (°C)"
            "PH" -> "Water pH Chart"
            "TDS" -> "TDS Chart (ppm)"
            "UV" -> "UV Light Chart"
            else -> ""
        }
    }
    private fun setupChart() {
        val description = Description()
        description.text = "Monitoring"
        lineChart.description = description

        lineChart.axisRight.isEnabled = false

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        lineChart.animateX(1000)
    }
    private fun loadDataByTime(type: String) {
        val entries = ArrayList<Entry>()

        val range = when (type) {
            "1H" -> 6
            "6H" -> 6
            "12H" -> 12
            "1D" -> 24
            "7D" -> 7
            else -> 6
        }

        for (i in 1..range) {
            val value = when (selectedType) {
                "TEMP" -> (25..32).random().toFloat()
                "PH" -> (5..7).random().toFloat()
                "TDS" -> (600..900).random().toFloat()
                "UV" -> (1..5).random().toFloat()
                else -> 0f
            }
            entries.add(Entry(i.toFloat(), value))
        }

        val label = when (selectedType) {
            "TEMP" -> "Temperature (°C)"
            "PH" -> "pH Air"
            "TDS" -> "TDS (ppm)"
            "UV" -> "UV Light"
            else -> ""
        }

        val dataSet = LineDataSet(entries, label)
        dataSet.color = Color.GREEN
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(Color.GREEN)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f

        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }

    private fun setActiveButton(active: TextView, vararg others: TextView) {
        active.setBackgroundColor(Color.parseColor("#4CAF50"))
        active.setTextColor(Color.WHITE)

        for (btn in others) {
            btn.setBackgroundColor(Color.TRANSPARENT)
            btn.setTextColor(Color.BLACK)
        }
    }
    private fun setActiveFilter(active: LinearLayout) {

        val all = listOf(btnTemp, btnPH, btnTDS, btnUV)

        for (btn in all) {
            btn.setBackgroundResource(R.drawable.bg_filter_inactive)

            val text = btn.getChildAt(1) as TextView
            text.setTextColor(Color.BLACK)
        }

        active.setBackgroundResource(R.drawable.bg_filter_active)

        val activeText = active.getChildAt(1) as TextView
        activeText.setTextColor(getColor(R.color.green))
    }
}