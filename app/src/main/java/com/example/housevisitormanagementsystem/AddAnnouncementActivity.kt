package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityAddAnnouncementBinding
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Validators
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.AnnouncementModel
import services.SendNotificationService
import types.AnnouncementsType
import types.UserRole
import java.util.Calendar

class AddAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAnnouncementBinding
    private var isEditMode = false
    private var announcementId: String? = null
    private var originalAnnouncement: AnnouncementModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupCategoryDropdown()
        setupClickListeners()

        checkForEditMode()
    }

    private fun setupCategoryDropdown() {
        val categories = AnnouncementsType.values().map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }
    
    private fun checkForEditMode() {
        val announcement = IntentCompat.getParcelableExtra(intent, DatabaseTableName.ANNOUNCEMENTS, AnnouncementModel::class.java)
        if (announcement != null) {
            isEditMode = true
            originalAnnouncement = announcement
            announcementId = announcement.id
            binding.etTitle.setText(announcement.title)
            binding.actvCategory.setText(announcement.category.name, false) // false to filter
            binding.etDescription.setText(announcement.description)
            binding.switchNotify.isChecked = announcement.notifyResidents
            binding.btnSendNotification.text = "Update Announcement"
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSendNotification.setOnClickListener {
            validateAndSave()
        }
        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
        }
        binding.etTitle.addTextChangedListener { binding.tilTitle.error = null }
        binding.actvCategory.addTextChangedListener { binding.tilCategory.error = null }
        binding.etDescription.addTextChangedListener { binding.tilDescription.error = null }
    }

    private fun validateAndSave() {
        val title = binding.etTitle.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

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

        if (isValid) {
            saveAnnouncement(title, category, description)
        }
    }

    private fun saveAnnouncement(title: String, category: String, description: String) {
        binding.btnSendNotification.isEnabled = false
        binding.btnSendNotification.text = if (isEditMode) "Updating..." else "Sending..."

        val announcement = if (isEditMode) {
            originalAnnouncement?.copy(
                title = title,
                category = AnnouncementsType.fromString(category) ?: AnnouncementsType.Other,
                description = description,
                updatedAt = Timestamp.now(),
                notifyResidents = binding.switchNotify.isChecked
            )
        } else {
            AnnouncementModel(
                title = title,
                category = AnnouncementsType.fromString(category) ?: AnnouncementsType.Other,
                description = description,
                createdRole = UserRole.fromString(SharedPreferencesService.getUserRole()) ?: UserRole.Admin,
                createdAt = Timestamp.now(),
                notifyResidents = binding.switchNotify.isChecked
            )
        }

        if(!isEditMode){
            lifecycleScope.launch {
                try{
                    FirestoreService().addDocument(DatabaseTableName.ANNOUNCEMENTS,announcement!!.toHashMap());
                    if(binding.switchNotify.isChecked){
                        SendNotificationService.sendNotification("New Announcement",title)
                    }
                    Toast.makeText(this@AddAnnouncementActivity, "Announcement Sent Successfully", Toast.LENGTH_LONG).show()
                    finish()
                }catch (e:Exception){
                    Log.d("Sent Successfully","Fail To Sent Announcement = $e")
                    Toast.makeText(this@AddAnnouncementActivity, "Fail To Sent Announcement", Toast.LENGTH_SHORT).show()
                }finally {
                    binding.btnSendNotification.isEnabled = true
                    binding.btnSendNotification.text = "Send Notification"
                }
            }
        }else{
            lifecycleScope.launch {
                try{
                    FirestoreService().updateDocument(DatabaseTableName.ANNOUNCEMENTS,announcementId!!, announcement!!.toHashMap())
                    if(binding.switchNotify.isChecked){
                        SendNotificationService.sendNotification("Change Announcement",title)
                    }
                    Toast.makeText(this@AddAnnouncementActivity, "Announcement Updated", Toast.LENGTH_LONG).show()
                    finish()
                }catch (e:Exception){
                    Log.d("Update Error","Fail To Update Announcement = $e")
                    Toast.makeText(this@AddAnnouncementActivity, "Fail To Update Announcement", Toast.LENGTH_SHORT).show()
                }finally {
                    binding.btnSendNotification.isEnabled = true
                    binding.btnSendNotification.text = if (isEditMode) "Update Announcement" else "Send Notification"
                }
            }
        }
    }
}