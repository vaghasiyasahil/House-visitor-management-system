package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityAddQrCodeBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Validators
import com.google.firebase.Timestamp
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.QrCodeModel
import model.VisitorModel
import services.base64Service
import types.VisitorStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddQrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddQrCodeBinding
    private var capturedImage: Bitmap? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(contentResolver, it)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(contentResolver, it)
                }
                
                capturedImage = bitmap
                binding.ivCapturedPhoto.setImageBitmap(bitmap)
                binding.ivCapturedPhoto.visibility = View.VISIBLE
                binding.captureContent.visibility = View.GONE
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = getColor(R.color.dark_navy_bg)
            insets
        }

        setupListeners()
        setupPurposeDropdown()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnGenerateQr.setOnClickListener { validateAndGenerate() }
        binding.btnCapturePhoto.setOnClickListener { pickImageFromGallery() }

        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
        }

        // Show Purpose dropdown on click
        binding.etPurpose.setOnTouchListener { _, _ ->
            binding.etPurpose.showDropDown()
            false
        }
        binding.etPurpose.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.etPurpose.showDropDown()
            }
        }
    }

    private fun setupPurposeDropdown() {
        val purposes = arrayOf("Guest", "Delivery", "Service", "Maintenance", "Emergency", "Courier", "Other")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, purposes)
        binding.etPurpose.setAdapter(adapter)
    }

    private fun pickImageFromGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun validateAndGenerate() {
        val name = binding.etVisitorName.text.toString().trim()
        val mobile = binding.etMobileNumber.text.toString().trim()
        val purpose = binding.etPurpose.text.toString().trim()

        if (Validators.validateName(name, "Name") != null) {
            Toast.makeText(this, Validators.validateName(name, "Name"), Toast.LENGTH_SHORT).show()
            return
        }
        if (Validators.validateMobile(mobile) != null) {
            Toast.makeText(this, Validators.validateMobile(mobile), Toast.LENGTH_SHORT).show()
            return
        }
        if (Validators.validateRequired(purpose, "Purpose") != null) {
            Toast.makeText(this, Validators.validateRequired(purpose, "Purpose"), Toast.LENGTH_SHORT).show()
            return
        }
        if (capturedImage == null) {
            Toast.makeText(this, "Please select visitor photo", Toast.LENGTH_SHORT).show()
            return
        }

        generateNewVisitorQr()
    }

    private fun generateNewVisitorQr() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnGenerateQr.isEnabled = false

        val houseNo = SharedPreferencesService.getUserHouseNo()
        val ownerEmail = SharedPreferencesService.getUserEmail()

        lifecycleScope.launch {
            val visitorModel = VisitorModel(
                name = binding.etVisitorName.text.toString().trim(),
                mobile = binding.etMobileNumber.text.toString().trim(),
                houseNo = houseNo,
                purpose = binding.etPurpose.text.toString().trim(),
                status = VisitorStatus.Approved,
                visitorPhoto = base64Service.bitmapToBase64(capturedImage!!),
                entryTime = Timestamp.now(),
                houseOwnerFcmToken = SharedPreferencesService.getFcmToken()
            )

            val visitorId = FirestoreService().addDocument(
                DatabaseTableName.VISITOR,
                visitorModel.toHashMap()
            )

            if (visitorId != null) {
                val qrData = UUID.randomUUID().toString()
                val now = Timestamp.now()
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.HOUR_OF_DAY, 24)
                val expiresAt = Timestamp(calendar.time)

                val qrCodeModel = QrCodeModel(
                    visitorId = visitorId,
                    visitorName = visitorModel.name,
                    visitorMobile = visitorModel.mobile,
                    houseNo = houseNo,
                    purpose = visitorModel.purpose,
                    qrData = qrData,
                    createdAt = now,
                    expiresAt = expiresAt,
                    isUsed = false,
                    houseOwnerEmail = ownerEmail,
                    visitorPhoto = visitorModel.visitorPhoto
                )

                val qrCodeId = FirestoreService().addDocument(
                    DatabaseTableName.QR_CODES,
                    qrCodeModel.toHashMap()
                )

                if (qrCodeId != null) {
                    Toast.makeText(this@AddQrCodeActivity, "QR Code generated successfully!", Toast.LENGTH_SHORT).show()

                    // Navigate to details activity
                    val intent = Intent(this@AddQrCodeActivity, ViewQrCodeDetailsActivity::class.java).apply {
                        putExtra("QR_DATA", qrData)
                        putExtra("QR_CODE_ID", qrCodeId)
                        putExtra("VISITOR_NAME", visitorModel.name)
                        putExtra("VISITOR_PURPOSE", visitorModel.purpose)
                        putExtra("VISITOR_MOBILE", visitorModel.mobile)
                        putExtra("VISITOR_PHOTO", visitorModel.visitorPhoto)
                        putExtra("HOUSE_NO", houseNo)
                        val sdf = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.getDefault())
                        putExtra("VISITOR_VALIDITY", "Valid until: ${sdf.format(expiresAt.toDate())}")
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@AddQrCodeActivity, "Failed to generate QR Code", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@AddQrCodeActivity, "Failed to register visitor", Toast.LENGTH_SHORT).show()
            }

            binding.progressBar.visibility = View.GONE
            binding.btnGenerateQr.isEnabled = true
        }
    }
}