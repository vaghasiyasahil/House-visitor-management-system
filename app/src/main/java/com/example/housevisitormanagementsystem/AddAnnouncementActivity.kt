package com.example.housevisitormanagementsystem

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.housevisitormanagementsystem.databinding.ActivityAddAnnouncementBinding
import com.example.todoapp.utils.KeyboardUtils
import java.util.Calendar
import java.util.Locale

class AddAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAnnouncementBinding
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar customization
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupClickListeners()
        updateDateDisplay()
        updateTimeDisplay()

        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this);
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.etTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnSendNotification.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val description = binding.etDescription.text.toString()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Announcement Sent Successfully", Toast.LENGTH_LONG).show()
                finish()
            }
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
}