package com.example.housevisitormanagementsystem

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityAddSecurityGuardBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Validators
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.HouseModel
import model.SecurityGuardModel
import services.base64Service
import types.SecurityGuardShiftType
import types.UserRole
import java.io.InputStream

class AddSecurityGuardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSecurityGuardBinding
    private var guardId: String? = null
    private var isEditMode = false
    private var originalGuard: SecurityGuardModel? = null
    private var capturedImage: Bitmap? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.extras?.getParcelable("data", Bitmap::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.extras?.get("data") as? Bitmap
            }
            
            if (imageBitmap != null) {
                setCapturedImage(imageBitmap)
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    setCapturedImage(bitmap)
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setCapturedImage(bitmap: Bitmap) {
        capturedImage = bitmap
        binding.ivGuardPhoto.setImageBitmap(bitmap)
        binding.ivGuardPhoto.visibility = View.VISIBLE
        binding.pickContent.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSecurityGuardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)

        setupDropdowns()
        
        if (intent.hasExtra("guardData")) {
            val guard = IntentCompat.getParcelableExtra(intent, "guardData", SecurityGuardModel::class.java)
            if (guard != null) {
                originalGuard = guard
                guardId = guard.id
                isEditMode = true
                preFillData(guard)
                binding.btnSaveGuard.text = "Update Guard"
                binding.etEmail.isEnabled = false
            }
        }

        setupClickListeners()
        setupTextWatchers()
    }

    private fun setupDropdowns() {
        val shifts = SecurityGuardShiftType.values().map { it.name }
        val shiftAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, shifts)
        binding.actvShift.setAdapter(shiftAdapter)
    }

    private fun preFillData(guard: SecurityGuardModel) {
        binding.etFullName.editText?.setText(guard.name)
        binding.etEmail.editText?.setText(guard.email)
        binding.etPhoneNumber.editText?.setText(guard.phoneNumber)
        binding.etSalary.editText?.setText(guard.salary)
        binding.etPassword.editText?.setText(guard.password)
        binding.actvShift.setText(guard.shift.name, false)
        if (guard.image.isNotEmpty()) {
            val bitmap = base64Service.base64ToBitmap(guard.image)
            if (bitmap != null) {
                setCapturedImage(bitmap)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.main.setOnClickListener { KeyboardUtils.hideKeyboard(this) }
        binding.btnSaveGuard.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
            validateAndSave()
        }
        binding.btnPickPhoto.setOnClickListener {
            showImagePickOptions()
        }
    }

    private fun showImagePickOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photo")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> checkCameraPermission()
                1 -> launchGallery()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            cameraLauncher.launch(takePictureIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Camera not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }
    
    private fun setupTextWatchers() {
        val layouts = listOf(
            binding.etFullName, binding.etEmail, binding.etPhoneNumber, 
            binding.etSalary, binding.etPassword, binding.spShift
        )
        layouts.forEach { layout ->
            layout.editText?.addTextChangedListener { layout.error = null }
        }
    }

    private fun validateAndSave() {
        val name = binding.etFullName.editText?.text.toString().trim()
        val email = binding.etEmail.editText?.text.toString().trim()
        val phone = binding.etPhoneNumber.editText?.text.toString().trim()
        val salary = binding.etSalary.editText?.text.toString().trim()
        val password = binding.etPassword.editText?.text.toString().trim()
        val shift = binding.actvShift.text.toString().trim()

        var isValid = true
        
        if (Validators.validateName(name, "Full Name") != null) {
            binding.etFullName.error = Validators.validateName(name, "Full Name")
            isValid = false
        }
        if (Validators.validateEmail(email) != null) {
            binding.etEmail.error = Validators.validateEmail(email)
            isValid = false
        }
        if (Validators.validateMobile(phone) != null) {
            binding.etPhoneNumber.error = Validators.validateMobile(phone)
            isValid = false
        }
        if (Validators.validateRequired(salary, "Salary") != null) {
            binding.etSalary.error = Validators.validateRequired(salary, "Salary")
            isValid = false
        }
        if (Validators.validatePassword(password) != null) {
            binding.etPassword.error = Validators.validatePassword(password)
            isValid = false
        }
        if (shift.isEmpty()) {
            binding.spShift.error = "Shift is required"
            isValid = false
        }

        if (capturedImage == null && !isEditMode) {
            Toast.makeText(this, "Please select guard photo", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (!isValid) return
        checkUniquenessAndSave(name, email, phone, salary, password, shift)
    }

    private fun checkUniquenessAndSave(name: String, email: String, phone: String, salary: String, password: String, shift: String) {

        lifecycleScope.launch {
            binding.btnSaveGuard.isEnabled = false
            binding.btnSaveGuard.text = "Saving..."
            try{
                FirestoreService().getDocumentsWithEqualToConditionsOnce(DatabaseTableName.USER, mapOf("email" to email)){
                        userSnap ->
                    if(userSnap!=null && !isEditMode && !userSnap.isEmpty){
                        Toast.makeText(this@AddSecurityGuardsActivity,"Email already registered",Toast.LENGTH_LONG);
                        binding.etEmail.error = "Email already registered"
                        resetButtonState()
                        return@getDocumentsWithEqualToConditionsOnce
                    }else{
                        saveGuard(name, email, phone, salary, password, shift)
                    }
                }
            }catch (e:Exception){
                Log.d("Checking Security Guard Error","Fail To Checking Security Guard = $e")
                Toast.makeText(this@AddSecurityGuardsActivity, "Fail To Checking Security Guard", Toast.LENGTH_SHORT).show()
                resetButtonState();
            }
        }
    }

    private fun saveGuard(name: String, email: String, phone: String, salary: String, password: String, shift: String) {
        val guard = if (isEditMode) {
            originalGuard?.copy(
                name = name,
                email = email,
                phoneNumber = phone,
                salary = salary,
                password = password,
                shift = SecurityGuardShiftType.fromString(shift) ?: SecurityGuardShiftType.Morning,
                image = if (capturedImage != null) base64Service.bitmapToBase64(capturedImage!!) else originalGuard?.image ?: "",
                updatedAt = Timestamp.now()
            )
        } else {
            SecurityGuardModel(
                name = name,
                email = email,
                phoneNumber = phone,
                salary = salary,
                password = password,
                image = if (capturedImage != null) base64Service.bitmapToBase64(capturedImage!!) else "",
                shift = SecurityGuardShiftType.fromString(shift) ?: SecurityGuardShiftType.Morning,
                createdAt = Timestamp.now()
            )
        }

        lifecycleScope.launch {
            try {
                if (!isEditMode) {
                    val addedGuardId = FirestoreService().addDocument(DatabaseTableName.SECURITY_GUARDS, guard!!.toHashMap())
                    if (addedGuardId != null) {
                        val userData = hashMapOf<String, Any?>(
                            "id" to addedGuardId,
                            "email" to email,
                            "password" to password,
                            "role" to UserRole.SecurityGuard.name,
                            "name" to name,
                            "createdAt" to Timestamp.now()
                        )
                        FirestoreService().addDocument(DatabaseTableName.USER, userData, addedGuardId)
                        Toast.makeText(this@AddSecurityGuardsActivity, "Security Guard Add Successfully", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddSecurityGuardsActivity, "Fail To Add Security Guard", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val success = FirestoreService().updateDocument(DatabaseTableName.SECURITY_GUARDS, guardId!!, guard!!.toHashMap())
                    if (success) {
                        val userData = hashMapOf<String, Any?>(
                            "email" to email,
                            "password" to password,
                            "role" to UserRole.SecurityGuard.name,
                            "name" to name,
                            "updatedAt" to Timestamp.now()
                        )
                        FirestoreService().updateDocument(DatabaseTableName.USER, guardId!!, userData)
                        Toast.makeText(this@AddSecurityGuardsActivity, "Security Guard Updated", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddSecurityGuardsActivity, "Fail To Update Security Guard", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.d(if (isEditMode) "Update Security Guard Error" else "Add Security Guard Error", "Error: $e")
                Toast.makeText(this@AddSecurityGuardsActivity, "Error saving security guard", Toast.LENGTH_SHORT).show()
            } finally {
                resetButtonState()
            }
        }
    }

    private fun resetButtonState() {
        binding.btnSaveGuard.isEnabled = true
        binding.btnSaveGuard.text = if (isEditMode) "Update Guard" else "Save Guard"
    }
}