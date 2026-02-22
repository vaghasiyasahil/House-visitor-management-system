package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityAdminDashboardBinding
import com.yourpackage.services.FirestoreService
import types.UserRole

class AdminDashboardActivity : AppCompatActivity() {

    lateinit var binding: ActivityAdminDashboardBinding
    private var registrations = listOf<com.google.firebase.firestore.ListenerRegistration>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.adminUserName.text = SharedPreferencesService.getUserName();

        binding.houseManagement.setOnClickListener {
            intent = Intent(this,ViewHouseActivity::class.java)
            startActivity(intent)
        }
        binding.guardManagement.setOnClickListener {
            intent = Intent(this,ViewSecurityGuardActivity::class.java)
            startActivity(intent)
        }
        binding.announcements.setOnClickListener {
            intent = Intent(this,ViewAnnouncementActivity::class.java);
            startActivity(intent)
        }
        binding.visitorLogs.setOnClickListener {
            intent = Intent(this,ViewVisitorActivity::class.java)
            startActivity(intent)
        }
        binding.complaints.setOnClickListener {
            intent = Intent(this, ViewComplaintActivity::class.java)
            startActivity(intent)
        }
        binding.blocklist.setOnClickListener {
            intent = Intent(this, ViewBlocklistActivity::class.java)
            startActivity(intent)
        }

        setupDashboardCounts()
    }

    private fun setupDashboardCounts() {
        registrations.forEach { it.remove() }
        registrations = FirestoreService().getDashboardStatistics(UserRole.Admin) { stats ->
            binding.txtTotalHouses.text = (stats["totalHouses"] ?: 0).toString()
            binding.txtTotalGuards.text = (stats["totalGuards"] ?: 0).toString()
            binding.txtTotalAnnouncements.text = (stats["totalAnnouncements"] ?: 0).toString()
            binding.txtTotalVisitors.text = (stats["totalVisitors"] ?: 0).toString()
            binding.txtTotalComplaints.text = (stats["totalComplaints"] ?: 0).toString()
            binding.txtTotalBlockedVisitors.text = (stats["totalBlockedVisitors"] ?: 0).toString()
        }
        setupProfile()
    }

    private fun setupProfile() {
        binding.profileContainer.setOnClickListener {
            showLogoutDialog()
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

    override fun onDestroy() {
        super.onDestroy()
        registrations.forEach { it.remove() }
    }
}