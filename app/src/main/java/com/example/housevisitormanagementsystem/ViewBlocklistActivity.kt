package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityViewBlocklistBinding
import com.google.android.material.imageview.ShapeableImageView
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.VisitorModel
import services.setBase64Image
import types.TempBlocklistType
import types.UserRole

class ViewBlocklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewBlocklistBinding
    private var isHistoryTab = false
    private var selectedTempBlockFilter: TempBlocklistType = TempBlocklistType.Blocked;
    private var visitorsList: List<VisitorModel> = emptyList()
    private var filterVisitorsList: List<VisitorModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBlocklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0
        getVisitor();
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnGenerateReport.setOnClickListener {
            Toast.makeText(this, "Exporting to PDF...", Toast.LENGTH_SHORT).show()
        }

        binding.etSearch.addTextChangedListener {
            setupFilters()
        }

        binding.tabBlocked.setOnClickListener {
            if (isHistoryTab) {
                isHistoryTab = false
                updateTabUI()
                selectedTempBlockFilter=TempBlocklistType.Blocked
                setupFilters()
            }
        }

        binding.tabHistory.setOnClickListener {
            if (!isHistoryTab) {
                isHistoryTab = true
                updateTabUI()
                selectedTempBlockFilter=TempBlocklistType.History
                setupFilters()
            }
        }
    }

    private fun getVisitor(){
        val filter: Map<String, Any>
        if(UserRole.fromString(SharedPreferencesService.getUserRole())== UserRole.HouseOwner){
            filter = mapOf("houseNo" to SharedPreferencesService.getUserHouseNo())
        }else{
            filter = mapOf()
        }
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.VISITOR, filter) {
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
        val searchText = binding.etSearch.text.toString().trim().lowercase()
        Log.d("","search Text = ${searchText}")
        filterVisitorsList = visitorsList.filter {
                visitorModel ->
            var isBlocked = false;
            if(selectedTempBlockFilter == TempBlocklistType.Blocked){
               isBlocked = true;
            }else{
                isBlocked=false;
            }
            val statusMatch = visitorModel.isBlocked == isBlocked

            val searchMatch =
                searchText.isEmpty() ||
                        visitorModel.name.lowercase().contains(searchText) ||
                        visitorModel.mobile.lowercase().contains(searchText) ||
                        visitorModel.houseNo.lowercase().contains(searchText)

            statusMatch && searchMatch
        }
        displayFilteredVisitors()
    }

    private fun displayFilteredVisitors() {
        binding.tempBlockContainer.removeAllViews()
        if (filterVisitorsList.isEmpty()) {
            binding.tempBlockContainer.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            return
        }
        binding.tempBlockContainer.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE
        for (visitor in filterVisitorsList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_blocked_visitor,
                binding.tempBlockContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.visitorName).text = visitor.name
            itemView.findViewById<TextView>(R.id.visitorMobile).text = visitor.mobile
            itemView.findViewById<TextView>(R.id.houseNo).text = "House #${visitor.houseNo}"
            if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.HouseOwner){
                itemView.findViewById<TextView>(R.id.houseNo).visibility = View.GONE
            }
            if(visitor.isBlocked){
                itemView.findViewById<TextView>(R.id.btnAction).text = "Unblock Visitor"
            }else{
                itemView.findViewById<TextView>(R.id.btnAction).text = "Block Visitor"
            }
            itemView.findViewById<TextView>(R.id.btnAction).setOnClickListener {
                lifecycleScope.launch {
                    FirestoreService().updateDocument(DatabaseTableName.VISITOR,visitor.id, mapOf("isBlocked" to !visitor.isBlocked));
                }
            }
            itemView.findViewById<ShapeableImageView>(R.id.visitorImage).setBase64Image(visitor.visitorPhoto)
            binding.tempBlockContainer.addView(itemView)
        }
    }

    private fun updateTabUI() {
        if (isHistoryTab) {
            binding.tabBlocked.setBackgroundResource(android.R.color.transparent)
            binding.tabBlocked.setTextColor(ContextCompat.getColor(this, R.color.text_grey))
            binding.tabHistory.setBackgroundResource(R.drawable.bg_chip_selected)
            binding.tabHistory.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tvSectionTitle.text = "Block History"
        } else {
            binding.tabHistory.setBackgroundResource(android.R.color.transparent)
            binding.tabHistory.setTextColor(ContextCompat.getColor(this, R.color.text_grey))
            binding.tabBlocked.setBackgroundResource(R.drawable.bg_chip_selected)
            binding.tabBlocked.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tvSectionTitle.text = "Currently Blocked"
        }
    }
}