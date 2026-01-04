package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import adapter.VisitorLogAdapter
import com.example.housevisitormanagementsystem.databinding.ActivityViewVisitorBinding
import model.VisitorLog
import com.example.housevisitormanagementsystem.utils.KeyboardUtils

class ViewVisitorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewVisitorBinding
    private lateinit var adapter: VisitorLogAdapter
    private var visitorList = mutableListOf<VisitorLog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewVisitorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupRecyclerView()
        setupDummyData()
        setupSearch()
        setupFilters()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = VisitorLogAdapter(this, visitorList)
        binding.rvVisitorLogs.layoutManager = LinearLayoutManager(this)
        binding.rvVisitorLogs.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // TODO: Implement actual search filtering
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
        }
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.chipInside -> "Inside"
                R.id.chipCheckedOut -> "Checked Out"
                R.id.chipDelivery -> "Delivery"
                else -> "All"
            }
            Toast.makeText(this, "Filter: $filter", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnCalendar.setOnClickListener {
            Toast.makeText(this, "Calendar Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.btnFilter.setOnClickListener {
            Toast.makeText(this, "Filter Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.btnGenerateReport.setOnClickListener {
            Toast.makeText(this, "Exporting to PDF...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDummyData() {
        visitorList.clear()
        visitorList.add(
            VisitorLog(
                id = "1", name = "John Doe", type = "Guest", unit = "Unit 101", 
                status = "Inside", entryTime = "10:00 AM", isActive = true
            )
        )
        visitorList.add(
            VisitorLog(
                id = "2", name = "Amazon Delivery", type = "Delivery", unit = "Unit 305", 
                status = "Checked Out", entryTime = "11:15 AM", exitTime = "11:20 AM", isActive = false
            )
        )
        visitorList.add(
            VisitorLog(
                id = "3", name = "Mike Smith", type = "Maintenance", unit = "Clubhouse", 
                status = "Inside", entryTime = "08:00 AM", isActive = false
            )
        )
        visitorList.add(
            VisitorLog(
                id = "4", name = "Sarah Connor", type = "Guest", unit = "Unit 202", 
                status = "Checked Out", entryTime = "09:30 AM", exitTime = "12:45 PM", isActive = false
            )
        )
        visitorList.add(
            VisitorLog(
                id = "5", name = "Urban Company", type = "Maintenance", unit = "Unit 401", 
                status = "Inside", entryTime = "02:15 PM", isActive = false
            )
        )
        visitorList.add(
            VisitorLog(
                id = "6", name = "Zomato", type = "Delivery", unit = "Unit 105", 
                status = "Inside", entryTime = "03:00 PM", isActive = false
            )
        )
        adapter.notifyDataSetChanged()
        binding.tvEntriesCount.text = "${visitorList.size} Entries"
    }
}