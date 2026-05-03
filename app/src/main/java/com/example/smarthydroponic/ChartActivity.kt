package com.example.smarthydroponic

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.Description

class ChartActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        lineChart = findViewById(R.id.lineChart)

        setupChart()
        loadDummyData()
    }

    private fun setupChart() {
        val description = Description()
        description.text = "Monitoring Nutrisi"
        lineChart.description = description

        lineChart.axisRight.isEnabled = false

        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        lineChart.animateX(1000)
    }

    private fun loadDummyData() {
        val entries = ArrayList<Entry>()

        entries.add(Entry(1f, 5f))
        entries.add(Entry(2f, 6f))
        entries.add(Entry(3f, 5.5f))
        entries.add(Entry(4f, 7f))
        entries.add(Entry(5f, 6.5f))

        val dataSet = LineDataSet(entries, "pH Air")
        dataSet.color = Color.GREEN
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(Color.GREEN)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.invalidate() // refresh chart
    }
}