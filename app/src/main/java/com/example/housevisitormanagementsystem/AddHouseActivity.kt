package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.housevisitormanagementsystem.databinding.ActivityAddHouseBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Validators
import types.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import constants.DatabaseTableName
import model.HouseModel

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
            val house = intent.getSerializableExtra("houseData") as HouseModel
            houseId = house.id
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
            validateAndSave()
        }

        setupTextWatchers()
    }

    private fun setupTextWatchers() {
        val rootLayouts = listOf(
            binding.etHouseNumber,
            binding.etOwnerName,
            binding.etOwnerEmail,
            binding.etMembers,
            binding.etPassword
        )
        rootLayouts.forEach { layout ->
            layout.editText?.addTextChangedListener { layout.error = null }
        }
    }

    private fun preFillData(house: HouseModel) {
        binding.etHouseNumber.editText?.setText(house.houseNo)
        binding.etOwnerName.editText?.setText(house.ownerName)
        binding.etOwnerEmail.editText?.setText(house.ownerEmail)
        binding.etMembers.editText?.setText(house.members.toString())
        binding.etPassword.editText?.setText(house.password)
        // Disable house number editing in edit mode to prevent conflict issues implies logic change
        binding.etHouseNumber.editText?.isEnabled = false
    }

    private fun validateAndSave() {
        val houseNo = binding.etHouseNumber.editText?.text.toString().trim()
        val ownerName = binding.etOwnerName.editText?.text.toString().trim()
        val ownerEmail = binding.etOwnerEmail.editText?.text.toString().trim()
        val memberStr = binding.etMembers.editText?.text.toString().trim()
        val password = binding.etPassword.editText?.text.toString().trim()

        var isValid = true

        if (Validators.validateRequired(houseNo, "House number") != null) {
            binding.etHouseNumber.error = Validators.validateRequired(houseNo, "House number")
            isValid = false
        }
        if (Validators.validateName(ownerName, "Owner name") != null) {
            binding.etOwnerName.error = Validators.validateName(ownerName, "Owner name")
            isValid = false
        }
        if (Validators.validateEmail(ownerEmail) != null) {
            binding.etOwnerEmail.error = Validators.validateEmail(ownerEmail)
            isValid = false
        }
        if (memberStr.isEmpty()) {
            binding.etMembers.error = "Members count is required"
            isValid = false
        }
        if (Validators.validatePassword(password) != null) {
            binding.etPassword.error = Validators.validatePassword(password)
            isValid = false
        }
        if (!isValid) return
        val members = memberStr.toIntOrNull() ?: 0
        binding.btnAddHouse.isEnabled = false
        binding.btnAddHouse.text = "Checking..."
        checkUniquenessAndSave(houseNo, ownerEmail, ownerName, password, members)
    }

    private fun checkUniquenessAndSave(
        houseNo: String, 
        email: String, 
        name: String, 
        password: String, 
        members: Int
    ) {
        val houseQuery = db.collection(DatabaseTableName.HOUSES).whereEqualTo("houseNo", houseNo)
        houseQuery.get().addOnSuccessListener { houseSnap ->
            if (!isEditMode && !houseSnap.isEmpty) {
                Toast.makeText(this,"House Number already exists",Toast.LENGTH_LONG);
                binding.etHouseNumber.error = "House Number already exists"
                resetButtonState()
                return@addOnSuccessListener
            }
            val userQuery = db.collection(DatabaseTableName.USER).whereEqualTo("email", email)
            userQuery.get().addOnSuccessListener { userSnap ->
                var emailExistsOtherUser = false
                if (!userSnap.isEmpty) {
                    if (!isEditMode) {
                         emailExistsOtherUser = true
                    } else {
                    }
                }

                if (emailExistsOtherUser) {
                    binding.etOwnerEmail.error = "Email already registered"
                    resetButtonState()
                    return@addOnSuccessListener
                }
                saveData(houseNo, email, name, password, members)
            }
        }
    }

    private fun saveData(houseNo: String, email: String, name: String, password: String, members: Int) {
        val id = if (isEditMode && houseId != null) houseId!! else generateUniqueId()
        
        val house = HouseModel(
            id = id,
            houseNo = houseNo,
            ownerName = name,
            ownerEmail = email,
            password = password,
            members = members
        )

        val batch = db.batch()

        val houseRef = db.collection(DatabaseTableName.HOUSES).document(id)
        batch.set(houseRef, house)

        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "password" to password,
            "role" to UserRole.HouseOwner.name,
            "id" to id
        )

        db.collection(DatabaseTableName.USER).whereEqualTo("houseId", id).get()
            .addOnSuccessListener { userSnap ->
                val userRef = if (!userSnap.isEmpty) {
                    userSnap.documents[0].reference
                } else {
                     db.collection(DatabaseTableName.USER).document(id)
                }
                
                batch.set(userRef, userMap)
                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, if (isEditMode) "House Updated" else "House Added", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    resetButtonState()
                }
            }
    }
    
    private fun generateUniqueId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20)
            .map { chars.random() }
            .joinToString("")
    }

    private fun resetButtonState() {
        binding.btnAddHouse.isEnabled = true
        binding.btnAddHouse.text = if (isEditMode) "Update House" else "Add House"
    }
}