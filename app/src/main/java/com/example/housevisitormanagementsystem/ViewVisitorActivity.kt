package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
                "1", "John Doe", "Guest", "Unit 101", "Inside",
                "10:00 AM", null, R.drawable.person_image, null, null, true
            )
        )
        visitorList.add(
            VisitorLog(
                "2", "Amazon Delivery", "Delivery", "Unit 305", "Checked Out",
                "11:15 AM", "11:20 AM", null, R.drawable.ic_delivery, R.color.orange, false
            )
        )
        visitorList.add(
            VisitorLog(
                "3", "Mike Smith", "Maintenance", "Clubhouse", "Inside",
                "08:00 AM", null, null, R.drawable.ic_maintenance, R.color.bright_blue, false
            )
        )
        visitorList.add(
            VisitorLog(
                "4", "Sarah Connor", "Guest", "Unit 202", "Checked Out",
                "09:30 AM", "12:45 PM", R.drawable.person_image, null, null, false
            )
        )
        visitorList.add(
            VisitorLog(
                "5", "Urban Company", "Maintenance", "Unit 401", "Inside",
                "02:15 PM", null, null, R.drawable.ic_maintenance, R.color.purple, false
            )
        )
        visitorList.add(
            VisitorLog(
                "6", "Zomato", "Delivery", "Unit 105", "Inside",
                "03:00 PM", null, null, R.drawable.ic_delivery, R.color.red, false
            )
        )
        adapter.notifyDataSetChanged()
        binding.tvEntriesCount.text = "${visitorList.size} Entries"
    }
}