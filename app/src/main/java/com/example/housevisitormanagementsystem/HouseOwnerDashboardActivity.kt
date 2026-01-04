package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityHouseOwnerDashboardBinding

class HouseOwnerDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHouseOwnerDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseOwnerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.imgProfile.setOnClickListener {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
        }

        binding.notificationContainer.setOnClickListener {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnGenerateQR.setOnClickListener {
            Toast.makeText(this, "Generating QR Code...", Toast.LENGTH_SHORT).show()
        }

        binding.btnVisitorApproval.setOnClickListener {
            Toast.makeText(this, "Opening Visitor Approvals", Toast.LENGTH_SHORT).show()
        }

        binding.btnVisitorHistory.setOnClickListener {
            Toast.makeText(this, "Opening Visitor History", Toast.LENGTH_SHORT).show()
        }

        binding.btnAnnouncements.setOnClickListener {
            Toast.makeText(this, "Opening Announcements", Toast.LENGTH_SHORT).show()
        }

        binding.btnComplaints.setOnClickListener {
            Toast.makeText(this, "Opening Complaints", Toast.LENGTH_SHORT).show()
        }

        binding.btnBlockVisitor.setOnClickListener {
            Toast.makeText(this, "Opening Security Settings", Toast.LENGTH_SHORT).show()
        }

        binding.btnDirectory.setOnClickListener {
            Toast.makeText(this, "Opening Directory", Toast.LENGTH_SHORT).show()
        }

        binding.tvViewAll.setOnClickListener {
            Toast.makeText(this, "Opening All Recent Activity", Toast.LENGTH_SHORT).show()
        }
    }
}