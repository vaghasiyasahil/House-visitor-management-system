package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityViewVisitorBinding
import model.VisitorModel
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.google.android.material.imageview.ShapeableImageView
import com.google.api.LogDescriptor
import com.google.firebase.Timestamp
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import services.setBase64Image
import types.VisitorStatus
import utils.TimeAndDateUtils

class ViewVisitorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewVisitorBinding
    private var selectedVisitorFilter: VisitorStatus? = null;
    private var visitorsList: List<VisitorModel> = emptyList()
    private var filterVisitorsList: List<VisitorModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewVisitorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupClickListeners()
        getVisitor()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.chipAll.setOnClickListener {
            selectedVisitorFilter=null
            setupFilters()
        }
        binding.chipPending.setOnClickListener {
            selectedVisitorFilter=VisitorStatus.Pending
            setupFilters()
        }
        binding.chipInside.setOnClickListener {
            selectedVisitorFilter=VisitorStatus.Inside
            setupFilters()
        }
        binding.chipCheckedOut.setOnClickListener {
            selectedVisitorFilter=VisitorStatus.CheckedOut
            setupFilters()
        }
        binding.chipExpired.setOnClickListener {
            selectedVisitorFilter=VisitorStatus.Expired
            setupFilters()
        }
        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
        }
        binding.visitorLogsSearch.addTextChangedListener {
            setupFilters()
        }
    }

    private fun getVisitor(){
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.VISITOR, mapOf()) {
                snapshot ->
            val visitorList = mutableListOf<VisitorModel>()
            snapshot?.documents?.forEach {
                visitorList.add(VisitorModel.fromHashMap(it.data ?: emptyMap()))
            }
            visitorsList = visitorList
            setupFilters()
        };
    }

    private fun setupFilters() {
        val searchText = binding.visitorLogsSearch.text.toString().trim().lowercase()
        filterVisitorsList = visitorsList.filter {
                visitorModel ->
            val statusMatch = selectedVisitorFilter == null || visitorModel.status == selectedVisitorFilter
            val searchMatch =
                searchText.isEmpty() ||
                        visitorModel.name.lowercase().contains(searchText) ||
                        visitorModel.mobile.lowercase().contains(searchText) ||
                        visitorModel.houseNo.lowercase().contains(searchText)
            statusMatch && searchMatch
        }
        binding.visitorEntriesCount.text = "${filterVisitorsList.size} Entries"
        displayFilteredVisitors()
    }

    private fun displayFilteredVisitors() {
        binding.visitorContainer.removeAllViews()
        if (filterVisitorsList.isEmpty()) {
            binding.visitorContainer.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            return
        }
        binding.visitorContainer.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE
        for (visitor in filterVisitorsList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_all_visitor,
                binding.visitorContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.visitorName).text = visitor.name
            itemView.findViewById<TextView>(R.id.visitorStatus).text = visitor.status.name.uppercase()
            itemView.findViewById<TextView>(R.id.visitorStatus).setTextColor(
                ContextCompat.getColor(itemView.context, VisitorStatus.getColor(visitor.status))
            )
            itemView.findViewById<TextView>(R.id.visitorHouseInfo).text = "House #${visitor.houseNo}"
            itemView.findViewById<TextView>(R.id.visitorPurpose).text = visitor.purpose
            itemView.findViewById<TextView>(R.id.visitorCheckIn).text = TimeAndDateUtils.formatShortDateTime(visitor.entryTime)
            itemView.findViewById<TextView>(R.id.visitorCheckOut).text = TimeAndDateUtils.formatShortDateTime(visitor.exitTime)
            itemView.findViewById<ShapeableImageView>(R.id.visitorImage).setBase64Image(visitor.visitorPhoto)
            binding.visitorContainer.addView(itemView)
        }
    }

}