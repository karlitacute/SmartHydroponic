package com.example.smarthydroponic.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.smarthydroponic.auth.LoginActivity
import com.example.smarthydroponic.home.NotificationActivity
import com.example.smarthydroponic.profile.ProfileActivity
import com.example.smarthydroponic.pump.PumpScheduleActivity
import com.example.smarthydroponic.R

class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_setting,
            container,
            false
        )

        ViewCompat.setOnApplyWindowInsetsListener(
            view.findViewById(R.id.main)
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

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgProfile =
            view.findViewById<ImageView>(R.id.imgProfile)

        imgProfile.setOnClickListener {
            val intent =
                Intent(requireContext(), ProfileActivity::class.java)

            startActivity(intent)
        }

        val itemSchedule =
            view.findViewById<View>(R.id.itemSchedule)

        val itemNotif =
            view.findViewById<View>(R.id.itemNotif)

        val itemLogout =
            view.findViewById<View>(R.id.itemLogout)

        itemSchedule.setOnClickListener {
            startActivity(
                Intent(requireContext(), PumpScheduleActivity::class.java)
            )
        }

        itemNotif.setOnClickListener {
            startActivity(
                Intent(requireContext(), NotificationActivity::class.java)
            )
        }

        itemLogout.setOnClickListener {
            logoutUser()
        }
    }
    private fun logoutUser() {

        Toast.makeText(
            requireContext(),
            "Logout berhasil",
            Toast.LENGTH_SHORT
        ).show()

        startActivity(
            Intent(requireContext(), LoginActivity::class.java)
        )

        requireActivity().finish()
    }
}