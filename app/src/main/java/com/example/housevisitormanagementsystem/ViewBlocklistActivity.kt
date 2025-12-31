package com.example.housevisitormanagementsystem

import adapter.BlockedVisitorAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityViewBlocklistBinding
import model.BlockedVisitor

class ViewBlocklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewBlocklistBinding
    private lateinit var adapter: BlockedVisitorAdapter
    private var isHistoryTab = false

    private val blockedList = mutableListOf<BlockedVisitor>()
    private val historyList = mutableListOf<BlockedVisitor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBlocklistBinding.inflate(layoutInflater)
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

        setupDummyData()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = BlockedVisitorAdapter(
            this,
            if (isHistoryTab) historyList else blockedList,
            isHistoryTab
        ) { visitor ->
            handleAction(visitor)
        }
        binding.rvBlocklist.layoutManager = LinearLayoutManager(this)
        binding.rvBlocklist.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnGenerateReport.setOnClickListener {
            Toast.makeText(this, "Exporting to PDF...", Toast.LENGTH_SHORT).show()
        }

        binding.tabBlocked.setOnClickListener {
            if (isHistoryTab) {
                isHistoryTab = false
                updateTabUI()
            }
        }

        binding.tabHistory.setOnClickListener {
            if (!isHistoryTab) {
                isHistoryTab = true
                updateTabUI()
            }
        }
    }

    private fun updateTabUI() {
        if (isHistoryTab) {
            binding.tabBlocked.setBackgroundResource(android.R.color.transparent)
            binding.tabBlocked.setTextColor(ContextCompat.getColor(this, R.color.text_grey))
            binding.tabHistory.setBackgroundResource(R.drawable.bg_chip_selected)
            binding.tabHistory.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tvSectionTitle.text = "Block History"
        } else {
            binding.tabHistory.setBackgroundResource(android.R.color.transparent)
            binding.tabHistory.setTextColor(ContextCompat.getColor(this, R.color.text_grey))
            binding.tabBlocked.setBackgroundResource(R.drawable.bg_chip_selected)
            binding.tabBlocked.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tvSectionTitle.text = "Currently Blocked"
        }
        
        // Update adapter
        setupRecyclerView()
    }

    private fun handleAction(visitor: BlockedVisitor) {
        if (isHistoryTab) {
            showBlockVisitorDialog(visitor)
        } else {
            Toast.makeText(this, "${visitor.name} Unblocked successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBlockVisitorDialog(visitor: BlockedVisitor) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_block_visitor)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvDescription = dialog.findViewById<android.widget.TextView>(R.id.tvDescription)
        tvDescription.text = "Are you sure you want to block ${visitor.name}?\nThey will no longer be able to request entry."

        dialog.findViewById<android.view.View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<android.view.View>(R.id.btnBlockVisitor).setOnClickListener {
            Toast.makeText(this, "${visitor.name} Blocked successfully", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupDummyData() {
        // Blocked Data
        blockedList.add(
            BlockedVisitor(
                "1", "John Doe", "+1 (555) 019-2834", "Construction",
                "Until Oct 15", R.drawable.person_image, null, null, true
            )
        )
        blockedList.add(
            BlockedVisitor(
                "2", "Metro Deliveries", "Service Provider", "Vacation Mode",
                "Ends Nov 01", null, R.drawable.ic_delivery, R.color.orange, true
            )
        )
        blockedList.add(
            BlockedVisitor(
                "3", "Sarah Jenkins", "+1 (555) 991-2200", "Private Matter",
                "Indefinite", R.drawable.person_image, null, null, true
            )
        )

        // History Data (mocking some previously blocked visitors)
        historyList.add(
            BlockedVisitor(
                "4", "Mike Ross", "+1 (555) 123-4567", "Late Night Entry",
                "Resolved on Sep 10", null, R.drawable.ic_maintenance, R.color.bright_blue, false
            )
        )
        historyList.add(
            BlockedVisitor(
                "5", "Fast Track Couriers", "Delivery Service", "Misconduct",
                "Resolved on Aug 25", null, R.drawable.ic_delivery, R.color.red, false
            )
        )
    }
}