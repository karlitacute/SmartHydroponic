package com.example.smarthydroponic.profile

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.smarthydroponic.R

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val mainView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        val sp = getSharedPreferences("USER_DATA", MODE_PRIVATE)

        val name = sp.getString("NAME", "")
        val email = sp.getString("EMAIL", "")
        val photo = sp.getString("PHOTO", "")

        val profileCard = findViewById<LinearLayout>(R.id.profileCard)
        val nameCard = findViewById<LinearLayout>(R.id.nameCard)
        val emailCard = findViewById<LinearLayout>(R.id.emailCard)

        val imgProfile = profileCard.getChildAt(0) as ImageView

        val tvNameProfile = (profileCard.getChildAt(1) as LinearLayout)
            .getChildAt(0) as TextView

        val tvEmailProfile = (profileCard.getChildAt(1) as LinearLayout)
            .getChildAt(1) as TextView

        val tvName = (nameCard.getChildAt(1) as LinearLayout)
            .getChildAt(1) as TextView

        val tvEmail = (emailCard.getChildAt(1) as LinearLayout)
            .getChildAt(1) as TextView

        tvNameProfile.text = name
        tvEmailProfile.text = email

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