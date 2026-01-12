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
import model.VisitorModel

class CheckOutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckOutBinding
    private lateinit var adapter: ActiveVisitorAdapter
    private val activeVisitors = mutableListOf<VisitorModel>()

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
    }

    private fun setupRecyclerView() {
        adapter = ActiveVisitorAdapter(this, activeVisitors) { visitor ->
            Toast.makeText(this, "Checking out ${visitor.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvActiveVisitors.layoutManager = LinearLayoutManager(this)
        binding.rvActiveVisitors.adapter = adapter
    }


}