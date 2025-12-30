package com.example.housevisitormanagementsystem

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import adapter.SecurityGuardAdapter
import com.example.housevisitormanagementsystem.databinding.ActivityViewSecurityGuardBinding
import model.SecurityGuard
import java.util.Locale

class ViewSecurityGuardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewSecurityGuardBinding
    private lateinit var adapter: SecurityGuardAdapter
    private val allGuards = ArrayList<SecurityGuard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewSecurityGuardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0 // Clear light status bar flag to use light icons

        setupDummyData()
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupClickListeners()

        binding.fabAddGuard.setOnClickListener {
            intent = Intent(this, AddSecurityGuardsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.fabAddGuard.setOnClickListener {
            // TODO: Navigate to Add Guard Screen
            // For now, show a placeholder or do nothing
             android.widget.Toast.makeText(this, "Add Guard Clicked", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDummyData() {
        allGuards.add(
            SecurityGuard(
                "John Doe", "GD-8821", "Morning Shift", "Block A", "Active", R.drawable.person_image
            )
        )
        allGuards.add(
            SecurityGuard(
                "Jane Smith", "GD-9942", "Night Shift", "Block B", "Night", R.drawable.person_image
            )
        )
        allGuards.add(
            SecurityGuard(
                "Robert Fox", "GD-1102", "Off Duty", "", "Off", R.drawable.person_image
            )
        )
        // Add more dummy data to make the list scrollable/interesting if needed
        allGuards.add(
            SecurityGuard(
                "Michael Brown", "GD-4521", "Morning Shift", "Block C", "Active", R.drawable.person_image
            )
        )
        allGuards.add(
            SecurityGuard(
                "Emily White", "GD-3321", "Night Shift", "Block A", "Night", R.drawable.person_image
            )
        )
    }

    private fun setupRecyclerView() {
        adapter = SecurityGuardAdapter(this, allGuards)
        binding.rvGuards.layoutManager = LinearLayoutManager(this)
        binding.rvGuards.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString(), getCurrentFilter())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedChangeListener { group, checkedId ->
            val query = binding.etSearch.text.toString()
            filterList(query, idToFilterName(checkedId))
        }
    }

    private fun getCurrentFilter(): String {
        return idToFilterName(binding.chipGroupFilters.checkedChipId)
    }

    private fun idToFilterName(id: Int): String {
        return when (id) {
            R.id.chipMorning -> "Morning Shift"
            R.id.chipNight -> "Night Shift"
            else -> "All Guards"
        }
    }

    private fun filterList(query: String, filterType: String) {
        val filteredList = allGuards.filter { guard ->
            val matchesSearch = guard.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT)) ||
                    guard.guardId.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
            
            val matchesFilter = when (filterType) {
                "Morning Shift" -> guard.shift == "Morning Shift"
                "Night Shift" -> guard.shift == "Night Shift"
                else -> true // All Guards
            }

            matchesSearch && matchesFilter
        }
        
        // Update adapter
        binding.rvGuards.adapter = SecurityGuardAdapter(this, filteredList)
    }
}