package com.example.housevisitormanagementsystem

import adapter.ActiveVisitorAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityCheckOutBinding
import model.VisitorLog

class CheckOutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckOutBinding
    private lateinit var adapter: ActiveVisitorAdapter
    private val activeVisitors = mutableListOf<VisitorLog>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        
        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_navy_bg)
        window.decorView.systemUiVisibility = 0

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        loadDummyData()
    }

    private fun setupRecyclerView() {
        adapter = ActiveVisitorAdapter(this, activeVisitors) { visitor ->
            Toast.makeText(this, "Checking out ${visitor.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvActiveVisitors.layoutManager = LinearLayoutManager(this)
        binding.rvActiveVisitors.adapter = adapter
    }


    private fun loadDummyData() {
        activeVisitors.clear()
        activeVisitors.add(
            VisitorLog(
                id = "1", name = "John Doe", unit = "Unit 402", ownerName = "Mr. Smith",
                entryTime = "10:42 AM", vehiclePlate = "ABC - 1234", entryGate = "Main Gate"
            )
        )
        activeVisitors.add(
            VisitorLog(
                id = "2", name = "Sarah Jenkins", unit = "Unit 105", ownerName = "Ms. Davis",
                entryTime = "11:15 AM", vehiclePlate = "XYZ - 9876", entryGate = "Side Gate"
            )
        )
        activeVisitors.add(
            VisitorLog(
                id = "3", name = "Amazon Delivery", unit = "Reception",
                entryTime = "11:45 AM", vehiclePlate = "DL - 2233", entryGate = "Service Entrance"
            )
        )
        
        adapter.notifyDataSetChanged()
        binding.tvActiveCount.text = "${activeVisitors.size} Active"
    }
}