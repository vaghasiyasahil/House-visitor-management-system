package com.example.housevisitormanagementsystem

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    lateinit var binding: ActivityAdminDashboardBinding

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
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection(constants.DatabaseTableName.HOUSES).addSnapshotListener { value, _ ->
            binding.txtTotalHouses.text = (value?.size() ?: 0).toString()
        }

        db.collection(constants.DatabaseTableName.SECURITY_GUARDS).addSnapshotListener { value, _ ->
            binding.txtTotalGuards.text = (value?.size() ?: 0).toString()
        }

        db.collection(constants.DatabaseTableName.ANNOUNCEMENTS).addSnapshotListener { value, _ ->
            binding.txtTotalAnnouncements.text = (value?.size() ?: 0).toString()
        }
        
        setupProfile()
    }

    private fun setupProfile() {
        binding.profileContainer.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        SharedPreferencesService.clear()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}