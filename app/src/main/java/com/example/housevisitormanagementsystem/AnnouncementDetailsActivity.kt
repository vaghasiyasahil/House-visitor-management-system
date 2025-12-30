package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.housevisitormanagementsystem.databinding.ActivityAnnouncementDetailsBinding

class AnnouncementDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnnouncementDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnnouncementDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupClickListeners()
        loadAnnouncementData()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadAnnouncementData() {
        val title = intent.getStringExtra("TITLE") ?: ""
        val description = intent.getStringExtra("DESCRIPTION") ?: ""
        val category = intent.getStringExtra("CATEGORY") ?: ""
        val author = intent.getStringExtra("AUTHOR") ?: ""
        val timestamp = intent.getStringExtra("TIMESTAMP") ?: ""

        binding.tvTitle.text = title
        binding.tvDescription.text = description
        binding.tvAuthorInfo.text = "$author • $timestamp"

        // Handle badge
        when (category) {
            "Urgent" -> {
                binding.tvBadge.visibility = View.VISIBLE
                binding.tvBadge.text = "Urgent"
                binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_urgent)
                binding.tvBadge.setTextColor(ContextCompat.getColor(this, R.color.red))
            }
            "Event" -> {
                binding.tvBadge.visibility = View.VISIBLE
                binding.tvBadge.text = "Event"
                binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_event)
                binding.tvBadge.setTextColor(ContextCompat.getColor(this, R.color.bright_azure_blue))
            }
            else -> {
                binding.tvBadge.visibility = View.GONE
            }
        }
    }
}