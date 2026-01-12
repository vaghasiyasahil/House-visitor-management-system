package com.example.housevisitormanagementsystem

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.housevisitormanagementsystem.databinding.ActivityAddAnnouncementBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Validators
import com.google.firebase.firestore.FirebaseFirestore
import constants.DatabaseTableName
import model.AnnouncementModel
import java.util.Calendar
import java.util.Locale

class AddAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAnnouncementBinding
    private val calendar = Calendar.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    private var isEditMode = false
    private var announcementId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupCategoryDropdown()
        setupClickListeners()
        setupValidation()
        
        checkForEditMode()
        
        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
        }
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf("Urgent", "Maintenance", "Event")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }
    
    private fun checkForEditMode() {
        val announcement = intent.getSerializableExtra("announcement") as? AnnouncementModel
        if (announcement != null) {
            isEditMode = true
            announcementId = announcement.id
            
            binding.etTitle.setText(announcement.title)
            binding.actvCategory.setText(announcement.category, false) // false to filter
            binding.etDescription.setText(announcement.description)
            binding.etDate.setText(announcement.date)
            binding.etTime.setText(announcement.time)
            
            binding.btnSendNotification.text = "Update Announcement"
        } else {
            updateDateDisplay()
            updateTimeDisplay()
        }
    }

    private fun setupValidation() {
        binding.etTitle.addTextChangedListener { binding.tilTitle.error = null }
        binding.actvCategory.addTextChangedListener { binding.tilCategory.error = null }
        binding.etDescription.addTextChangedListener { binding.tilDescription.error = null }
        binding.etDate.addTextChangedListener { binding.etDate.error = null }
        binding.etTime.addTextChangedListener { binding.etTime.error = null }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }

        binding.etDate.setOnClickListener { showDatePicker() }
        binding.etTime.setOnClickListener { showTimePicker() }

        binding.btnSendNotification.setOnClickListener {
            validateAndSave()
        }
    }

    private fun validateAndSave() {
        val title = binding.etTitle.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val time = binding.etTime.text.toString().trim()

        var isValid = true

        if (Validators.validateRequired(title, "Title") != null) {
            binding.tilTitle.error = Validators.validateRequired(title, "Title")
            isValid = false
        }
        if (category.isEmpty()) {
            binding.tilCategory.error = "Category is Required"
            isValid = false
        }
        if (Validators.validateRequired(description, "Description") != null) {
            binding.tilDescription.error = Validators.validateRequired(description, "Description")
            isValid = false
        }
        if (date.isEmpty()) {
            binding.etDate.error = "Date is Required"
            isValid = false
        }
        if (time.isEmpty()) {
            binding.etTime.error = "Time is Required"
            isValid = false
        }

        if (isValid) {
            saveAnnouncement(title, category, description, date, time)
        }
    }

    private fun saveAnnouncement(title: String, category: String, description: String, date: String, time: String) {
        binding.btnSendNotification.isEnabled = false
        binding.btnSendNotification.text = if (isEditMode) "Updating..." else "Sending..."

        val id = if (isEditMode && announcementId != null) announcementId!! else generateUniqueId()
        val announcement = AnnouncementModel(
            id = id,
            title = title,
            category = category,
            description = description,
            date = date,
            time = time,
            timestamp = "$date $time",
            author = "Admin", // Preserving default
            iconRes = R.drawable.ic_announcement,
            showReadMore = description.length > 50
        )

        db.collection(DatabaseTableName.ANNOUNCEMENTS).document(id)
            .set(announcement)
            .addOnSuccessListener {
                val msg = if (isEditMode) "Announcement Updated" else "Announcement Sent Successfully"
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.btnSendNotification.isEnabled = true
                binding.btnSendNotification.text = if (isEditMode) "Update Announcement" else "Send Notification"
            }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            R.style.CustomDatePickerDialog,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(
            this,
            R.style.CustomTimePickerDialog,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                updateTimeDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateDisplay() {
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(calendar.time))
    }

    private fun updateTimeDisplay() {
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
        binding.etTime.setText(timeFormat.format(calendar.time))
    }

    private fun generateUniqueId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20)
            .map { chars.random() }
            .joinToString("")
    }
}