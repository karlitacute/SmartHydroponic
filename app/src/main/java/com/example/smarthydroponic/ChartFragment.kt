package com.example.smarthydroponic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class ChartFragment : Fragment() {

    private lateinit var rootView: View

    private lateinit var lineChart: LineChart
    private lateinit var tvChartTitle: TextView

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

    private val dataTemp = ArrayDeque<Pair<Long, Float>>()
    private val dataPH = ArrayDeque<Pair<Long, Float>>()
    private val dataTDS = ArrayDeque<Pair<Long, Float>>()
    private val dataUV = ArrayDeque<Pair<Long, Float>>()

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L

    private val dateFormat =
        SimpleDateFormat("d MMM yyyy HH:mm", Locale("id", "ID"))

    private val timeFormat =
        SimpleDateFormat("HH:mm", Locale("id", "ID"))

    private val realtimeRunnable = object : Runnable {
        override fun run() {
            addNewSensorData()
            updateChart()
            updatePumpControl()
            updateDataHistory()
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        rootView = inflater.inflate(
            R.layout.fragment_chart,
            container,
            false
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView.findViewById(R.id.main)
        ) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        initViews()
        setupChart()
        generateInitialData()

        selectedType = "TEMP"

        updateChartTitle()
        setActiveFilter(btnTemp)
        updateChart()
        updatePumpControl()

        setupClickListeners()

        handler.post(realtimeRunnable)

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(realtimeRunnable)
    }

    private fun initViews() {

        val imgProfile =
            rootView.findViewById<ImageView>(R.id.imgProfile)

        imgProfile.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    ProfileActivity::class.java
                )
            )
        }

        lineChart = rootView.findViewById(R.id.lineChart)
        tvChartTitle = rootView.findViewById(R.id.tvChartTitle)

        btnTemp = rootView.findViewById(R.id.btnTemp)
        btnPH = rootView.findViewById(R.id.btnPH)
        btnTDS = rootView.findViewById(R.id.btnTDS)
        btnUV = rootView.findViewById(R.id.btnUV)

        tvMin = rootView.findViewById(R.id.tvMin)
        tvAvg = rootView.findViewById(R.id.tvAvg)
        tvMax = rootView.findViewById(R.id.tvMax)

        tvHistTemp = rootView.findViewById(R.id.tvHistTemp)
        tvHistTempTime = rootView.findViewById(R.id.tvHistTempTime)

        tvHistPH = rootView.findViewById(R.id.tvHistPH)
        tvHistPHTime = rootView.findViewById(R.id.tvHistPHTime)

        tvHistTDS = rootView.findViewById(R.id.tvHistTDS)
        tvHistTDSTime = rootView.findViewById(R.id.tvHistTDSTime)

        tvHistUV = rootView.findViewById(R.id.tvHistUV)
        tvHistUVTime = rootView.findViewById(R.id.tvHistUVTime)
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
    }

    private fun generateInitialData() {

        val now = System.currentTimeMillis()

        val stepMs = 60 * 60_000L
        val count = (7 * 24)

        var lastTemp = 28f
        var lastPH = 6.5f
        var lastTDS = 700f
        var lastUV = 3.2f

        for (i in count downTo 0) {

            val t = now - i * stepMs

            lastTemp = clamp(
                lastTemp + randomNoise(0.3f),
                25f,
                32f
            )

            lastPH = clamp(
                lastPH + randomNoise(0.05f),
                5.5f,
                7.5f
            )

            lastTDS = clamp(
                lastTDS + randomNoise(10f),
                600f,
                900f
            )

            lastUV = clamp(
                lastUV + randomNoise(0.2f),
                1f,
                5f
            )

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

        dataTemp.add(
            Pair(
                now,
                clamp(lastTemp + randomNoise(0.3f), 25f, 32f)
            )
        )

        dataPH.add(
            Pair(
                now,
                clamp(lastPH + randomNoise(0.05f), 5.5f, 7.5f)
            )
        )

        dataTDS.add(
            Pair(
                now,
                clamp(lastTDS + randomNoise(10f), 600f, 900f)
            )
        )

        dataUV.add(
            Pair(
                now,
                clamp(lastUV + randomNoise(0.2f), 1f, 5f)
            )
        )

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

        if (rawData.isEmpty()) return

        val displayData = rawData.toList().takeLast(24)

        val entries = displayData.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second)
        }

        val xLabels = displayData.map {
            timeFormat.format(Date(it.first))
        }

        lineChart.xAxis.valueFormatter =
            object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {

                    val idx = value.toInt()

                    return if (idx >= 0 && idx < xLabels.size) {
                        xLabels[idx]
                    } else {
                        ""
                    }
                }
            }

        lineChart.xAxis.setLabelCount(6, true)
        lineChart.xAxis.labelRotationAngle = -30f

        val color = when (selectedType) {
            "TEMP" -> Color.parseColor("#2e7d32")
            "PH" -> Color.parseColor("#1565c0")
            "TDS" -> Color.parseColor("#6a1b9a")
            "UV" -> Color.parseColor("#f57f17")
            else -> Color.GREEN
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

        if (rawData.isEmpty()) return

        val values = rawData.map { it.second }

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

            tvHistTemp.text =
                String.format(Locale.US, "%.1f °C", it.second)

            tvHistTempTime.text =
                dateFormat.format(Date(it.first))
        }

        dataPH.lastOrNull()?.let {

            tvHistPH.text =
                String.format(Locale.US, "%.2f", it.second)

            tvHistPHTime.text =
                dateFormat.format(Date(it.first))
        }

        dataTDS.lastOrNull()?.let {

            tvHistTDS.text =
                String.format(Locale.US, "%.0f ppm", it.second)

            tvHistTDSTime.text =
                dateFormat.format(Date(it.first))
        }

        dataUV.lastOrNull()?.let {

            tvHistUV.text =
                String.format(Locale.US, "%.1f", it.second)

            tvHistUVTime.text =
                dateFormat.format(Date(it.first))
        }
    }

    private fun getActiveData(): ArrayDeque<Pair<Long, Float>> {

        return when (selectedType) {
            "TEMP" -> dataTemp
            "PH" -> dataPH
            "TDS" -> dataTDS
            "UV" -> dataUV
            else -> dataTemp
        }
    }

    private fun getUnit(): String {

        return when (selectedType) {
            "TEMP" -> " °C"
            "PH" -> ""
            "TDS" -> " ppm"
            "UV" -> ""
            else -> ""
        }
    }

    private fun formatValue(v: Float): String {

        return when (selectedType) {

            "TDS" -> String.format(Locale.US, "%.0f", v)

            "PH" -> String.format(Locale.US, "%.2f", v)

            else -> String.format(Locale.US, "%.1f", v)
        }
    }

    private fun randomNoise(scale: Float): Float {
        return ((Math.random() - 0.5) * 2 * scale).toFloat()
    }

    private fun clamp(
        v: Float,
        min: Float,
        max: Float
    ): Float {
        return v.coerceIn(min, max)
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

    private fun setActiveFilter(active: LinearLayout) {

        val all = listOf(
            btnTemp,
            btnPH,
            btnTDS,
            btnUV
        )

        for (btn in all) {

            btn.setBackgroundResource(
                R.drawable.bg_filter_inactive
            )

            val text = btn.getChildAt(1) as TextView
            text.setTextColor(Color.BLACK)
        }

        active.setBackgroundResource(
            R.drawable.bg_filter_active
        )

        val activeText =
            active.getChildAt(1) as TextView

        activeText.setTextColor(
            requireContext().getColor(R.color.green)
        )
    }
}