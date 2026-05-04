package com.example.smarthydroponic

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val mainView = findViewById<android.view.View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sp = getSharedPreferences("USER_DATA", MODE_PRIVATE)

        val name = sp.getString("NAME", "")
        val email = sp.getString("EMAIL", "")
        val photo = sp.getString("PHOTO", "")

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)

        tvName.text = name
        tvEmail.text = email

        if (!photo.isNullOrEmpty()) {
            Glide.with(this)
                .load(photo)
                .circleCrop()
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile)
                .into(imgProfile)
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile)
        }
    }
}