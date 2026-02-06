package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityHouseOwnerDashboardBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.HouseModel
import model.VisitorModel
import services.setBase64Image
import types.VisitorStatus
import utils.TimeAndDateUtils

class HouseOwnerDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHouseOwnerDashboardBinding
    private var pendingVisitors: List<VisitorModel> = emptyList()
    private var isInitialLoad = true
    private val processedVisitorIds = mutableSetOf<String>()

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

        checkNotificationPermission()
        getLoginUserHouseNo()
        setupClickListeners()
    }

    private fun checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(
                    this, 
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 
                    101
                )
            }
        }
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

    private fun getPendingRequest(){
        val currentHouseNo = SharedPreferencesService.getUserHouseNo()
        Log.d("NotificationDebug", "Starting getPendingRequest with HouseNo: $currentHouseNo")
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.VISITOR, mapOf("status" to VisitorStatus.Pending.name,"houseNo" to currentHouseNo)) {
                snapshot ->
            val visitorList = mutableListOf<VisitorModel>()
            val now = Timestamp.now()
            val tenMinutesInMillis = 10 * 60 * 1000

            snapshot?.documents?.forEach { doc ->
                val visitor = VisitorModel.fromHashMap(doc.data ?: emptyMap())
                Log.d("NotificationDebug", "Checking visitor: ${visitor.name}, ID: ${visitor.id}, status: ${visitor.status}")
                if (visitor.entryTime != null) {
                    val diffMillis = now.toDate().time - visitor.entryTime!!.toDate().time
                    if (diffMillis in 0..tenMinutesInMillis) {
                        visitorList.add(visitor)
                        
                        Log.d("NotificationDebug", "New visitor in time window. isInitialLoad: $isInitialLoad, alreadyProcessed: ${processedVisitorIds.contains(visitor.id)}")
                        // Notify if it's a new visitor after the initial load
                        if (!isInitialLoad && !processedVisitorIds.contains(visitor.id)) {
                            Log.d("NotificationDebug", "Triggering local notification for ${visitor.name}")
                            services.NotificationService.sendLocalNotification(
                                this,
                                "New Visitor Arrival",
                                "${visitor.name} has arrived for Unit ${visitor.houseNo} - Name: ${visitor.name} - Purpose: ${visitor.purpose}"
                            )
                        }
                        processedVisitorIds.add(visitor.id)
                    } else {
                        setExpireStatus(visitor.id)
                    }
                }
            }
            isInitialLoad = false
            pendingVisitors = visitorList
            displayPendingVisitors(visitorList)
        };
    }

    private fun setExpireStatus(docId:String){
        lifecycleScope.launch {
            FirestoreService().updateDocument(DatabaseTableName.VISITOR,docId, mapOf("status" to VisitorStatus.Expired.name));
        }
    }

    private fun setInsideStatus(docId:String){
        lifecycleScope.launch {
            FirestoreService().updateDocument(DatabaseTableName.VISITOR,docId, mapOf("status" to VisitorStatus.Inside.name));
        }
    }

    private fun setRejectStatus(docId:String){
        lifecycleScope.launch {
            FirestoreService().updateDocument(DatabaseTableName.VISITOR,docId, mapOf("status" to VisitorStatus.Reject.name));
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
            itemView.findViewById<TextView>(R.id.visitorStatus).setOnClickListener {
                setInsideStatus(visitor.id)
            }
            itemView.findViewById<LinearLayout>(R.id.itemPendingVisitor).setOnClickListener {
                showVisitorRequestDialog(visitor)
            }
            binding.llRecentActivityContainer.addView(itemView)
        }
    }

    private fun showVisitorRequestDialog(visitor: VisitorModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_visitor_request, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.imgVisitor).setBase64Image(visitor.visitorPhoto)
        dialogView.findViewById<TextView>(R.id.tvVisitorName).text = visitor.name
        dialogView.findViewById<TextView>(R.id.tvPurpose).text = visitor.purpose
        
        visitor.entryTime?.let {
            val date = it.toDate()
            val format = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())
            dialogView.findViewById<TextView>(R.id.tvTime).text = format.format(date)
        } ?: run {
             dialogView.findViewById<TextView>(R.id.tvTime).text = "N/A"
        }


        dialogView.findViewById<View>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReject).setOnClickListener {
            setRejectStatus(visitor.id)
            dialog.dismiss()
        }

        dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnApprove).setOnClickListener {
            setInsideStatus(visitor.id)
            dialog.dismiss()
        }

        dialog.show()
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

    private fun getLoginUserHouseNo() {
        val userEmail = SharedPreferencesService.getUserEmail()
        if (userEmail.isEmpty()) return

        Log.d("NotificationDebug", "Fetching house no for email: $userEmail")
        FirestoreService().getDocumentsWithEqualToConditionsOnce(
            DatabaseTableName.HOUSES, 
            mapOf("ownerEmail" to userEmail)
        ) { snapshot ->
            val document = snapshot?.documents?.firstOrNull()
            Log.d("NotificationDebug", "House doc found: ${document?.id}")
            if (document != null) {
                val houseNo = document.getString("houseNo")
                Log.d("NotificationDebug", "House No retrieved: $houseNo")
                SharedPreferencesService.setUserHouseNo(houseNo ?: "")
                getPendingRequest()
            } else {
                Log.d("NotificationDebug", "No house found for $userEmail")
                Toast.makeText(this, "House details not found for $userEmail", Toast.LENGTH_SHORT).show()
            }
        }
    }
}