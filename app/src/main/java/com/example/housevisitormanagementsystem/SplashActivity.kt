package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import types.UserRole


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Handler(Looper.getMainLooper()).postDelayed({
            SharedPreferencesService.init(this)
            navigateUser();
        },5000)
    }

    fun navigateUser(){
        if(SharedPreferencesService.getIsLogin()){
            if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.Admin){
                startActivity(Intent(this,AdminDashboardActivity::class.java))
            }else{
                Toast.makeText(this, "Pending...",Toast.LENGTH_LONG).show();
            }
        }else{
            startActivity(Intent(this,LoginActivity::class.java))
        }
        finish();
    }

}