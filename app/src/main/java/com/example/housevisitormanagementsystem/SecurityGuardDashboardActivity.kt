package com.example.housevisitormanagementsystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivitySecurityGuardDashboardBinding
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import model.VisitorModel

class SecurityGuardDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityGuardDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityGuardDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCheckIn.setOnClickListener {
            startActivity(Intent(this,CheckInActivity::class.java))
        }
        binding.btnCheckOut.setOnClickListener {
            startActivity(Intent(this,CheckOutActivity::class.java))
        }
        binding.btnScanQR.setOnClickListener {
            startActivity(Intent(this, QrCodeScanActivity::class.java))
        }
        binding.btnAnnouncements.setOnClickListener {
            startActivity(Intent(this, ViewAnnouncementActivity::class.java))
        }
        binding.ivProfile.setOnClickListener {
            showLogoutDialog()
        }
        getPendingRequest()
    }

    private fun getPendingRequest(){
        FirestoreService().getAllDocuments(DatabaseTableName.VISITOR) {
                snapshot ->
            val visitorList = mutableListOf<VisitorModel>()

            snapshot?.documents?.forEach {
                visitorList.add(
                    VisitorModel.fromHashMap(it.data ?: emptyMap())
                )
            }

            visitorList.forEach {
                Log.d("TAG", "Visitor => $it")
            }
        };
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