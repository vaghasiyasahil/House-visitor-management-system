package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityQrCodeScanBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.QrCodeModel
import model.VisitorModel
import services.SendNotificationService
import services.setBase64Image
import types.VisitorStatus
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrCodeScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeScanBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var isScanning = true
    private var currentQrCode: QrCodeModel? = null
    private var currentVisitor: VisitorModel? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupBottomSheet()
        setupClickListeners()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    isScanning = true
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        val bottomSheetView = binding.bottomSheet

        bottomSheetView.findViewById<MaterialButton>(R.id.btnInstantApproval).setOnClickListener {
            approveVisitorEntry()
        }

        bottomSheetView.findViewById<MaterialButton>(R.id.btnDenyEntry).setOnClickListener {
            Toast.makeText(this, "Entry Denied", Toast.LENGTH_SHORT).show()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e("QrCodeScanActivity", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && isScanning) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient()

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        if (barcode.format == Barcode.FORMAT_QR_CODE) {
                            val rawValue = barcode.rawValue
                            if (rawValue != null) {
                                runOnUiThread {
                                    onQrCodeDetected(rawValue)
                                }
                                break
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QrCodeScanActivity", "Barcode scanning failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun onQrCodeDetected(data: String) {
        if (!isScanning) return
        isScanning = false

        // Validate QR code against Firestore
        validateQrCode(data)
    }

    private fun validateQrCode(qrData: String) {
        FirestoreService().getDocumentsWithEqualToConditionsOnce(
            DatabaseTableName.QR_CODES,
            mapOf("qrData" to qrData)
        ) { snapshot ->
            if (snapshot == null || snapshot.isEmpty) {
                showError("Invalid QR Code", "This QR code is not recognized by the system.")
                return@getDocumentsWithEqualToConditionsOnce
            }

            val doc = snapshot.documents[0]
            val qrCode = QrCodeModel.fromHashMap(doc.data ?: emptyMap())
            currentQrCode = qrCode

            // Check if already used
            if (qrCode.isUsed) {
                showError("QR Code Already Used", "This QR code has already been used for entry.")
                return@getDocumentsWithEqualToConditionsOnce
            }

            // Check if expired
            val now = Timestamp.now()
            if (qrCode.expiresAt != null && qrCode.expiresAt!!.toDate().before(now.toDate())) {
                showError("QR Code Expired", "This QR code has expired and is no longer valid.")
                return@getDocumentsWithEqualToConditionsOnce
            }

            // Valid QR — show visitor details
            lifecycleScope.launch {
                val visitorSnapshot = FirestoreService().getDocument(DatabaseTableName.VISITOR, qrCode.visitorId)
                if (visitorSnapshot != null && visitorSnapshot.exists()) {
                    val visitor = model.VisitorModel.fromHashMap(visitorSnapshot.data ?: emptyMap())
                    currentVisitor = visitor
                    showValidVisitor(qrCode, visitor)
                } else {
                    showError("Visitor Not Found", "Could not find visitor details.")
                }
            }
        }
    }

    private fun showError(title: String, message: String) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Scan Again") { d, _ ->
                d.dismiss()
                isScanning = true
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun showValidVisitor(qrCode: QrCodeModel, visitor: model.VisitorModel) {
        val bottomSheetView = binding.bottomSheet

        val statusBadgeText = bottomSheetView.findViewById<TextView>(R.id.tvVisitorName)
        bottomSheetView.findViewById<TextView>(R.id.tvVisitorName).text = visitor.name
        bottomSheetView.findViewById<TextView>(R.id.tvHouseDetails).text = "House #${visitor.houseNo}"
        bottomSheetView.findViewById<TextView>(R.id.tvVisitorId).text = "ID: ${qrCode.id.takeLast(8).uppercase()}"
        bottomSheetView.findViewById<TextView>(R.id.tvPurpose).text = visitor.purpose

        qrCode.expiresAt?.let {
            val sdf = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
            bottomSheetView.findViewById<TextView>(R.id.tvValidUntil).text = sdf.format(it.toDate())
        }

        val ivVisitorImage = bottomSheetView.findViewById<ShapeableImageView>(R.id.ivVisitorImage)
        ivVisitorImage.setBase64Image(visitor.visitorPhoto)

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun approveVisitorEntry() {
        val qrCode = currentQrCode ?: return
        val visitor = currentVisitor ?: return

        lifecycleScope.launch {
            try {
                // Mark QR code as used
                FirestoreService().updateDocument(
                    DatabaseTableName.QR_CODES,
                    qrCode.id,
                    mapOf("isUsed" to true)
                )

                if (qrCode.visitorId.isNotEmpty()) {
                    FirestoreService().updateDocument(
                        DatabaseTableName.VISITOR,
                        qrCode.visitorId,
                        mapOf(
                            "status" to VisitorStatus.Inside.name,
                            "entryTime" to Timestamp.now(),
                            "securityGuardFcmToken" to SharedPreferencesService.getFcmToken()
                        )
                    )
                }

                Toast.makeText(
                    this@QrCodeScanActivity,
                    "✓ ${visitor.name} checked in successfully!",
                    Toast.LENGTH_LONG
                ).show()

                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

            } catch (e: Exception) {
                Log.e("QrCodeScanActivity", "Error approving visitor", e)
                Toast.makeText(
                    this@QrCodeScanActivity,
                    "Failed to check in visitor. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                isScanning = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
