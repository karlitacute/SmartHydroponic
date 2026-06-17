package com.example.smarthydroponic.chart

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.smarthydroponic.R
import com.example.smarthydroponic.profile.ProfileActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

class ChartFragment : Fragment() {

    private lateinit var rootView: View

    private lateinit var lineChart: LineChart
    private lateinit var tvChartTitle: TextView

    private lateinit var btnPH: LinearLayout
    private lateinit var btnTDS: LinearLayout
    private lateinit var btnUV: LinearLayout

    private lateinit var tvMin: TextView
    private lateinit var tvAvg: TextView
    private lateinit var tvMax: TextView

    private lateinit var tvHistPH: TextView
    private lateinit var tvHistPHTime: TextView
    private lateinit var tvHistTDS: TextView
    private lateinit var tvHistTDSTime: TextView
    private lateinit var tvHistUV: TextView
    private lateinit var tvHistUVTime: TextView

    private var selectedType = "PH"

    private val dataPH  = ArrayDeque<Pair<Long, Float>>()
    private val dataTDS = ArrayDeque<Pair<Long, Float>>()
    private val dataUV  = ArrayDeque<Pair<Long, Float>>()

    private lateinit var database: DatabaseReference
    private var listenerPH:  ValueEventListener? = null
    private var listenerTDS: ValueEventListener? = null
    private var listenerUV:  ValueEventListener? = null

    private val dateFormat = SimpleDateFormat("d MMM yyyy HH:mm", Locale("id", "ID"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))

