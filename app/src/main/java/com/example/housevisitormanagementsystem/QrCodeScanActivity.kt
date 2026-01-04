package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityQrCodeScanBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class QrCodeScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrCodeScanBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.decorView.systemUiVisibility = 0

        setupBottomSheet()
        setupClickListeners()

        // Simulate a successful scan after 2 seconds for UI demonstration
        binding.root.postDelayed({
            showScanResult()
        }, 2000)
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    // Resume camera if it was paused
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        
        // Bottom Sheet Buttons
        val bottomSheetView = binding.bottomSheet
        bottomSheetView.findViewById<View>(R.id.btnInstantApproval).setOnClickListener {
            Toast.makeText(this, "Visitor Approved!", Toast.LENGTH_SHORT).show()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetView.findViewById<View>(R.id.btnDenyEntry).setOnClickListener {
            Toast.makeText(this, "Entry Denied", Toast.LENGTH_SHORT).show()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun showScanResult() {
        // In a real scenario, you'd bind the data here from the QR result
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}
