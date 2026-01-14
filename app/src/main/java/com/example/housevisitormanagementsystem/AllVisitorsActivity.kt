package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.housevisitormanagementsystem.databinding.ActivityAllVisitorsBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import model.VisitorModel
import services.setBase64Image
import types.VisitorStatus
import utils.TimeAndDateUtils

class AllVisitorsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllVisitorsBinding
    private val visitorList = mutableListOf<VisitorModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllVisitorsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        setupClickListeners()
        getAllVisitor()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun getAllVisitor(){
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.VISITOR, mapOf()) {
                snapshot ->
            visitorList.clear()
            snapshot?.documents?.forEach {
                visitorList.add(VisitorModel.fromHashMap(it.data ?: emptyMap()))
            }
            runOnUiThread {
                if (visitorList.isEmpty()) {
                    binding.llVisitorList.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                } else {
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.llVisitorList.visibility = View.VISIBLE
                    displayVisitors()
                }
            }
        };
    }

    private fun displayVisitors() {
        binding.llVisitorList.removeAllViews()
        for (visitor in visitorList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_all_visitor,
                binding.llVisitorList,
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
            binding.llVisitorList.addView(itemView)
        }
    }
}
