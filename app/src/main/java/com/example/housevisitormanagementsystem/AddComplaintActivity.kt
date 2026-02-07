package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityAddComplaintBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Loader
import com.example.housevisitormanagementsystem.utils.Validators
import com.google.firebase.Timestamp
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.AnnouncementModel
import model.ComplaintModel
import types.AnnouncementsType
import types.ComplaintStatusType
import types.UserRole

class AddComplaintActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddComplaintBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddComplaintBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnSubmitComplaint.setOnClickListener {
            submitComplaint()
        }

        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this);
        }
    }

    fun submitComplaint(){
        val title = binding.title.text.toString();
        val des = binding.description.text.toString();
        val titleError = Validators.validateRequired(title, "Title")
        val desError = Validators.validateRequired(des, "Description")
        binding.title.error = titleError
        binding.description.error = desError

        if(titleError==null && desError==null){
            val complaint = ComplaintModel(
                title = title,
                description = des,
                createdHouseNo = SharedPreferencesService.getUserHouseNo(),
                createdAt = Timestamp.now(),
            )
            lifecycleScope.launch {
                try{
                    Loader.showLoader(this@AddComplaintActivity);
                    FirestoreService().addDocument(DatabaseTableName.COMPLAINT,complaint.toHashMap());
                    Toast.makeText(this@AddComplaintActivity, "Complaint Sent Successfully", Toast.LENGTH_LONG).show()
                    finish()
                }catch (e:Exception){
                    Log.d("Sent Successfully","Fail To Sent Complaint = $e")
                    Toast.makeText(this@AddComplaintActivity, "Fail To Sent Complaint", Toast.LENGTH_SHORT).show()
                }finally {
                    Loader.showLoader(this@AddComplaintActivity);
                }
            }
        }
    }

}