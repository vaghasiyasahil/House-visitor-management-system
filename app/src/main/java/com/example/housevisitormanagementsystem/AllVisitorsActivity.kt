package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
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
            snapshot?.documents?.forEach {
                visitorList.add(VisitorModel.fromHashMap(it.data ?: emptyMap()))
                runOnUiThread {
                    if (visitorList.isEmpty()) {
                        binding.rvAllVisitors.visibility = View.GONE
                        binding.emptyStateLayout.visibility = View.VISIBLE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.rvAllVisitors.visibility = View.VISIBLE
                        displayVisitors()
                    }
                }
            }
        };
    }
    private fun displayVisitors() {
        binding.rvAllVisitors.removeAllViews()
        for (visitor in visitorList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_all_visitor,
                binding.rvAllVisitors,
                false
            )
            itemView.findViewById<TextView>(R.id.visitorName).text = visitor.name
            itemView.findViewById<TextView>(R.id.visitorStatus).text = visitor.status.name.uppercase()
            itemView.findViewById<ShapeableImageView>(R.id.visitorImage).setBase64Image(visitor.visitorPhoto)

            binding.rvAllVisitors.addView(itemView)
        }
    }
}
