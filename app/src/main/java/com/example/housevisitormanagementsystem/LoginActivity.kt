package com.example.housevisitormanagementsystem

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.housevisitormanagementsystem.databinding.ActivityLoginBinding
import com.example.todoapp.utils.KeyboardUtils
import com.example.todoapp.utils.Validators

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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

        binding.email.editText?.addTextChangedListener {
            binding.email.error = null;
        }

        binding.password.editText?.addTextChangedListener {
            binding.password.error = null;
        }

        binding.login.setOnClickListener {
            KeyboardUtils.hideKeyboard(this);
            binding.email.error=null
            binding.password.error=null
            val emailError = Validators.validateEmail(binding.email.editText?.text.toString().trim())
            val passwordError = Validators.validatePassword(binding.password.editText?.text.toString().trim())
            binding.email.error=emailError
            binding.password.error=passwordError
            if(emailError==null && passwordError==null){
                if(binding.switchAdmin.isChecked){
                    val intent = Intent(this,AdminDashboardActivity::class.java);
                    startActivity(intent)
                    finish()
                }
            }
        }

        binding.switchAdmin.setOnClickListener {
            binding.switchAdmin.isChecked=true;
            binding.switchSecurity.isChecked=false
            binding.switchOwner.isChecked=false;
        }
        binding.switchSecurity.setOnClickListener {
            binding.switchAdmin.isChecked=false;
            binding.switchSecurity.isChecked=true
            binding.switchOwner.isChecked=false;
        }
        binding.switchOwner.setOnClickListener {
            binding.switchAdmin.isChecked=false;
            binding.switchSecurity.isChecked=false
            binding.switchOwner.isChecked=true;
        }

    }
}