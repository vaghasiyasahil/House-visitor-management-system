package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityUpdateComplaintStatusBinding
import com.example.housevisitormanagementsystem.utils.Loader
import com.google.firebase.Timestamp
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.ComplaintModel
import types.ComplaintStatusType
import types.UserRole

class UpdateComplaintStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateComplaintStatusBinding
    private var selectedStatus: String = "Pending"
    private var complaintModel:ComplaintModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateComplaintStatusBinding.inflate(layoutInflater)
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

        complaintModel = intent.getParcelableExtra<ComplaintModel>("complaint")
        if (complaintModel == null) {
            finish()
            return
        }

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        binding.tvComplaintTitle.text = "${complaintModel?.title?:""}"
        binding.tvComplaintHouseNo.text = "House No • ${complaintModel?.createdHouseNo?:""}"
        binding.tvComplaintDescription.text = complaintModel?.description?:""
        selectedStatus = ComplaintStatusType.toDisplayString(complaintModel?.status?:ComplaintStatusType.Pending)?:"Peinding"
        updateOptionSelection()
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.optionPending.setOnClickListener {
            selectedStatus = "Pending"
            updateOptionSelection()
        }

        binding.optionInProgress.setOnClickListener {
            selectedStatus = "In Progress"
            updateOptionSelection()
        }

        binding.optionResolved.setOnClickListener {
            selectedStatus = "Resolved"
            updateOptionSelection()
        }

        binding.btnUpdateStatus.setOnClickListener {
            updateStatus();
        }
    }

    private fun updateOptionSelection() {
        binding.rbPending.isChecked = selectedStatus == "Pending"
        binding.optionPending.isSelected = selectedStatus == "Pending"

        binding.rbInProgress.isChecked = selectedStatus == "In Progress"
        binding.optionInProgress.isSelected = selectedStatus == "In Progress"

        binding.rbResolved.isChecked = selectedStatus == "Resolved"
        binding.optionResolved.isSelected = selectedStatus == "Resolved"
    }

    private fun updateStatus(){
        if(complaintModel==null){
            finish();
        }
        lifecycleScope.launch {
            try{
                Loader.showLoader(this@UpdateComplaintStatusActivity);
                val updateStatus = ComplaintStatusType.fromString(selectedStatus)
                complaintModel = complaintModel?.copy(status = updateStatus?:ComplaintStatusType.Pending)
                val success = FirestoreService().updateDocument(DatabaseTableName.COMPLAINT, complaintModel?.id?:"", complaintModel!!.toHashMap())
                if (success) {
                    Toast.makeText(this@UpdateComplaintStatusActivity, "Status updated to ${updateStatus?.name}", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@UpdateComplaintStatusActivity, "Fail To Update Status", Toast.LENGTH_SHORT).show()
                }
            }catch (e:Exception){
                Log.d("","update complaint status = $e")
                Toast.makeText(this@UpdateComplaintStatusActivity,"Fail To Update Status",Toast.LENGTH_SHORT).show();
            }finally {
                Loader.closeLoader();
            }
        }

    }

}