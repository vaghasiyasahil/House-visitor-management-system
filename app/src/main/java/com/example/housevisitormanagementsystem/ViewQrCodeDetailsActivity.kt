package com.example.housevisitormanagementsystem

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityViewQrCodeDetailsBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ViewQrCodeDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewQrCodeDetailsBinding
    private var qrBitmap: Bitmap? = null
    private var visitorName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewQrCodeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleIntentData()
        setupClickListeners()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = getColor(R.color.dark_navy_bg)
            insets
        }
    }

    private fun handleIntentData() {
        val qrData = intent.getStringExtra("QR_DATA") ?: ""
        visitorName = intent.getStringExtra("VISITOR_NAME") ?: "N/A"
        val purpose = intent.getStringExtra("VISITOR_PURPOSE") ?: "N/A"
        val validity = intent.getStringExtra("VISITOR_VALIDITY") ?: "N/A"
        val qrCodeId = intent.getStringExtra("QR_CODE_ID") ?: ""

        binding.apply {
            tvVisitorName.text = visitorName
            tvVisitorPurpose.text = purpose
            tvValidity.text = validity
            tvQrId.text = "ID: ${qrCodeId.takeLast(8).uppercase()}"
        }

        // Generate actual QR code bitmap from qrData
        if (qrData.isNotEmpty()) {
            qrBitmap = generateQrBitmap(qrData, 800)
            qrBitmap?.let {
                binding.ivQrCode.setImageBitmap(it)
                binding.ivQrCode.setPadding(20, 20, 20, 20)
            }
        }
    }

    private fun generateQrBitmap(data: String, size: Int): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = 1
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSave.setOnClickListener {
            saveQrToGallery()
        }

        binding.btnShare.setOnClickListener {
            shareQrCode()
        }
    }

    private fun saveQrToGallery() {
        val bitmap = qrBitmap
        if (bitmap == null) {
            Toast.makeText(this, "No QR code to save", Toast.LENGTH_SHORT).show()
            return
        }

        val filename = "QR_Visitor_${System.currentTimeMillis()}.png"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/VisitorPass")
                }
                val imageUri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                outputStream = imageUri?.let { contentResolver.openOutputStream(it) }
            } else {
                Toast.makeText(
                    this,
                    "Save function requires Android 10+",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                Toast.makeText(this, "QR Code saved to Gallery", Toast.LENGTH_LONG).show()
            } ?: run {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareQrCode() {
        val bitmap = qrBitmap
        if (bitmap == null) {
            Toast.makeText(this, "No QR code to share", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Save bitmap to cache for sharing
            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "visitor_pass_qr.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()

            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Visitor Pass for $visitorName\n" +
                            "Purpose: ${binding.tvVisitorPurpose.text}\n" +
                            "${binding.tvValidity.text}\n\n" +
                            "Please show this QR code at the society entrance."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Visitor Pass via"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to share QR Code", Toast.LENGTH_SHORT).show()
        }
    }
}