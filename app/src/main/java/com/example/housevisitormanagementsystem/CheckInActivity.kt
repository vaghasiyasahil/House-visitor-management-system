package com.example.housevisitormanagementsystem

import SharedPreferencesService
import adapter.HouseSearchAdapter
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityCheckInBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Validators
import com.google.firebase.Timestamp
import java.io.ByteArrayOutputStream
import com.google.firebase.firestore.FirebaseFirestore
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.HouseModel
import model.VisitorModel
import services.NotificationService
import services.SendNotificationService
import services.base64Service
import types.VisitorStatus

class CheckInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckInBinding
    private val db = FirebaseFirestore.getInstance()
    private var houseList = ArrayList<HouseModel>()
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
                capturedImage = imageBitmap
                binding.ivCapturedPhoto.setImageBitmap(imageBitmap)
                binding.ivCapturedPhoto.visibility = View.VISIBLE
                binding.captureContent.visibility = View.GONE
            }
        }
    }

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
        binding.etHouseNo.setOnTouchListener { _, _ ->
            binding.etHouseNo.showDropDown()
            false
        }
        binding.etHouseNo.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.etHouseNo.showDropDown()
            }
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

        binding.btnCapturePhoto.setOnClickListener {
            checkCameraPermission()
        }

        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this);
        }
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

    private fun fetchHouses() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        db.collection(DatabaseTableName.HOUSES)
            .get()
            .addOnSuccessListener { value ->
                binding.progressBar.visibility = android.view.View.GONE
                houseList.clear()
                if (value != null) {
                    for (doc in value) {
                        val house = doc.toObject(HouseModel::class.java)
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

        if(Validators.validateName(name,"Name")!=null){
            Toast.makeText(this,Validators.validateName(name,"Name"),Toast.LENGTH_SHORT).show();
            return;
        }
        if(Validators.validateMobile(mobile)!=null){
            Toast.makeText(this,Validators.validateMobile(mobile),Toast.LENGTH_SHORT).show();
            return;
        }
        if(Validators.validateRequired(houseNo, "House No")!=null){
            Toast.makeText(this,Validators.validateRequired(houseNo, "House No"),Toast.LENGTH_SHORT).show();
            return;
        }

        val houseExist = houseList.any { it.houseNo == houseNo }
        if(!houseExist){
            Toast.makeText(this,"House No Invalid",Toast.LENGTH_SHORT).show();
            return;
        }

        if(Validators.validateRequired(purpose, "Purpose")!=null){
            Toast.makeText(this,Validators.validateRequired(purpose, "Purpose"),Toast.LENGTH_SHORT).show();
            return;
        }

        if (capturedImage == null) {
            Toast.makeText(this, "Please capture visitor photo", Toast.LENGTH_SHORT).show()
            return
        }

        saveCheckInData()
    }

    private fun saveCheckInData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCheckInVisitor.isEnabled = false

        var ownerEmail = ""
        var houseOwnerFcmToken = ""
        for(house in houseList){
            if(binding.etHouseNo.text.toString()==house.houseNo){
                ownerEmail = house.ownerEmail;
                break
            }
        }
        lifecycleScope.launch {
            FirestoreService().getDocumentsWithEqualToConditionsOnce(DatabaseTableName.USER,mapOf("email" to ownerEmail)){
                    userSnap ->
                if(userSnap!=null && !userSnap.isEmpty){
                    houseOwnerFcmToken = userSnap.documents[0].getString("fcmToken")?:""
                }
            }
        }

        val visitorModel = VisitorModel(
            name = binding.etVisitorName.text.toString(),
            houseNo = binding.etHouseNo.text.toString(),
            mobile = binding.etMobileNumber.text.toString(),
            purpose = binding.etPurpose.text.toString(),
            status = VisitorStatus.Pending,
            visitorPhoto = base64Service.bitmapToBase64(capturedImage!!),
            entryTime = Timestamp.now(),
            houseOwnerFcmToken = houseOwnerFcmToken,
            securityGuardFcmToken = SharedPreferencesService.getFcmToken()
        );
        lifecycleScope.launch{
            val docId = FirestoreService().addDocument(DatabaseTableName.VISITOR,visitorModel.toHashMap());
            if (docId != null) {
                SendNotificationService.sendNotification("New Visitor","${binding.etVisitorName.text} ${binding.etMobileNumber.text}",houseOwnerFcmToken)
                Toast.makeText(
                    this@CheckInActivity,
                    "Visitor checked in successfully",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this@CheckInActivity,
                    "Failed to check in visitor",
                    Toast.LENGTH_SHORT
                ).show()
                binding.progressBar.visibility = View.GONE
                binding.btnCheckInVisitor.isEnabled = true
            }
        }
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
    }
}