    private val phMin  = 6.0f;  private val phMax  = 7.0f
    private val tdsMin = 560f;  private val tdsMax = 840f
    private val uvMin  = 2.0f;  private val uvMax  = 4.0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_chart, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView.findViewById(R.id.main)
        ) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupChart()
        setupFirebaseListeners()

        selectedType = "PH"
        updateChartTitle()
        setActiveFilter(btnPH)
        setupClickListeners()

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerPH?.let  { database.child("sensors/ph").removeEventListener(it) }
        listenerTDS?.let { database.child("sensors/tds").removeEventListener(it) }
        listenerUV?.let  { database.child("sensors/uv").removeEventListener(it) }
    }

    private fun initViews() {
        rootView.findViewById<ImageView>(R.id.imgProfile).setOnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        lineChart    = rootView.findViewById(R.id.lineChart)
        tvChartTitle = rootView.findViewById(R.id.tvChartTitle)

        btnPH  = rootView.findViewById(R.id.btnPH)
        btnTDS = rootView.findViewById(R.id.btnTDS)
        btnUV  = rootView.findViewById(R.id.btnUV)

        tvMin = rootView.findViewById(R.id.tvMin)
        tvAvg = rootView.findViewById(R.id.tvAvg)
        tvMax = rootView.findViewById(R.id.tvMax)

        tvHistPH     = rootView.findViewById(R.id.tvHistPH)
        tvHistPHTime = rootView.findViewById(R.id.tvHistPHTime)
        tvHistTDS     = rootView.findViewById(R.id.tvHistTDS)
        tvHistTDSTime = rootView.findViewById(R.id.tvHistTDSTime)
        tvHistUV     = rootView.findViewById(R.id.tvHistUV)
        tvHistUVTime = rootView.findViewById(R.id.tvHistUVTime)
    }

    private fun setupFirebaseListeners() {
        database = FirebaseDatabase.getInstance(
            "https://smarthydroponic-303e9-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).reference

        listenerPH = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Double::class.java)?.toFloat() ?: return
                val now   = System.currentTimeMillis()

                dataPH.add(Pair(now, value))
                if (dataPH.size > 2000) dataPH.removeFirst()

                tvHistPH.text     = String.Companion.format(Locale.US, "%.2f", value)
                tvHistPHTime.text = dateFormat.format(Date(now))

                if (selectedType == "PH") {
                    updateChart()
                    updateSummary()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("sensors/ph").addValueEventListener(listenerPH!!)

        listenerTDS = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Double::class.java)?.toFloat() ?: return
                val now   = System.currentTimeMillis()

                dataTDS.add(Pair(now, value))
                if (dataTDS.size > 2000) dataTDS.removeFirst()

                tvHistTDS.text     = String.Companion.format(Locale.US, "%.0f ppm", value)
                tvHistTDSTime.text = dateFormat.format(Date(now))

                if (selectedType == "TDS") {
                    updateChart()
                    updateSummary()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("sensors/tds").addValueEventListener(listenerTDS!!)

        listenerUV = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Double::class.java)?.toFloat() ?: return
                val now   = System.currentTimeMillis()

                dataUV.add(Pair(now, value))
                if (dataUV.size > 2000) dataUV.removeFirst()

                tvHistUV.text     = String.Companion.format(Locale.US, "%.2f", value)
                tvHistUVTime.text = dateFormat.format(Date(now))

                if (selectedType == "UV") {
                    updateChart()
                    updateSummary()
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        database.child("sensors/uv").addValueEventListener(listenerUV!!)
    }

    private fun setupClickListeners() {
        btnPH.setOnClickListener {
            selectedType = "PH"
            updateChartTitle()
            setActiveFilter(btnPH)
            updateChart()
            updateSummary()
        }
        btnTDS.setOnClickListener {
            selectedType = "TDS"
            updateChartTitle()
            setActiveFilter(btnTDS)
            updateChart()
            updateSummary()
        }
        btnUV.setOnClickListener {
            selectedType = "UV"
            updateChartTitle()
            setActiveFilter(btnUV)
            updateChart()
            updateSummary()
        }
    }

    private fun setupChart() {
        lineChart.description = Description().apply { text = "" }
        lineChart.axisRight.isEnabled = false
        lineChart.legend.isEnabled    = false

        lineChart.xAxis.apply {
            position           = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            labelRotationAngle = -30f
            textSize           = 9f
        }

        lineChart.extraBottomOffset = 10f
        lineChart.axisLeft.textSize = 9f
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled     = true
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

        val xLabels = displayData.map { timeFormat.format(Date(it.first)) }

        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val idx = value.toInt()
                return if (idx in xLabels.indices) xLabels[idx] else ""
            }
        }
        lineChart.xAxis.setLabelCount(6, true)
        lineChart.xAxis.labelRotationAngle = -30f

        val color = when (selectedType) {
            "PH"  -> Color.parseColor("#1565c0")
            "TDS" -> Color.parseColor("#6a1b9a")
            "UV"  -> Color.parseColor("#f57f17")
            else  -> Color.GREEN
        }

        val dataSet = LineDataSet(entries, "").apply {
            this.color     = color
            setCircleColor(color)
            valueTextColor = Color.TRANSPARENT
            lineWidth      = 2f
            circleRadius   = 3f
            setDrawValues(false)
            mode           = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor      = color
            fillAlpha      = 30
        }

        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }

    private fun updateSummary() {
        val rawData = getActiveData()
        if (rawData.isEmpty()) return

        val values = rawData.map { it.second }
        val unit   = getUnit()

        val minVal = values.min()
        val avgVal = values.average().toFloat()
        val maxVal = values.max()

        tvMin.text = formatValue(minVal) + unit
        tvAvg.text = formatValue(avgVal) + unit
        tvMax.text = formatValue(maxVal) + unit

        tvMin.setTextColor(getStatusColor(minVal))
        tvAvg.setTextColor(getStatusColor(avgVal))
        tvMax.setTextColor(getStatusColor(maxVal))
    }

    // Hijau = ideal, Merah = di luar rentang ideal pakcoy
    private fun getStatusColor(value: Float): Int {
        val isIdeal = when (selectedType) {
            "PH"  -> value in phMin..phMax
            "TDS" -> value in tdsMin..tdsMax
            "UV"  -> value in uvMin..uvMax
            else  -> true
        }
        return if (isIdeal) Color.parseColor("#16A34A") else Color.parseColor("#EF4444")
    }

    private fun getActiveData() = when (selectedType) {
        "PH"  -> dataPH
        "TDS" -> dataTDS
        "UV"  -> dataUV
        else  -> dataPH
    }

    private fun getUnit() = when (selectedType) {
        "TDS" -> " ppm"
        else  -> ""
    }

    private fun formatValue(v: Float) = when (selectedType) {
        "TDS" -> String.Companion.format(Locale.US, "%.0f", v)
        "PH"  -> String.Companion.format(Locale.US, "%.2f", v)
        else  -> String.Companion.format(Locale.US, "%.2f", v)
    }

    private fun updateChartTitle() {
        tvChartTitle.text = when (selectedType) {
            "PH"  -> "Water pH Chart"
            "TDS" -> "TDS Chart (ppm)"
            "UV"  -> "UV Light Chart"
            else  -> ""
        }
    }

    private fun setActiveFilter(active: LinearLayout) {
        listOf(btnPH, btnTDS, btnUV).forEach { btn ->
            btn.setBackgroundResource(R.drawable.bg_filter_inactive)
            (btn.getChildAt(1) as TextView).setTextColor(Color.BLACK)
        }
        active.setBackgroundResource(R.drawable.bg_filter_active)
        (active.getChildAt(1) as TextView).setTextColor(
            requireContext().getColor(R.color.green)
        )
    }
}