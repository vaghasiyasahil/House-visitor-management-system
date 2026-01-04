package com.example.housevisitormanagementsystem

import adapter.HouseSearchAdapter
import android.os.Bundle
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

        binding.tvPurpose.parent.let { (it as? android.view.View)?.setOnClickListener {
            // Logic for purpose selection would go here
            Toast.makeText(this, "Select Purpose", Toast.LENGTH_SHORT).show()
        } }
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

        if (name.isEmpty() || mobile.isEmpty() || houseNo.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Proceed with check-in logic
        Toast.makeText(this, "Checking in $name...", Toast.LENGTH_SHORT).show()
    }

    private fun setupHouseSelector() {
        val adapter = HouseSearchAdapter(this, houseList)
        binding.etHouseNo.setAdapter(adapter)

        binding.etHouseNo.setOnItemClickListener { parent, view, position, id ->
            val selectedHouse = adapter.getItem(position)
            binding.etHouseNo.setText(selectedHouse?.houseNo)
            // Move focus to next field if needed or hide keyboard
            KeyboardUtils.hideKeyboard(this)
        }
    }
}