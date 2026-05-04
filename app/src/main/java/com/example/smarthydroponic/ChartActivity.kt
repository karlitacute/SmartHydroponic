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
class ChartActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var btn1H: TextView
    private lateinit var btn6H: TextView
    private lateinit var btn12H: TextView
    private lateinit var btn1D: TextView
    private lateinit var btn7D: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        val imgProfile = findViewById<ImageView>(R.id.imgProfile)

        imgProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        lineChart = findViewById(R.id.lineChart)

        btn1H = findViewById(R.id.btn1H)
        btn6H = findViewById(R.id.btn6H)
        btn12H = findViewById(R.id.btn12H)
        btn1D = findViewById(R.id.btn1D)
        btn7D = findViewById(R.id.btn7D)

        setupChart()

        loadDataByTime("6H")
        setActiveButton(btn6H, btn1H, btn12H, btn1D, btn7D)

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

    private fun loadDataByTime(type: String) {
        val entries = ArrayList<Entry>()

        when (type) {
            "1H" -> {
                for (i in 1..6) {
                    entries.add(Entry(i.toFloat(), (5..6).random().toFloat()))
                }
            }
            "6H" -> {
                for (i in 1..6) {
                    entries.add(Entry(i.toFloat(), (5..7).random().toFloat()))
                }
            }
            "12H" -> {
                for (i in 1..12) {
                    entries.add(Entry(i.toFloat(), (5..7).random().toFloat()))
                }
            }
            "1D" -> {
                for (i in 1..24) {
                    entries.add(Entry(i.toFloat(), (5..7).random().toFloat()))
                }
            }
            "7D" -> {
                for (i in 1..7) {
                    entries.add(Entry(i.toFloat(), (5..7).random().toFloat()))
                }
            }
        }

        val dataSet = LineDataSet(entries, "pH Air")
        dataSet.color = Color.GREEN
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(Color.GREEN)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f

        val lineData = LineData(dataSet)
        lineChart.data = lineData
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
}