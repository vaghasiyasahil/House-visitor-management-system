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
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch
import services.SendNotificationService
import services.setBase64Image
import utils.TimeAndDateUtils

class SecurityGuardDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityGuardDashboardBinding
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var pendingVisitors: List<VisitorModel> = emptyList()

    private val timeUpdater = object : Runnable {
        override fun run() {
            getPendingRequest()
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
        binding.tvViewAllActivity.setOnClickListener {
            startActivity(Intent(this, AllVisitorsActivity::class.java))
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
                    }else{
                        setExpireStatus(visitor);
                    }
                }
            }
            pendingVisitors = visitorList
            displayPendingVisitors(visitorList)
        };
    }

    private fun setExpireStatus(visitor:VisitorModel){
        lifecycleScope.launch {
            FirestoreService().updateDocument(DatabaseTableName.VISITOR,visitor.id, mapOf("status" to VisitorStatus.Expired.name));
        }
    }

    private fun displayPendingVisitors(visitorList: List<VisitorModel>) {
        binding.llRecentActivityContainer.removeAllViews()
        if (visitorList.isEmpty()) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.llRecentActivityContainer.visibility = View.GONE
            return
        }
        binding.llEmptyState.visibility = View.GONE
        binding.llRecentActivityContainer.visibility = View.VISIBLE
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
            itemView.findViewById<TextView>(R.id.visitorStatus).setTextColor(ContextCompat.getColor(itemView.context, VisitorStatus.getColor(visitor.status)))
            itemView.findViewById<ShapeableImageView>(R.id.visitorImage).setBase64Image(visitor.visitorPhoto)
            val timeAgoText = visitor.entryTime?.let {
                val diffMillis = nowMillis - it.toDate().time
                TimeAndDateUtils.getTimeAgo(diffMillis)
            } ?: "just now"
            itemView.findViewById<TextView>(R.id.visitorTimeAgo).text = timeAgoText

            binding.llRecentActivityContainer.addView(itemView)
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