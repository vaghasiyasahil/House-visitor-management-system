package com.example.housevisitormanagementsystem

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityAdminDashboardBinding

class AdminDashboardActivity : AppCompatActivity() {

    lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.houseManagement.setOnClickListener {
            intent = Intent(this,ViewHouseActivity::class.java)
            startActivity(intent)
        }
        binding.guardManagement.setOnClickListener {
            intent = Intent(this,ViewSecurityGuardActivity::class.java)
            startActivity(intent)
        }

    }
}