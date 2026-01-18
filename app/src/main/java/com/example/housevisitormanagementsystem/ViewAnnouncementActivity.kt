package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.content.ContentValues
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityViewAnnouncementBinding
import com.example.housevisitormanagementsystem.dialogs.DeleteConfirmationDialogFragment
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Loader
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.AnnouncementModel
import services.setBase64Image
import types.AnnouncementsType
import types.UserRole
import types.VisitorStatus
import utils.TimeAndDateUtils
import java.io.File
import java.io.FileOutputStream

class ViewAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAnnouncementBinding
    private val db = FirebaseFirestore.getInstance()
    private var allAnnouncementsList: List<AnnouncementModel> = emptyList()
    private var filterAnnouncementsList: List<AnnouncementModel> = emptyList()
    private  var selectedAnnouncementsType : AnnouncementsType?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAnnouncementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.HouseOwner){
            binding.fabAddAnnouncement.visibility = View.GONE
        }
        setupListeners()
        getAllAnnouncement()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddAnnouncement.setOnClickListener {
            startActivity(Intent(this, AddAnnouncementActivity::class.java))
        }

        binding.btnGenerateReport.setOnClickListener {
            generatePDF()
        }

        binding.btnFilter.setOnClickListener {
            Toast.makeText(this, "Use chips to filter categories", Toast.LENGTH_SHORT).show()
        }
        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
        }
        binding.chipAll.setOnClickListener {
            selectedAnnouncementsType=null;
            setupFilters()
        }
        binding.chipUrgent.setOnClickListener {
            selectedAnnouncementsType=AnnouncementsType.Urgent;
            setupFilters()
        }
        binding.chipMaintenance.setOnClickListener {
            selectedAnnouncementsType=AnnouncementsType.Maintenance;
            setupFilters()
        }
        binding.chipEvents.setOnClickListener {
            selectedAnnouncementsType=AnnouncementsType.Event;
            setupFilters()
        }
        binding.announcementsSearch.addTextChangedListener {
            setupFilters()
        }
    }

    private fun getAllAnnouncement(){
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.ANNOUNCEMENTS, mapOf()) {
                snapshot ->
            val announcementsList = mutableListOf<AnnouncementModel>()
            snapshot?.documents?.forEach {
                announcementsList.add(AnnouncementModel.fromHashMap(it.data ?: emptyMap()))
            }
            allAnnouncementsList = announcementsList
            setupFilters()
        };
    }

    private fun setupFilters() {
        val searchText = binding.announcementsSearch.text.toString().trim().lowercase()
        filterAnnouncementsList = allAnnouncementsList.filter {
                announcementModel ->
            val statusMatch = selectedAnnouncementsType == null || announcementModel.category == selectedAnnouncementsType

            val searchMatch =
                searchText.isEmpty() ||
                        announcementModel.title.lowercase().contains(searchText) ||
                        announcementModel.description.lowercase().contains(searchText)

            statusMatch && searchMatch
        }
        displayFilteredAnnouncements()
        Log.d("","filterAnnouncementsList = ${filterAnnouncementsList.size}")
    }

    private fun displayFilteredAnnouncements() {
        binding.announcementsContainer.removeAllViews()
        if (filterAnnouncementsList.isEmpty()) {
            binding.announcementsContainer.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            return
        }
        binding.announcementsContainer.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE
        for (announcements in filterAnnouncementsList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_announcement,
                binding.announcementsContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.announcementsTitle).text = announcements.title
            itemView.findViewById<TextView>(R.id.announcementsDescription).text = announcements.description
            itemView.findViewById<TextView>(R.id.announcementsInfo).text = "Admin • ${TimeAndDateUtils.formatShortDateTime(announcements.timestamp)}"
            val categoryTv = itemView.findViewById<TextView>(R.id.announcementsCategory)
            categoryTv.visibility = View.VISIBLE
            categoryTv.text = announcements.category.name
            categoryTv.setTextColor(ContextCompat.getColor(itemView.context, AnnouncementsType.getColor(announcements.category)))
            categoryTv.setBackgroundResource(AnnouncementsType.getBackground(announcements.category))
            itemView.findViewById<ImageView>(R.id.announcementsIcon).setImageResource(AnnouncementsType.getIcon(announcements.category))
            itemView.findViewById<ImageView>(R.id.announcementsIcon).setColorFilter(ContextCompat.getColor(itemView.context, AnnouncementsType.getColor(announcements.category)))
            itemView.findViewById<View>(R.id.announcementsLeftBorder).setBackgroundColor(ContextCompat.getColor(itemView.context, AnnouncementsType.getColor(announcements.category)))
            if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.HouseOwner){
                itemView.findViewById<ImageView>(R.id.announcementsBtnOptions).visibility = View.GONE
            } else {
                itemView.findViewById<ImageView>(R.id.announcementsBtnOptions).setOnClickListener { view ->
                    val popup = android.widget.PopupMenu(this, view)
                    popup.menuInflater.inflate(R.menu.menu_guard_options, popup.menu)
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> {
                                val intent = Intent(this, AddAnnouncementActivity::class.java)
                                intent.putExtra("announcement", announcements)
                                startActivity(intent)
                                true
                            }
                            R.id.action_delete -> {
                                showDeleteDialog(announcements)
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
            }
            binding.announcementsContainer.addView(itemView)
        }
    }


