package com.example.smarthydroponic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

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

    private lateinit var tvMin: TextView
    private lateinit var tvAvg: TextView
    private lateinit var tvMax: TextView

    private lateinit var tvHistTemp: TextView
    private lateinit var tvHistTempTime: TextView
    private lateinit var tvHistPH: TextView
    private lateinit var tvHistPHTime: TextView
    private lateinit var tvHistTDS: TextView
    private lateinit var tvHistTDSTime: TextView
    private lateinit var tvHistUV: TextView
    private lateinit var tvHistUVTime: TextView
    private var selectedType = "TEMP"
    private var selectedTimeRange = "1H"

    private val dataTemp = ArrayDeque<Pair<Long, Float>>()
    private val dataPH = ArrayDeque<Pair<Long, Float>>()
    private val dataTDS = ArrayDeque<Pair<Long, Float>>()
    private val dataUV = ArrayDeque<Pair<Long, Float>>()

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 3000L

    private val dateFormat = SimpleDateFormat("d MMM yyyy HH:mm", Locale("id", "ID"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))

    private val realtimeRunnable = object : Runnable {
        override fun run() {
            addNewSensorData()
            updateChart()
            updatePumpControl()
            updateDataHistory()
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupChart()
        generateInitialData()

        selectedType = "TEMP"
        selectedTimeRange = "1H"
        updateChartTitle()
        setActiveFilter(btnTemp)
        setActiveTimeButton(btn1H, btn6H, btn12H, btn1D, btn7D)

        setupClickListeners()

        handler.post(realtimeRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(realtimeRunnable)
    }

    private fun initViews() {
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

        tvMin = findViewById(R.id.tvMin)
        tvAvg = findViewById(R.id.tvAvg)
        tvMax = findViewById(R.id.tvMax)

        tvHistTemp = findViewById(R.id.tvHistTemp)
        tvHistTempTime = findViewById(R.id.tvHistTempTime)
        tvHistPH = findViewById(R.id.tvHistPH)
        tvHistPHTime = findViewById(R.id.tvHistPHTime)
        tvHistTDS = findViewById(R.id.tvHistTDS)
        tvHistTDSTime = findViewById(R.id.tvHistTDSTime)
        tvHistUV = findViewById(R.id.tvHistUV)
        tvHistUVTime = findViewById(R.id.tvHistUVTime)
    }

    private fun setupClickListeners() {
        btnTemp.setOnClickListener {
            selectedType = "TEMP"
            updateChartTitle()
            setActiveFilter(btnTemp)
            updateChart()
            updatePumpControl()
        }
        btnPH.setOnClickListener {
            selectedType = "PH"
            updateChartTitle()
            setActiveFilter(btnPH)
            updateChart()
            updatePumpControl()
        }
        btnTDS.setOnClickListener {
            selectedType = "TDS"
            updateChartTitle()
            setActiveFilter(btnTDS)
            updateChart()
            updatePumpControl()
        }
        btnUV.setOnClickListener {
            selectedType = "UV"
            updateChartTitle()
            setActiveFilter(btnUV)
            updateChart()
            updatePumpControl()
        }

        btn1H.setOnClickListener {
            selectedTimeRange = "1H"
            setActiveTimeButton(btn1H, btn6H, btn12H, btn1D, btn7D)
            updateChart()
            updatePumpControl()
        }
        btn6H.setOnClickListener {
            selectedTimeRange = "6H"
            setActiveTimeButton(btn6H, btn1H, btn12H, btn1D, btn7D)
            updateChart()
            updatePumpControl()
        }
        btn12H.setOnClickListener {
            selectedTimeRange = "12H"
            setActiveTimeButton(btn12H, btn1H, btn6H, btn1D, btn7D)
            updateChart()
            updatePumpControl()
        }
        btn1D.setOnClickListener {
            selectedTimeRange = "1D"
            setActiveTimeButton(btn1D, btn1H, btn6H, btn12H, btn7D)
            updateChart()
            updatePumpControl()
        }
        btn7D.setOnClickListener {
            selectedTimeRange = "7D"
            setActiveTimeButton(btn7D, btn1H, btn6H, btn12H, btn1D)
            updateChart()
            updatePumpControl()
        }
    }

    private fun generateInitialData() {
        val now = System.currentTimeMillis()

        val stepMs = 10 * 60_000L
        val count = (7 * 24 * 60 / 10)

        var lastTemp = 28f
        var lastPH = 6.5f
        var lastTDS = 700f
        var lastUV = 3.2f

        for (i in count downTo 0) {
            val t = now - i * stepMs

            lastTemp = clamp(lastTemp + randomNoise(0.3f), 25f, 32f)
            lastPH = clamp(lastPH + randomNoise(0.05f), 5.5f, 7.5f)
            lastTDS = clamp(lastTDS + randomNoise(10f), 600f, 900f)
            lastUV = clamp(lastUV + randomNoise(0.2f), 1f, 5f)

            dataTemp.add(Pair(t, lastTemp))
            dataPH.add(Pair(t, lastPH))
            dataTDS.add(Pair(t, lastTDS))
            dataUV.add(Pair(t, lastUV))
        }
    }

    private fun addNewSensorData() {
        val now = System.currentTimeMillis()

        val lastTemp = dataTemp.lastOrNull()?.second ?: 28f
        val lastPH = dataPH.lastOrNull()?.second ?: 6.5f
        val lastTDS = dataTDS.lastOrNull()?.second ?: 700f
        val lastUV = dataUV.lastOrNull()?.second ?: 3.2f

        dataTemp.add(Pair(now, clamp(lastTemp + randomNoise(0.3f), 25f, 32f)))
        dataPH.add(Pair(now, clamp(lastPH + randomNoise(0.05f), 5.5f, 7.5f)))
        dataTDS.add(Pair(now, clamp(lastTDS + randomNoise(10f), 600f, 900f)))
        dataUV.add(Pair(now, clamp(lastUV + randomNoise(0.2f), 1f, 5f)))

        if (dataTemp.size > 2000) dataTemp.removeFirst()
        if (dataPH.size > 2000) dataPH.removeFirst()
        if (dataTDS.size > 2000) dataTDS.removeFirst()
        if (dataUV.size > 2000) dataUV.removeFirst()
    }
    private fun setupChart() {
        val description = Description()
        description.text = ""
        lineChart.description = description
        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled = false

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -30f
        xAxis.textSize = 9f
        lineChart.extraBottomOffset = 10f

        lineChart.axisLeft.textSize = 9f
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(false)
        lineChart.animateX(500)
    }

    private fun updateChart() {
        val rawData = getActiveData()
        val filtered = filterByTimeRange(rawData)
        if (filtered.isEmpty()) return

        val startTime = filtered.first().first

        val entries = filtered.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second)
        }

        val labelFormat = when (selectedTimeRange) {
            "7D"  -> SimpleDateFormat("d/MM", Locale("id", "ID"))
            "1D"  -> SimpleDateFormat("d/MM HH:mm", Locale("id", "ID"))
            else  -> timeFormat
        }
        val xLabels = filtered.map { labelFormat.format(Date(it.first)) }
        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val idx = value.toInt()
                return if (idx >= 0 && idx < xLabels.size) xLabels[idx] else ""
            }
        }

        val maxLabels = when (selectedTimeRange) {
            "1H"  -> 6
            "6H"  -> 6
            "12H" -> 6
            "1D"  -> 4
            "7D"  -> 7
            else  -> 6
        }
        lineChart.xAxis.setLabelCount(maxLabels, true)

        lineChart.xAxis.labelRotationAngle = when (selectedTimeRange) {
            "1D" -> -45f
            "7D" -> -30f
            else -> -30f
        }

        val color = when (selectedType) {
            "TEMP" -> Color.parseColor("#2e7d32")
            "PH"   -> Color.parseColor("#1565c0")
            "TDS"  -> Color.parseColor("#6a1b9a")
            "UV"   -> Color.parseColor("#f57f17")
            else   -> Color.GREEN
        }

        val dataSet = LineDataSet(entries, "").apply {
            this.color = color
            setCircleColor(color)
            valueTextColor = Color.TRANSPARENT
            lineWidth = 2f
            circleRadius = 3f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = color
            fillAlpha = 30
        }

        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }

    private fun updatePumpControl() {
        val rawData = getActiveData()
        val filtered = filterByTimeRange(rawData)
        if (filtered.isEmpty()) return

        val values = filtered.map { it.second }
        val minVal = values.min()
        val maxVal = values.max()
        val avgVal = values.average().toFloat()

        val unit = getUnit()

        tvMin.text = formatValue(minVal) + unit
        tvAvg.text = formatValue(avgVal) + unit
        tvMax.text = formatValue(maxVal) + unit
    }

    private fun updateDataHistory() {
        dataTemp.lastOrNull()?.let {
            tvHistTemp.text = String.format(Locale.US, "%.1f °C", it.second)
            tvHistTempTime.text = dateFormat.format(Date(it.first))
        }
        dataPH.lastOrNull()?.let {
            tvHistPH.text = String.format(Locale.US, "%.2f", it.second)
            tvHistPHTime.text = dateFormat.format(Date(it.first))
        }
        dataTDS.lastOrNull()?.let {
            tvHistTDS.text = String.format(Locale.US, "%.0f ppm", it.second)
            tvHistTDSTime.text = dateFormat.format(Date(it.first))
        }
        dataUV.lastOrNull()?.let {
            tvHistUV.text = String.format(Locale.US, "%.1f", it.second)
            tvHistUVTime.text = dateFormat.format(Date(it.first))
        }
    }

    private fun getActiveData(): ArrayDeque<Pair<Long, Float>> = when (selectedType) {
        "TEMP" -> dataTemp
        "PH"   -> dataPH
        "TDS"  -> dataTDS
        "UV"   -> dataUV
        else   -> dataTemp
    }

    private fun filterByTimeRange(data: ArrayDeque<Pair<Long, Float>>): List<Pair<Long, Float>> {
        val now = System.currentTimeMillis()
        val cutoff = when (selectedTimeRange) {
            "1H"  -> now - 1 * 3600_000L
            "6H"  -> now - 6 * 3600_000L
            "12H" -> now - 12 * 3600_000L
            "1D"  -> now - 24 * 3600_000L
            "7D"  -> now - 7 * 24 * 3600_000L
            else  -> now - 3600_000L
        }
        val filtered = data.filter { it.first >= cutoff }

        return if (filtered.size > 60) {
            val step = filtered.size / 60
            filtered.filterIndexed { index, _ -> index % step == 0 }
        } else filtered
    }

    private fun getUnit(): String = when (selectedType) {
        "TEMP" -> " °C"
        "PH"   -> ""
        "TDS"  -> " ppm"
        "UV"   -> ""
        else   -> ""
    }

    private fun formatValue(v: Float): String {
        val raw = when (selectedType) {
            "TDS" -> String.format(Locale.US, "%.0f", v)
            "PH"  -> String.format(Locale.US, "%.2f", v)
            else  -> String.format(Locale.US, "%.1f", v)
        }
        return raw
    }

    private fun randomNoise(scale: Float): Float = ((Math.random() - 0.5) * 2 * scale).toFloat()

    private fun clamp(v: Float, min: Float, max: Float): Float = v.coerceIn(min, max)

    private fun updateChartTitle() {
        tvChartTitle.text = when (selectedType) {
            "TEMP" -> "Temperature Chart (°C)"
            "PH"   -> "Water pH Chart"
            "TDS"  -> "TDS Chart (ppm)"
            "UV"   -> "UV Light Chart"
            else   -> ""
        }
    }

    private fun setActiveTimeButton(active: TextView, vararg others: TextView) {
        active.setBackgroundResource(R.drawable.bg_time_active)
        active.setTextColor(Color.WHITE)
        for (btn in others) {
            btn.setBackgroundResource(R.drawable.bg_time_inactive)
            btn.setTextColor(Color.parseColor("#333333"))
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