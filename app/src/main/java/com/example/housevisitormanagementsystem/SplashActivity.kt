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
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import services.NotificationService
import types.UserRole


class SplashActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        startApp()
    }

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

        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startApp()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startApp()
        }
    }

    private fun startApp() {
        Handler(Looper.getMainLooper()).postDelayed({
            SharedPreferencesService.init(this)
            NotificationService().init();
            navigateUser();
        }, 2000)
    }

    fun navigateUser(){
        if(SharedPreferencesService.getIsLogin()){
            if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.Admin){
                startActivity(Intent(this,AdminDashboardActivity::class.java))
            }else if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.SecurityGuard){
                startActivity(Intent(this,SecurityGuardDashboardActivity::class.java))
            }else{
                startActivity(Intent(this,HouseOwnerDashboardActivity::class.java))
            }
        }else{
            startActivity(Intent(this,LoginActivity::class.java))
        }
        finish();
    }

}