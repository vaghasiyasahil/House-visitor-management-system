package com.example.housevisitormanagementsystem

import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.housevisitormanagementsystem.databinding.ActivityAddHouseBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Validators
import com.google.firebase.firestore.FirebaseFirestore
import model.House
import java.util.UUID

class AddHouseActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddHouseBinding
    private val db = FirebaseFirestore.getInstance()
    private var houseId: String? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHouseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check for Edit Mode
        if (intent.hasExtra("houseData")) {
            val house = intent.getSerializableExtra("houseData") as House
            houseId = house.houseId
            isEditMode = true
            preFillData(house)
            binding.btnAddHouse.text = "Update House"
        }

        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnAddHouse.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
            
            val houseNo = binding.etHouseNumber.editText?.text.toString().trim()
            val ownerName = binding.etOwnerName.editText?.text.toString().trim()
            val ownerEmail = binding.etOwnerEmail.editText?.text.toString().trim()
            val password = binding.etPassword.editText?.text.toString().trim()
            val members = 4 // Default or add input field if needed

            binding.etHouseNumber.error = Validators.validateRequired(houseNo,"House number")
            binding.etOwnerName.error = Validators.validateName(ownerName,"Owner name")
            binding.etOwnerEmail.error = Validators.validateEmail(ownerEmail)
            binding.etPassword.error = Validators.validatePassword(password)
            
            if (binding.etHouseNumber.error == null && binding.etOwnerName.error == null &&
                binding.etOwnerEmail.error == null && binding.etPassword.error == null
            ) {
                saveHouse(houseNo, ownerName, ownerEmail, password, members)
            }
        }

        binding.etHouseNumber.editText?.addTextChangedListener {
            binding.etHouseNumber.error = null
        }
        binding.etOwnerName.editText?.addTextChangedListener {
            binding.etOwnerName.error = null
        }
        binding.etOwnerEmail.editText?.addTextChangedListener {
            binding.etOwnerEmail.error = null
        }
        binding.etPassword.editText?.addTextChangedListener {
            binding.etPassword.error = null
        }
    }

    private fun preFillData(house: House) {
        binding.etHouseNumber.editText?.setText(house.houseNo)
        binding.etOwnerName.editText?.setText(house.ownerName)
        binding.etOwnerEmail.editText?.setText(house.ownerEmail)
        binding.etPassword.editText?.setText(house.password)
    }

    private fun saveHouse(houseNo: String, ownerName: String, ownerEmail: String, password: String, members: Int) {
        val id = houseId ?: UUID.randomUUID().toString()
        val house = House(id, houseNo, ownerName, ownerEmail, password, members)

        val collection = db.collection("houses")
        
        // Show loading if you want, or just wait
        binding.btnAddHouse.isEnabled = false
        binding.btnAddHouse.text = "Saving..."

        collection.document(id).set(house)
            .addOnSuccessListener {
                Toast.makeText(this, if (isEditMode) "House Updated" else "House Added", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnAddHouse.isEnabled = true
                binding.btnAddHouse.text = if (isEditMode) "Update House" else "Add House"
            }
    }
}