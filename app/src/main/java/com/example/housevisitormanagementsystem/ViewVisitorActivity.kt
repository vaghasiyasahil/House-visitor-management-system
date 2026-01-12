package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityViewVisitorBinding
import model.VisitorModel
import com.example.housevisitormanagementsystem.utils.KeyboardUtils

class ViewVisitorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewVisitorBinding
    private var visitorList = mutableListOf<VisitorModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewVisitorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupSearch()
        setupFilters()
        setupClickListeners()
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
}