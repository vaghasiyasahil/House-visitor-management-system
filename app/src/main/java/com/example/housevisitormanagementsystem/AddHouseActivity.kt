package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.core.content.IntentCompat
import com.example.housevisitormanagementsystem.databinding.ActivityAddHouseBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Loader
import com.example.housevisitormanagementsystem.utils.Validators
import com.google.firebase.Timestamp
import types.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.AnnouncementModel
import model.HouseModel
import types.AnnouncementsType

class AddHouseActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddHouseBinding
    private var houseId: String? = null
    private var isEditMode = false
    private var originalHouse: HouseModel? = null

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

        if (intent.hasExtra("houseData")) {
            val house = IntentCompat.getParcelableExtra(intent, "houseData", HouseModel::class.java)
            if (house != null) {
                originalHouse = house
                houseId = house.id
                isEditMode = true
                preFillData(house)
                binding.btnAddHouse.text = "Update House"
            }
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
        checkUniquenessAndSave(houseNo, ownerEmail, ownerName, password, members)
    }

    private fun checkUniquenessAndSave(
        houseNo: String, 
        email: String, 
        name: String, 
        password: String, 
        members: Int
    ) {
        lifecycleScope.launch {
            binding.btnAddHouse.isEnabled = false
            binding.btnAddHouse.text = "Checking..."
            try{
                FirestoreService().getDocumentsWithEqualToConditionsOnce(DatabaseTableName.HOUSES, mapOf("houseNo" to houseNo)){
                        houseSnap ->
                    if(houseSnap != null && !isEditMode && !houseSnap.isEmpty){
                        Toast.makeText(this@AddHouseActivity,"House Number already exists",Toast.LENGTH_LONG);
                        binding.etHouseNumber.error = "House Number already exists"
                        resetButtonState()
                        return@getDocumentsWithEqualToConditionsOnce
                    }else{
                        FirestoreService().getDocumentsWithEqualToConditionsOnce(DatabaseTableName.USER, mapOf("email" to email)){
                                userSnap ->
                            if(userSnap!=null && !isEditMode && !userSnap.isEmpty){
                                Toast.makeText(this@AddHouseActivity,"Email already registered",Toast.LENGTH_LONG);
                                binding.etOwnerEmail.error = "Email already registered"
                                resetButtonState()
                                return@getDocumentsWithEqualToConditionsOnce
                            }else{
                                saveData(houseNo, email, name, password, members)
                            }
                        }
                    }
                }
            }catch (e:Exception){
                Log.d("Checking House Error","Fail To Checking houseNo and User email = $e")
                Toast.makeText(this@AddHouseActivity, "Fail To Checking House", Toast.LENGTH_SHORT).show()
                resetButtonState();
            }
        }
    }

    private fun saveData(houseNo: String, email: String, name: String, password: String, members: Int) {
        binding.btnAddHouse.text = if (isEditMode) "Updating..." else "Saving..."
        val house = if (isEditMode) {
            originalHouse?.copy(
                houseNo = houseNo,
                ownerName = name,
                ownerEmail = email,
                password = password,
                members = members,
                updatedAt = Timestamp.now()
            )
        } else {
            HouseModel(
                houseNo = houseNo,
                ownerName = name,
                ownerEmail = email,
                password = password,
                members = members,
                createdAt = Timestamp.now()
            )
        }
        if(!isEditMode){
            lifecycleScope.launch {
                try{
                    FirestoreService().addDocument(DatabaseTableName.HOUSES,house!!.toHashMap());
                    Toast.makeText(this@AddHouseActivity, "House Add Successfully", Toast.LENGTH_LONG).show()
                    finish()
                }catch (e:Exception){
                    Log.d("Add House Error","Fail To Add House = $e")
                    Toast.makeText(this@AddHouseActivity, "Fail To Add House", Toast.LENGTH_SHORT).show()
                }finally {
                    resetButtonState()
                }
            }
        }else{
            lifecycleScope.launch {
                try{
                    FirestoreService().updateDocument(DatabaseTableName.HOUSES,houseId!!, house!!.toHashMap())
                    Toast.makeText(this@AddHouseActivity, "House Updated", Toast.LENGTH_LONG).show()
                    finish()
                }catch (e:Exception){
                    Log.d("Update House Error","Fail To Update House = $e")
                    Toast.makeText(this@AddHouseActivity, "Fail To Update House", Toast.LENGTH_SHORT).show()
                }finally {
                    resetButtonState()
                }
            }
        }
    }

    private fun resetButtonState() {
        binding.btnAddHouse.isEnabled = true
        binding.btnAddHouse.text = if (isEditMode) "Update" else "Add House"
    }
}