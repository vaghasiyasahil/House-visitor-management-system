package com.example.housevisitormanagementsystem


import SharedPreferencesService
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.housevisitormanagementsystem.databinding.ActivityLoginBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Validators
import com.google.firebase.firestore.FirebaseFirestore
import types.UserRole

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private val db = FirebaseFirestore.getInstance()

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
            KeyboardUtils.hideKeyboard(this)
        }

        binding.email.editText?.addTextChangedListener {
            binding.email.error = null
        }

        binding.password.editText?.addTextChangedListener {
            binding.password.error = null
        }

        binding.login.setOnClickListener {
            KeyboardUtils.hideKeyboard(this);
            binding.email.error=null
            binding.password.error=null
            val email = binding.email.editText?.text.toString().trim();
            val password = binding.password.editText?.text.toString().trim();
            val emailError = Validators.validateEmail(email)
            val passwordError = Validators.validatePassword(password)
            binding.email.error=emailError
            binding.password.error=passwordError
            if(emailError==null && passwordError==null){
                performLogin(email,password);
            }
        }
    }

    private fun performLogin(email: String, value: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.login.isEnabled = false

        db.collection("user")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                binding.login.isEnabled = true
                
                if (documents.isEmpty) {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    val dbPassword = document.getString("password")
                    val role = document.getString("role")

                    if (dbPassword == value) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        SharedPreferencesService.setIsLogin(true);
                        if (role == "admin") {
                            SharedPreferencesService.setUserRole(UserRole.Admin.name)
                            startActivity(Intent(this, AdminDashboardActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Access Denied: Admin role required", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.login.isEnabled = true
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}