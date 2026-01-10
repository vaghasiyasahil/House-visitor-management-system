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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityViewQrCodeDetailsBinding
import java.io.OutputStream

class ViewQrCodeDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewQrCodeDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewQrCodeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        handleIntentData()
        setupClickListeners()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = getColor(R.color.dark_navy_bg)
            insets
        }
    }

    private fun setupUI() {
        // Any additional UI setup
    }

    private fun handleIntentData() {
        val name = intent.getStringExtra("VISITOR_NAME") ?: "N/A"
        val purpose = intent.getStringExtra("VISITOR_PURPOSE") ?: "N/A"
        val validity = intent.getStringExtra("VISITOR_VALIDITY") ?: "N/A"

        binding.apply {
            tvVisitorName.text = name.replace("Guest: ", "")
            tvVisitorPurpose.text = purpose
            tvValidity.text = validity
            tvQrId.text = "ID: PASS-${System.currentTimeMillis().toString().takeLast(6)}"
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
            shareQrDetails()
        }
    }

    private fun saveQrToGallery() {
        val bitmap = getBitmapFromView(binding.ivQrCode)
        val filename = "QR_Visitor_${System.currentTimeMillis()}.jpg"
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/VisitorPass")
                }
                val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                outputStream = imageUri?.let { contentResolver.openOutputStream(it) }
            } else {
                // For older versions, would need write permission, skipping for now or showing toast
                Toast.makeText(this, "Save function requires additional permissions on older Android", Toast.LENGTH_SHORT).show()
                return
            }

            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                Toast.makeText(this, "QR Code saved to Gallery", Toast.LENGTH_LONG).show()
            } ?: run {
                Toast.makeText(this, "Failed to create image file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save QR Code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareQrDetails() {
        val visitorName = binding.tvVisitorName.text.toString()
        val shareText = "Visitor Pass for $visitorName\n" +
                "Purpose: ${binding.tvVisitorPurpose.text}\n" +
                "Validity: ${binding.tvValidity.text}\n" +
                "Please show this QR code at the entrance."

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Visitor Pass - $visitorName")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, "Share Visitor Pass via"))
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }
}