//    private fun setupRecyclerView() {
//        adapter = AnnouncementAdapter(this, allAnnouncements,
//            onEditClick = { announcement ->
//                val intent = Intent(this, AddAnnouncementActivity::class.java)
//                intent.putExtra("announcement", announcement)
//                startActivity(intent)
//            },
//            onDeleteClick = { announcement ->
//                showDeleteDialog(announcement)
//            }
//        )
//    }




//    private fun filterList(query: String, filterType: String) {
//        val filtered = allAnnouncements.filter { announcement ->
//            val matchesSearch = announcement.title.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT)) ||
//                                announcement.description.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
//
//            val matchesFilter = when (filterType) {
//                "Urgent" -> announcement.category.equals("Urgent", ignoreCase = true)
//                "Maintenance" -> announcement.category.equals("Maintenance", ignoreCase = true)
//                "Event" -> announcement.category.equals("Event", ignoreCase = true)
//                else -> true
//            }
//            matchesSearch && matchesFilter
//        }
//
//        if (filtered.isEmpty()) {
//            binding.tvEmptyState.visibility = View.VISIBLE
//            binding.rvAnnouncements.visibility = View.GONE
//        } else {
//            binding.tvEmptyState.visibility = View.GONE
//            binding.rvAnnouncements.visibility = View.VISIBLE
//        }
//
//        adapter.updateList(filtered)
//    }

//    private fun fetchAnnouncements() {
//        db.collection(DatabaseTableName.ANNOUNCEMENTS)
//            .addSnapshotListener { value, error ->
//                if (error != null) {
//                    Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
//                    return@addSnapshotListener
//                }
//
//                allAnnouncements.clear()
//                if (value != null) {
//                    for (doc in value) {
//                        try {
//                            val announcement = doc.toObject(AnnouncementModel::class.java)
//                            allAnnouncements.add(announcement)
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//                }
//
//                val currentQuery = binding.etSearch.text.toString()
//                val currentFilter = getCurrentFilter()
//                filterList(currentQuery, currentFilter)
//            }
//    }

    private fun showDeleteDialog(announcement: AnnouncementModel) {
        val dialog = DeleteConfirmationDialogFragment.newInstance(
            title = "Delete Announcement?",
            message = "Are you sure you want to delete '${announcement.title}'?",
            onConfirm = {
                deleteAnnouncement(announcement)
            }
        )
        dialog.show(supportFragmentManager, "DeleteAnnouncementDialog")
    }

    private fun deleteAnnouncement(announcement: AnnouncementModel) {
        lifecycleScope.launch {
            Loader.showLoader(this@ViewAnnouncementActivity)
            val success  = FirestoreService().deleteDocument(DatabaseTableName.ANNOUNCEMENTS, announcement.id)
            if(success){
                Toast.makeText(this@ViewAnnouncementActivity, "Deleted successfully", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@ViewAnnouncementActivity, "Delete failed", Toast.LENGTH_SHORT).show()
            }
            Loader.closeLoader()
        }
    }

    private fun generatePDF() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint()

        titlePaint.textSize = 20f
        titlePaint.isFakeBoldText = true
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Announcements Report", 595f / 2, 50f, titlePaint)

        val startX = 20f
        val endX = 575f
        var yPos = 100f
        val rowHeight = 25f
        val padding = 5f

        val colSn = startX
        val colTitle = colSn + 30f
        val colCategory = colTitle + 120f
        val colDate = colCategory + 80f
        val colTime = colDate + 80f
        val colAuthor = colTime + 80f
        val colDesc = colAuthor + 70f

        paint.color = android.graphics.Color.LTGRAY
        canvas.drawRect(startX, yPos - 18f, endX, yPos + 7f, paint)

        paint.color = android.graphics.Color.BLACK
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("#", colSn + padding, yPos, paint)
        canvas.drawText("Title", colTitle + padding, yPos, paint)
        canvas.drawText("Category", colCategory + padding, yPos, paint)
        canvas.drawText("Date", colDate + padding, yPos, paint)
        canvas.drawText("Time", colTime + padding, yPos, paint)
        canvas.drawText("Author", colAuthor + padding, yPos, paint)

        canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
        
        yPos += rowHeight
        paint.isFakeBoldText = false

        for ((index, item) in filterAnnouncementsList.withIndex()) {
            if (yPos > 800) break

            canvas.drawText((index + 1).toString(), colSn + padding, yPos, paint)
            canvas.drawText(item.title.take(18), colTitle + padding, yPos, paint)
            canvas.drawText(item.category.name, colCategory + padding, yPos, paint)
            canvas.drawText(item.date, colDate + padding, yPos, paint)
            canvas.drawText(item.time, colTime + padding, yPos, paint)
            canvas.drawText(item.author, colAuthor + padding, yPos, paint)
            
            canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
            
            yPos += rowHeight
        }

        canvas.drawLine(startX, 100f - 18f, startX, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colTitle, 100f - 18f, colTitle, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colCategory, 100f - 18f, colCategory, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colDate, 100f - 18f, colDate, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colTime, 100f - 18f, colTime, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colAuthor, 100f - 18f, colAuthor, yPos - rowHeight + 7f, paint)
        canvas.drawLine(endX, 100f - 18f, endX, yPos - rowHeight + 7f, paint)

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