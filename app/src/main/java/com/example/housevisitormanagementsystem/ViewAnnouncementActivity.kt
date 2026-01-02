package com.example.housevisitormanagementsystem

import adapter.AnnouncementAdapter
import android.content.ContentValues
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityViewAnnouncementBinding
import com.example.housevisitormanagementsystem.dialogs.DeleteConfirmationDialogFragment
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.google.firebase.firestore.FirebaseFirestore
import constants.DatabaseTableName
import model.Announcement
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class ViewAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAnnouncementBinding
    private lateinit var adapter: AnnouncementAdapter
    private val db = FirebaseFirestore.getInstance()
    private var allAnnouncements = ArrayList<Announcement>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar customization
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupRecyclerView()
        setupListeners()
        fetchAnnouncements()

        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
        }
    }

    private fun setupRecyclerView() {
        adapter = AnnouncementAdapter(this, allAnnouncements,
            onEditClick = { announcement ->
                val intent = Intent(this, AddAnnouncementActivity::class.java)
                intent.putExtra("announcement", announcement)
                startActivity(intent)
            },
            onDeleteClick = { announcement ->
                showDeleteDialog(announcement)
            }
        )
        binding.rvAnnouncements.layoutManager = LinearLayoutManager(this)
        binding.rvAnnouncements.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddAnnouncement.setOnClickListener {
            startActivity(Intent(this, AddAnnouncementActivity::class.java))
        }

        binding.btnGenerateReport.setOnClickListener {
            generatePDF()
        }
        
        // Filter button (icon) - Maybe redundant if chips are there, but provided in layout
        binding.btnFilter.setOnClickListener {
            // Optional: Show filter dialog, or just rely on Chips
            Toast.makeText(this, "Use chips to filter categories", Toast.LENGTH_SHORT).show()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterList(s.toString(), getCurrentFilter())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupFilters.setOnCheckedChangeListener { _, checkedId ->
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
        val filtered = allAnnouncements.filter { announcement ->
            val matchesSearch = announcement.title.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT)) ||
                                announcement.description.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))

            val matchesFilter = when (filterType) {
                "Urgent" -> announcement.category.equals("Urgent", ignoreCase = true)
                "Maintenance" -> announcement.category.equals("Maintenance", ignoreCase = true)
                "Event" -> announcement.category.equals("Event", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesFilter
        }

        if (filtered.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvAnnouncements.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvAnnouncements.visibility = View.VISIBLE
        }

        adapter.updateList(filtered)
    }

    private fun fetchAnnouncements() {
        db.collection(DatabaseTableName.ANNOUNCEMENTS)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allAnnouncements.clear()
                if (value != null) {
                    for (doc in value) {
                        try {
                            val announcement = doc.toObject(Announcement::class.java)
                            allAnnouncements.add(announcement)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                
                // Sort by something? Timestamp? For now simpler is better.
                // Apply current filters
                val currentQuery = binding.etSearch.text.toString()
                val currentFilter = getCurrentFilter()
                filterList(currentQuery, currentFilter)
            }
    }

    private fun showDeleteDialog(announcement: Announcement) {
        val dialog = DeleteConfirmationDialogFragment.newInstance(
            title = "Delete Announcement?",
            message = "Are you sure you want to delete '${announcement.title}'?",
            onConfirm = {
                deleteAnnouncement(announcement)
            }
        )
        dialog.show(supportFragmentManager, "DeleteAnnouncementDialog")
    }

    private fun deleteAnnouncement(announcement: Announcement) {
        db.collection(DatabaseTableName.ANNOUNCEMENTS).document(announcement.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generatePDF() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.textSize = 18f
        canvas.drawText("Announcements Report", 100f, 50f, paint)

        paint.textSize = 12f
        var yPosition = 100f

        for (item in allAnnouncements) {
             // Only export filtered or all? Export All for report usually.
            canvas.drawText("${item.title} [${item.category}] - ${item.date}", 50f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("   ${item.description.take(60)}...", 50f, yPosition, paint)
            yPosition += 30f
            
            if (yPosition > 800) break
        }

        pdfDocument.finishPage(page)

        val fileName = "Announcements_Report_${System.currentTimeMillis()}.pdf"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { pdfDocument.writeTo(it) }
                    Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
                }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                Toast.makeText(this, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }
}