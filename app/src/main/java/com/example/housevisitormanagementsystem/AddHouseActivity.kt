package com.example.housevisitormanagementsystem

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.housevisitormanagementsystem.databinding.ActivityAddHouseBinding
import com.example.todoapp.utils.KeyboardUtils
import com.example.todoapp.utils.Validators

class AddHouseActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddHouseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddHouseBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this);
        }

        binding.btnBack.setOnClickListener {
            finish();
        }

        binding.btnCancel.setOnClickListener {
            binding.etHouseNumber.editText?.text?.clear();
            binding.etOwnerName.editText?.text?.clear();
            binding.etOwnerEmail.editText?.text?.clear();
            binding.etPassword.editText?.text?.clear();
        }

        binding.btnAddHouse.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
            binding.etHouseNumber.error = Validators.validateRequired(binding.etHouseNumber.editText.toString(),"House number")
            binding.etOwnerName.error = Validators.validateName(binding.etOwnerName.editText.toString(),"Owner name")
            binding.etOwnerEmail.error = Validators.validateEmail(binding.etOwnerEmail.editText.toString())
            binding.etPassword.error = Validators.validatePassword(binding.etPassword.editText.toString())
        }

        binding.etHouseNumber.editText?.addTextChangedListener {
            binding.etHouseNumber.error = null;
        }
        binding.etOwnerName.editText?.addTextChangedListener {
            binding.etOwnerName.error = null;
        }
        binding.etOwnerEmail.editText?.addTextChangedListener {
            binding.etOwnerEmail.error = null;
        }
        binding.etPassword.editText?.addTextChangedListener {
            binding.etPassword.error = null;
        }

    }
}