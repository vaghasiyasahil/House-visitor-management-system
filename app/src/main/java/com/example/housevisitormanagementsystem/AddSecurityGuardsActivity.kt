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
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.housevisitormanagementsystem.databinding.ActivityAddSecurityGuardBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Validators
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.SecurityGuardModel
import services.base64Service
import types.SecurityGuardShiftType
import types.UserRole
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AddSecurityGuardsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddSecurityGuardBinding
    private val db = FirebaseFirestore.getInstance()
    private var guardId: String? = null
    private var isEditMode = false
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
            val guard = intent.getSerializableExtra("guardData") as SecurityGuardModel
            guardId = guard.id
            isEditMode = true
            preFillData(guard)
            binding.btnSaveGuard.text = "Update Guard"
            binding.etEmail.isEnabled = false
        }

        setupClickListeners()
        setupTextWatchers()
    }

    private fun setupDropdowns() {
        val shifts = arrayOf("Morning", "Night")
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

        binding.btnSaveGuard.isEnabled = false
        binding.btnSaveGuard.text = "Checking..."
        
        checkUniquenessAndSave(name, email, phone, salary, password, shift)
    }

    private fun checkUniquenessAndSave(name: String, email: String, phone: String, salary: String, password: String, shift: String) {
        db.collection(DatabaseTableName.USER).whereEqualTo("email", email).get()
            .addOnSuccessListener { userSnap ->
                if (!isEditMode && !userSnap.isEmpty) {
                    binding.etEmail.error = "Email already registered"
                    resetButtonState()
                    return@addOnSuccessListener
                }
                
                saveGuard(name, email, phone, salary, password, shift)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking uniqueness: ${it.message}", Toast.LENGTH_SHORT).show()
                resetButtonState()
            }
    }

    private fun saveGuard(name: String, email: String, phone: String, salary: String, password: String, shift: String) {
        val id = if (isEditMode && guardId != null) guardId!! else generateUniqueId()
        
        val guard = SecurityGuardModel(
            id = id,
            name = name,
            email = email,
            phoneNumber = phone,
            salary = salary,
            password = password,
            image = if (capturedImage != null) base64Service.bitmapToBase64(capturedImage!!) else "",
            shift = SecurityGuardShiftType.fromString(shift)?:SecurityGuardShiftType.Morning,
            createdAt = if (isEditMode) null else Timestamp.now(),
            updatedAt = if (isEditMode) Timestamp.now() else null
        )
        
        val batch = db.batch()
        
        val guardRef = db.collection(DatabaseTableName.SECURITY_GUARDS).document(id)
        batch.set(guardRef, guard)
        
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "password" to password,
            "role" to UserRole.SecurityGuard.name,
            "id" to id
        )
        
        db.collection(DatabaseTableName.USER).whereEqualTo("email", email).get()
            .addOnSuccessListener { userSnap ->
                val userRef = if (!userSnap.isEmpty) {
                    userSnap.documents[0].reference
                } else {
                    db.collection(DatabaseTableName.USER).document(id)
                }

                batch.set(userRef, userMap)
                
                batch.commit().addOnSuccessListener {
                    Toast.makeText(this, if (isEditMode) "Guard Updated" else "Guard Saved", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this, "Error saving: ${it.message}", Toast.LENGTH_SHORT).show()
                    resetButtonState()
                }
            }
    }

    private fun generateUniqueId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20).map { chars.random() }.joinToString("")
    }

    private fun resetButtonState() {
        binding.btnSaveGuard.isEnabled = true
        binding.btnSaveGuard.text = if (isEditMode) "Update Guard" else "Save Guard"
    }
}