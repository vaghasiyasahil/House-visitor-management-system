package com.example.housevisitormanagementsystem

import adapter.AllVisitorsAdapter
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityAllVisitorsBinding
import model.VisitorLog

class AllVisitorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllVisitorsBinding
    private val visitorList = mutableListOf<VisitorLog>()
    private lateinit var adapter: AllVisitorsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllVisitorsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupClickListeners()
        loadDummyData()
    }

    private fun setupRecyclerView() {
        adapter = AllVisitorsAdapter(this, visitorList)
        binding.rvAllVisitors.layoutManager = LinearLayoutManager(this)
        binding.rvAllVisitors.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadDummyData() {
        visitorList.clear()
        // Adding dummy data for UI verification
        visitorList.add(VisitorLog(name = "Sarah Connor", unit = "Unit 402", status = "Inside", type = "Guest", entryTime = "Oct 12, 10:42 AM"))
        visitorList.add(VisitorLog(name = "John Wick", unit = "Unit 105", status = "Checked Out", type = "Delivery", entryTime = "Oct 12, 09:15 AM"))
        visitorList.add(VisitorLog(name = "Agent Smith", unit = "Unit 202", status = "Inside", type = "Maintenance", entryTime = "Oct 11, 04:30 PM"))
        visitorList.add(VisitorLog(name = "Alice Wonder", unit = "Unit 303", status = "Inside", type = "Guest", entryTime = "Oct 11, 02:10 PM"))
        
        if (visitorList.isEmpty()) {
            binding.rvAllVisitors.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        } else {
            binding.rvAllVisitors.visibility = View.VISIBLE
            binding.emptyStateLayout.visibility = View.GONE
            adapter.notifyDataSetChanged()
        }
    }
}
