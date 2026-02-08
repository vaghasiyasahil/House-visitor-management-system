package com.example.housevisitormanagementsystem

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import com.example.housevisitormanagementsystem.databinding.ActivityViewComplaintBinding
import com.google.android.material.card.MaterialCardView
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import model.ComplaintModel
import types.AnnouncementsType
import types.ComplaintStatusType
import utils.TimeAndDateUtils

class ViewComplaintActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewComplaintBinding
    private var allComplaintList: List<ComplaintModel> = emptyList()
    private var filterComplaintList: List<ComplaintModel> = emptyList()
    private  var selectedComplaintStatusType : ComplaintStatusType?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewComplaintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupClickListeners()
        getAllComplaint()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnGenerateReport.setOnClickListener {
            android.widget.Toast.makeText(this, "Exporting to PDF...", android.widget.Toast.LENGTH_SHORT).show()
        }
        binding.fabAddComplaint.setOnClickListener {
            startActivity(Intent(this,AddComplaintActivity::class.java))
        }
        binding.chipPending.setOnClickListener {
            selectedComplaintStatusType= ComplaintStatusType.Pending;
            setupFilters()
        }
        binding.chipInProgress.setOnClickListener {
            selectedComplaintStatusType=ComplaintStatusType.InProgress;
            setupFilters()
        }
        binding.chipResolved.setOnClickListener {
            selectedComplaintStatusType=ComplaintStatusType.Resolved;
            setupFilters()
        }
        binding.chipAll.setOnClickListener {
            selectedComplaintStatusType=null;
            setupFilters()
        }
        binding.etSearch.addTextChangedListener {
            setupFilters()
        }
    }

    private fun getAllComplaint(){
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.COMPLAINT, mapOf()) {
                snapshot ->
            val announcementsList = mutableListOf<ComplaintModel>()
            snapshot?.documents?.forEach {
                val model = ComplaintModel.fromHashMap(it.data ?: emptyMap())
                if (model.title.isNotEmpty() || model.description.isNotEmpty()) {
                    announcementsList.add(model)
                }
            }
            allComplaintList = announcementsList
            setupFilters()
        };
    }

    private fun setupFilters() {
        val searchText = binding.etSearch.text.toString().trim().lowercase()
        filterComplaintList = allComplaintList.filter {
                announcementModel ->
            val statusMatch = selectedComplaintStatusType == null || announcementModel.status == selectedComplaintStatusType

            val searchMatch =
                searchText.isEmpty() ||
                        announcementModel.title.lowercase().contains(searchText) ||
                        announcementModel.description.lowercase().contains(searchText)

            statusMatch && searchMatch
        }
        displayFilteredComplaint()
        Log.d("","filterComplaintList = ${filterComplaintList.size}")
    }

    private fun displayFilteredComplaint() {
        binding.complaintsContainer.removeAllViews()
        if (filterComplaintList.isEmpty()) {
            binding.complaintsContainer.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            return
        }
        binding.complaintsContainer.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE
        for (complaint in filterComplaintList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_complaint,
                binding.complaintsContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.complaintItemTitle).text = complaint.title
            itemView.findViewById<TextView>(R.id.complaintItemTime).text = TimeAndDateUtils.formatShortDateTime(complaint.createdAt)
            itemView.findViewById<TextView>(R.id.complaintItemDescription).text = complaint.description
            itemView.findViewById<ConstraintLayout>(R.id.complaintItemMain).setOnClickListener {
                val intent = Intent(this, UpdateComplaintStatusActivity::class.java)
                intent.putExtra("complaint", complaint)
                startActivity(intent)
            }
            itemView.findViewById<TextView>(R.id.complaintItemStatus).text = complaint.status.name
            itemView.findViewById<TextView>(R.id.complaintItemStatus).setTextColor(ContextCompat.getColor(this, ComplaintStatusType.getColor(complaint.status)))

            binding.complaintsContainer.addView(itemView)
        }
    }

}