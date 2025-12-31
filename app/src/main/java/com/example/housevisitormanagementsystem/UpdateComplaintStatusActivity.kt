package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityUpdateComplaintStatusBinding
import model.Complaint

class UpdateComplaintStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateComplaintStatusBinding
    private var selectedStatus: String = "Pending"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateComplaintStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        val complaint = intent.getSerializableExtra("complaint") as? Complaint
        if (complaint == null) {
            finish()
            return
        }

        setupUI(complaint)
        setupClickListeners()
    }

    private fun setupUI(complaint: Complaint) {
        binding.tvComplaintDetail.text = "#CMP-${complaint.id} • ${complaint.houseNo}"
        binding.tvDescription.text = complaint.title
        selectedStatus = complaint.status
        updateOptionSelection()
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.optionPending.setOnClickListener {
            selectedStatus = "Pending"
            updateOptionSelection()
        }

        binding.optionInProgress.setOnClickListener {
            selectedStatus = "In Progress"
            updateOptionSelection()
        }

        binding.optionResolved.setOnClickListener {
            selectedStatus = "Resolved"
            updateOptionSelection()
        }

        binding.btnUpdateStatus.setOnClickListener {
            Toast.makeText(this, "Status updated to $selectedStatus", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateOptionSelection() {
        binding.rbPending.isChecked = selectedStatus == "Pending"
        binding.optionPending.isSelected = selectedStatus == "Pending"

        binding.rbInProgress.isChecked = selectedStatus == "In Progress"
        binding.optionInProgress.isSelected = selectedStatus == "In Progress"

        binding.rbResolved.isChecked = selectedStatus == "Resolved"
        binding.optionResolved.isSelected = selectedStatus == "Resolved"
    }
}