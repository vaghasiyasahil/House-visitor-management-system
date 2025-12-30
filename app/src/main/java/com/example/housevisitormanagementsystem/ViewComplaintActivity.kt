package com.example.housevisitormanagementsystem

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import adapter.ComplaintAdapter
import com.example.housevisitormanagementsystem.databinding.ActivityViewComplaintBinding
import model.Complaint

class ViewComplaintActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewComplaintBinding
    private lateinit var adapter: ComplaintAdapter
    private val complaintList = mutableListOf<Complaint>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewComplaintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupRecyclerView()
        setupDummyData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = ComplaintAdapter(this, complaintList)
        binding.rvComplaints.layoutManager = LinearLayoutManager(this)
        binding.rvComplaints.adapter = adapter
    }

    private fun setupDummyData() {
        complaintList.clear()
        complaintList.add(
            Complaint(
                "1", "Water Leakage in Kitchen", "A-104", "Oct 24, 10:30 AM",
                "The sink pipe is leaking heavily and causing water to pool on the floor near the cabinets.",
                "In Progress", R.drawable.ic_water_drop, R.color.deep_ocean_blue, R.color.bright_azure_blue
            )
        )
        complaintList.add(
            Complaint(
                "2", "Noise Complaint", "B-202", "Oct 23, 11:15 PM",
                "Loud music playing from the apartment above late at night. It has been happening for 3 days.",
                "Pending", R.drawable.ic_announcement, R.color.dark_coffee, R.color.orange
            )
        )
        complaintList.add(
            Complaint(
                "3", "Elevator Malfunction", "Block C", "Oct 20, 09:00 AM",
                "Service elevator buttons on 5th floor are not responding.",
                "Resolved", R.drawable.ic_elevator, R.color.deep_teal, R.color.status_green
            )
        )
        complaintList.add(
            Complaint(
                "4", "Unauthorized Parking", "Slot 45", "Oct 25, 08:45 AM",
                "A silver sedan is parked in my designated spot (Slot 45) since last night.",
                "Pending", R.drawable.ic_car, R.color.midnight_blue, R.color.purple
            )
        )
        adapter.notifyDataSetChanged()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}