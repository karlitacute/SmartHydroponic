package com.example.smarthydroponic

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment

class SettingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)

        imgProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        val itemPump = view.findViewById<View>(R.id.itemPump)
        val itemSchedule = view.findViewById<View>(R.id.itemSchedule)
        val itemNotif = view.findViewById<View>(R.id.itemNotif)
        val itemLogout = view.findViewById<View>(R.id.itemLogout)

        itemPump.setOnClickListener {
            startActivity(Intent(requireContext(), PumpControlActivity::class.java))
        }

        itemSchedule.setOnClickListener {
            startActivity(Intent(requireContext(), PumpScheduleActivity::class.java))
        }

        itemNotif.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }

        itemLogout.setOnClickListener {
            logoutUser()
        }
    }
    private fun logoutUser() {
        Toast.makeText(requireContext(), "Logout berhasil", Toast.LENGTH_SHORT).show()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }
}