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
import model.VisitorModel

class AllVisitorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllVisitorsBinding
    private val visitorList = mutableListOf<VisitorModel>()
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
    }

    private fun setupRecyclerView() {
        adapter = AllVisitorsAdapter(this, visitorList)
        binding.rvAllVisitors.layoutManager = LinearLayoutManager(this)
        binding.rvAllVisitors.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
    }
}
