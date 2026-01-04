package com.example.housevisitormanagementsystem

import adapter.HouseSearchAdapter
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityCheckInBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.google.firebase.firestore.FirebaseFirestore
import constants.DatabaseTableName
import model.House

class CheckInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckInBinding
    private val db = FirebaseFirestore.getInstance()
    private var houseList = ArrayList<House>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
        fetchHouses()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
        }

        binding.btnCheckInVisitor.setOnClickListener {
            validateAndCheckIn()
        }

        // Show House No dropdown on focus or click
        binding.etHouseNo.setOnClickListener {
            binding.etHouseNo.showDropDown()
            binding.etHouseNo.requestFocus()
        }
        
        // Show Purpose dropdown on click
        binding.etPurpose.setOnClickListener {
            binding.etPurpose.showDropDown()
        }
    }

    private fun fetchHouses() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        db.collection(DatabaseTableName.HOUSES)
            .get()
            .addOnSuccessListener { value ->
                binding.progressBar.visibility = android.view.View.GONE
                houseList.clear()
                if (value != null) {
                    for (doc in value) {
                        val house = doc.toObject(House::class.java)
                        houseList.add(house)
                    }
                    setupHouseSelector()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = android.view.View.GONE
                Toast.makeText(this, "Error fetching houses: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun validateAndCheckIn() {
        val name = binding.etVisitorName.text.toString().trim()
        val mobile = binding.etMobileNumber.text.toString().trim()
        val houseNo = binding.etHouseNo.text.toString().trim()
        val purpose = binding.etPurpose.text.toString().trim()

        if (name.isEmpty() || mobile.isEmpty() || houseNo.isEmpty() || purpose.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Proceed with check-in logic
        Toast.makeText(this, "Checking in $name for $purpose...", Toast.LENGTH_SHORT).show()
    }

    private fun setupHouseSelector() {
        val adapter = HouseSearchAdapter(this, houseList)
        binding.etHouseNo.setAdapter(adapter)

        binding.etHouseNo.setOnItemClickListener { parent, view, position, id ->
            val selectedHouse = adapter.getItem(position)
            binding.etHouseNo.setText(selectedHouse?.houseNo)
            KeyboardUtils.hideKeyboard(this)
        }
        
        setupPurposeDropdown()
    }

    private fun setupPurposeDropdown() {
        val purposes = arrayOf("Guest", "Delivery", "Service", "Maintenance", "Emergency", "Courier", "Other")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, purposes)
        binding.etPurpose.setAdapter(adapter)
        
        // Ensure dropdown shows on click even if text is present
        binding.etPurpose.setOnTouchListener { v, event ->
            binding.etPurpose.showDropDown()
            false
        }
    }
}