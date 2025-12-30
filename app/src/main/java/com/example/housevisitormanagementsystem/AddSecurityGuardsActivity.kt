package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.housevisitormanagementsystem.databinding.ActivityAddSecurityGuardBinding

class AddSecurityGuardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSecurityGuardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSecurityGuardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar customization
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupDropdowns()
        setupClickListeners()
    }

    private fun setupDropdowns() {
        // Shift Dropdown
        val shifts = arrayOf("Morning", "Evening", "Night")
        val shiftAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, shifts)
        binding.actvShift.setAdapter(shiftAdapter)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSaveGuard.setOnClickListener {
            // TODO: Implement save guard logic
            android.widget.Toast.makeText(this, "Guard Saved Successfully", android.widget.Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}