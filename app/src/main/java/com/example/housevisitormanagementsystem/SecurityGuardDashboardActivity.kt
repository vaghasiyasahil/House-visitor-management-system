package com.example.housevisitormanagementsystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivitySecurityGuardDashboardBinding
import com.google.firebase.Timestamp
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import model.VisitorModel
import types.VisitorStatus
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.android.material.imageview.ShapeableImageView
import services.setBase64Image

class SecurityGuardDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityGuardDashboardBinding
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var pendingVisitors: List<VisitorModel> = emptyList()

    private val timeUpdater = object : Runnable {
        override fun run() {
            displayPendingVisitors(pendingVisitors)
            handler.postDelayed(this, 60_000) // 1 minute
        }
    }

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
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.VISITOR, mapOf("status" to VisitorStatus.Pending.name)) {
                snapshot ->
            val visitorList = mutableListOf<VisitorModel>()
            val now = Timestamp.now()
            val tenMinutesInMillis = 10 * 60 * 1000

            snapshot?.documents?.forEach {
                val visitor = VisitorModel.fromHashMap(it.data ?: emptyMap())
                if (visitor.entryTime != null) {
                    val diffMillis = now.toDate().time - visitor.entryTime!!.toDate().time
                    if (diffMillis in 0..tenMinutesInMillis) {
                        visitorList.add(visitor)
                    }
                }
            }
            pendingVisitors = visitorList
            displayPendingVisitors(visitorList)
        };
    }

    private fun displayPendingVisitors(visitorList: List<VisitorModel>) {
        binding.llRecentActivityContainer.removeAllViews()
        val nowMillis = System.currentTimeMillis()
        for (visitor in visitorList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_pending_visitor,
                binding.llRecentActivityContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.visitorName).text = visitor.name
            itemView.findViewById<TextView>(R.id.visitorDetails).text = "Checked in to Unit ${visitor.houseNo}"
            itemView.findViewById<TextView>(R.id.visitorStatus).text = visitor.status.name.uppercase()
            itemView.findViewById<ShapeableImageView>(R.id.visitorImage).setBase64Image(visitor.visitorPhoto)
            val timeAgoText = visitor.entryTime?.let {
                val diffMillis = nowMillis - it.toDate().time
                getTimeAgo(diffMillis)
            } ?: "just now"
            itemView.findViewById<TextView>(R.id.visitorTimeAgo).text = timeAgoText

            binding.llRecentActivityContainer.addView(itemView)
        }
    }

    private fun getTimeAgo(diffMillis: Long): String {
        val seconds = diffMillis / 1000
        val minutes = seconds / 60

        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            else -> "Just Now"
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

    override fun onStart() {
        super.onStart()
        handler.post(timeUpdater)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(timeUpdater)
    }

}