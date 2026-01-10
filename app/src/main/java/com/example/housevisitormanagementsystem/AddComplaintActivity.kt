package com.example.housevisitormanagementsystem

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityAddComplaintBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Validators

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
            finish()
        }
    }

}