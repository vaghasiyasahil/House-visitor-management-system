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
import services.SendNotificationService
import services.setBase64Image
import types.VisitorStatus
import utils.TimeAndDateUtils

class HouseOwnerDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHouseOwnerDashboardBinding
    private var pendingVisitors: List<VisitorModel> = emptyList()

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

        getLoginUserHouseNo()
        getPendingRequest()
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
            lifecycleScope.launch {
                SendNotificationService.sendNotification(
                    "title test",
                    "body test",
                    "fJnmSNHWSp6_jYKGypMp-i:APA91bF3tSzsLNWwAUky8dgco9qrosCdWjA1RD_goNDbAI7jhldjgOpSmGVpYer_we5SDcoFRQi5C9SFm0mRLpWMJ22YYwC3elvZmodhFc9stoJq26fmA3I"
//                    "fNioogYCQFmJDGLDwtsXx1:APA91bFV7T-MAKfnPy1V3EEjZeZWl1Ld3E9I9ttsc1xqh3e_4zwSg3YN2KRLdCo7cj3QBO5Q4MZMzywySIqKPwM0pQje3sOoin3B2eybwK1-TgK9Ty5euo8"
                )
            }
//            startActivity(Intent(this,AddQrCodeActivity::class.java))
        }
    }

    private fun getPendingRequest(){
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.VISITOR, mapOf("status" to VisitorStatus.Pending.name,"houseNo" to SharedPreferencesService.getUserHouseNo())) {
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
                        setExpireStatus(visitor.id);
                    }
                }
            }
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


    private fun getLoginUserHouseNo(){
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.VISITOR, mapOf()) {
                snapshot ->
            snapshot?.documents?.forEach {
                val houseModel = HouseModel.fromHashMap(it.data ?: emptyMap());
                SharedPreferencesService.setUserHouseNo(houseModel.houseNo)
            }
            getPendingRequest()
        };
    }
}