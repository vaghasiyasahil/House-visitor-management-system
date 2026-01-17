package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.view.View
import android.util.Base64
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityCheckOutBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Timestamp
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.VisitorModel
import services.setBase64Image
import types.VisitorStatus
import utils.TimeAndDateUtils

class CheckOutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckOutBinding
    private val activeVisitors = mutableListOf<VisitorModel>()
    private var insideVisitors: List<VisitorModel> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        window.statusBarColor = ContextCompat.getColor(this, R.color.dark_navy_bg)
        window.decorView.systemUiVisibility = 0
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        getInsideRequest()
    }

    private fun getInsideRequest(){
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.VISITOR, mapOf("status" to VisitorStatus.Inside.name)) {
                snapshot ->
            val visitorList = mutableListOf<VisitorModel>()
            snapshot?.documents?.forEach {
                visitorList.add(VisitorModel.fromHashMap(it.data ?: emptyMap()))
            }
            insideVisitors = visitorList
            displayInsideVisitors()
            Log.d("","pendingVisitors size = ${insideVisitors.size}");
        };
    }

    private fun displayInsideVisitors() {
        binding.visitorContainer.removeAllViews()
        for (visitor in insideVisitors) {
            val itemView = layoutInflater.inflate(
                R.layout.item_visitor_active,
                binding.visitorContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.visitorName).text = visitor.name
            itemView.findViewById<TextView>(R.id.visitorPhone).text = visitor.mobile
            itemView.findViewById<TextView>(R.id.visitorHouseNo).text = visitor.houseNo
            itemView.findViewById<TextView>(R.id.visitorStatus).text = visitor.status.name
            itemView.findViewById<TextView>(R.id.visitorStatus).setTextColor(
                ContextCompat.getColor(itemView.context, VisitorStatus.getColor(visitor.status))
            )
            itemView.findViewById<TextView>(R.id.visitorEntryTime).text = TimeAndDateUtils.formatShortDateTime(visitor.entryTime)
            itemView.findViewById<TextView>(R.id.btnCheckOut).setOnClickListener {
                setCheckedOutStatus(visitor.id)
            }

            binding.visitorContainer.addView(itemView)
        }
    }

    private fun setCheckedOutStatus(docId:String){
        lifecycleScope.launch {
            FirestoreService().updateDocument(DatabaseTableName.VISITOR,docId, mapOf("status" to VisitorStatus.CheckedOut.name,"exitTime" to Timestamp.now()));
        }
    }


}