package com.example.housevisitormanagementsystem

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import adapter.AnnouncementAdapter
import com.example.housevisitormanagementsystem.databinding.ActivityViewAnnouncementBinding
import model.Announcement
import com.example.todoapp.utils.KeyboardUtils

class ViewAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAnnouncementBinding
    private lateinit var adapter: AnnouncementAdapter
    private val allAnnouncements = ArrayList<Announcement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar customization
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupDummyData()
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupClickListeners()

        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this);
        }
        binding.fabAddAnnouncement.setOnClickListener {
            intent = Intent(this, AddAnnouncementActivity::class.java);
            startActivity(intent)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnFilter.setOnClickListener {
            android.widget.Toast.makeText(this, "Filter Clicked", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.btnGenerateReport.setOnClickListener {
            android.widget.Toast.makeText(this, "Exporting to PDF...", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.fabAddAnnouncement.setOnClickListener {
            android.widget.Toast.makeText(this, "Add Announcement", android.widget.Toast.LENGTH_SHORT).show()
            // TODO: Navigate to Add Announcement screen
        }
    }

    private fun setupDummyData() {
        allAnnouncements.add(
            Announcement(
                title = "Gate A Maintenance",
                description = "The main electronic gate will be offline for emergency repairs from 2 PM to...",
                category = "Urgent",
                author = "Admin",
                timestamp = "10:00 AM",
                iconRes = R.drawable.ic_home_filled,
                showReadMore = true,
                borderColor = R.color.red,
                iconColor = R.color.red
            )
        )
        allAnnouncements.add(
            Announcement(
                title = "Visitor Parking Full",
                description = "Please direct all guests to the B2 overflow lot until further notice due to...",
                category = "General",
                author = "Manager",
                timestamp = "Yesterday",
                iconRes = R.drawable.ic_home_filled,
                showReadMore = false,
                borderColor = R.color.bright_azure_blue,
                iconColor = R.color.bright_azure_blue
            )
        )
        allAnnouncements.add(
            Announcement(
                title = "Community Diwali Event",
                description = "Expect higher visitor volume this weekend. Additional temporary parki...",
                category = "Event",
                author = "Events Co.",
                timestamp = "2 Days ago",
                iconRes = R.drawable.ic_home_filled,
                showReadMore = true,
                borderColor = R.color.bright_azure_blue,
                iconColor = R.color.bright_azure_blue
            )
        )
        allAnnouncements.add(
            Announcement(
                title = "Lost Keys Found",
                description = "A set of car keys (Toyota) was found near the Clubhouse. Please verify...",
                category = "General",
                author = "Head Guard",
                timestamp = "3 Days ago",
                iconRes = R.drawable.ic_home_filled,
                showReadMore = false,
                borderColor = R.color.bright_azure_blue,
                iconColor = R.color.bright_azure_blue
            )
        )
        allAnnouncements.add(
            Announcement(
                title = "System Maintenance",
                description = "The Visitor Management System app will undergo server maintenance on...",
                category = "Maintenance",
                author = "IT Support",
                timestamp = "1 Week ago",
                iconRes = R.drawable.ic_home_filled,
                showReadMore = false,
                borderColor = R.color.bright_azure_blue,
                iconColor = R.color.bright_azure_blue
            )
        )
    }

    private fun setupRecyclerView() {
        adapter = AnnouncementAdapter(this, allAnnouncements)
        binding.rvAnnouncements.layoutManager = LinearLayoutManager(this)
        binding.rvAnnouncements.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString(), getCurrentFilter())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        binding.chipGroupFilters.setOnCheckedChangeListener { group, checkedId ->
            val query = binding.etSearch.text.toString()
            filterList(query, idToFilterName(checkedId))
        }
    }

    private fun getCurrentFilter(): String {
        return idToFilterName(binding.chipGroupFilters.checkedChipId)
    }

    private fun idToFilterName(id: Int): String {
        return when (id) {
            R.id.chipUrgent -> "Urgent"
            R.id.chipMaintenance -> "Maintenance"
            R.id.chipEvents -> "Event"
            else -> "All"
        }
    }

    private fun filterList(query: String, filterType: String) {
        val filteredList = allAnnouncements.filter { announcement ->
            val matchesSearch = announcement.title.lowercase().contains(query.lowercase()) ||
                    announcement.description.lowercase().contains(query.lowercase())

            val matchesFilter = when (filterType) {
                "Urgent" -> announcement.category == "Urgent"
                "Maintenance" -> announcement.category == "Maintenance"
                "Event" -> announcement.category == "Event"
                else -> true // All
            }

            matchesSearch && matchesFilter
        }

        binding.rvAnnouncements.adapter = AnnouncementAdapter(this, filteredList)
    }
}