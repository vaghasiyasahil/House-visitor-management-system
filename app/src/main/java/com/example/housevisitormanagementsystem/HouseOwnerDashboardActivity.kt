package com.example.housevisitormanagementsystem

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityHouseOwnerDashboardBinding

class HouseOwnerDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHouseOwnerDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHouseOwnerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.imgProfile.setOnClickListener {
            showLogoutDialog()
        }

        binding.notificationContainer.setOnClickListener {
            Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnVisitorApproval.setOnClickListener {
            startActivity(Intent(this,CheckOutActivity::class.java))
        }

        binding.btnVisitorHistory.setOnClickListener {
            startActivity(Intent(this,ViewVisitorActivity::class.java))
        }

        binding.btnAnnouncements.setOnClickListener {
            startActivity(Intent(this,ViewAnnouncementActivity::class.java))
        }

        binding.btnComplaints.setOnClickListener {
            startActivity(Intent(this,ViewComplaintActivity::class.java))
        }

        binding.btnBlockVisitor.setOnClickListener {
            startActivity(Intent(this,ViewBlocklistActivity::class.java))
        }

        binding.btnViewQRCode.setOnClickListener {
            startActivity(Intent(this,ViewQrCodeActivity::class.java))
        }

        binding.btnGenerateQR.setOnClickListener {
            startActivity(Intent(this,AddQrCodeActivity::class.java))
        }


    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<android.widget.Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.widget.Button>(R.id.btnYes).setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        dialog.show()
    }

    private fun performLogout() {
        SharedPreferencesService.clear()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}