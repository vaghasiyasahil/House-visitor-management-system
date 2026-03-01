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
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.AnnouncementModel
import types.AnnouncementsType
import types.UserRole
import utils.TimeAndDateUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import android.graphics.Typeface
import android.graphics.Color
import android.net.Uri
import androidx.core.content.FileProvider

class ViewAnnouncementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAnnouncementBinding
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
            if (filterAnnouncementsList.isEmpty()) {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            } else {
                generatePDF()
            }
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
        binding.chipOther.setOnClickListener {
            selectedAnnouncementsType=AnnouncementsType.Other;
            setupFilters()
        }
        binding.announcementsSearch.addTextChangedListener {
            setupFilters()
        }
    }

    private fun getAllAnnouncement(){
        binding.progressBar.visibility = View.VISIBLE
        binding.announcementsContainer.visibility = View.GONE
        
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.ANNOUNCEMENTS, mapOf()) {
                snapshot ->
            val announcementsList = mutableListOf<AnnouncementModel>()
            snapshot?.documents?.forEach {
                announcementsList.add(AnnouncementModel.fromHashMap(it.data ?: emptyMap()))
            }
            allAnnouncementsList = announcementsList
            setupFilters()
            binding.progressBar.visibility = View.GONE
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
            itemView.findViewById<TextView>(R.id.announcementsInfo).text = "${announcements.createdRole.name} • ${TimeAndDateUtils.formatShortDateTime(announcements.createdAt)}"
            val categoryTv = itemView.findViewById<TextView>(R.id.announcementsCategory)
            categoryTv.visibility = View.VISIBLE
            categoryTv.text = announcements.category.name
            categoryTv.setTextColor(ContextCompat.getColor(itemView.context, AnnouncementsType.getColor(announcements.category)))
            categoryTv.setBackgroundResource(AnnouncementsType.getBackground(announcements.category))
            itemView.findViewById<ImageView>(R.id.announcementsIcon).setImageResource(AnnouncementsType.getIcon(announcements.category))
            itemView.findViewById<ImageView>(R.id.announcementsIcon).setColorFilter(ContextCompat.getColor(itemView.context, AnnouncementsType.getColor(announcements.category)))
            itemView.findViewById<View>(R.id.announcementsLeftBorder).setBackgroundColor(ContextCompat.getColor(itemView.context, AnnouncementsType.getColor(announcements.category)))
            if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.HouseOwner){
                itemView.findViewById<ImageView>(R.id.announcementsBtnOptions).visibility = View.INVISIBLE
            } else {
                itemView.findViewById<ImageView>(R.id.announcementsBtnOptions).setOnClickListener { view ->
                    val popup = android.widget.PopupMenu(this, view)
                    popup.menuInflater.inflate(R.menu.menu_guard_options, popup.menu)
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.action_edit -> {
                                val intent = Intent(this, AddAnnouncementActivity::class.java)
                                intent.putExtra(DatabaseTableName.ANNOUNCEMENTS, announcements)
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
        val paint = Paint()
        val titlePaint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = 50f

        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 20f
        titlePaint.color = Color.BLACK
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Announcements Report", 595f / 2, yPos, titlePaint)

        yPos += 30f
        paint.textSize = 12f
        canvas.drawText("Generated on: ${TimeAndDateUtils.formatShortDateTime(com.google.firebase.Timestamp.now())}", 40f, yPos, paint)

        yPos += 20f
        val filterText = if (binding.announcementsSearch.text.isNullOrBlank()) "All" else binding.announcementsSearch.text.toString()
        val categoryText = selectedAnnouncementsType?.name ?: "All"
        canvas.drawText("Filters - Category: $categoryText, Search: $filterText", 40f, yPos, paint)

        yPos += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        
        val startX = 20f
        val endX = 575f
        
        val colSn = startX
        val colTitle = colSn + 30f
        val colCategory = colTitle + 140f
        val colDate = colCategory + 80f
        val colTime = colDate + 110f
        val colAuthor = colTime + 80f

        paint.color = Color.LTGRAY
        canvas.drawRect(startX, yPos - 18f, endX, yPos + 7f, paint)

        paint.color = Color.BLACK
        canvas.drawText("#", colSn + 5, yPos, paint)
        canvas.drawText("Title", colTitle + 5, yPos, paint)
        canvas.drawText("Category", colCategory + 5, yPos, paint)
        canvas.drawText("Date & Time", colDate + 5, yPos, paint)
        canvas.drawText("Author", colAuthor + 5, yPos, paint)

        canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
        
        yPos += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        for ((index, item) in filterAnnouncementsList.withIndex()) {
            if (yPos > 800) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
                // Redraw table headers on new page
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.LTGRAY
                canvas.drawRect(startX, yPos - 18f, endX, yPos + 7f, paint)
                paint.color = Color.BLACK
                canvas.drawText("#", colSn + 5, yPos, paint)
                canvas.drawText("Title", colTitle + 5, yPos, paint)
                canvas.drawText("Category", colCategory + 5, yPos, paint)
                canvas.drawText("Date", colDate + 5, yPos, paint)
                canvas.drawText("Author", colAuthor + 5, yPos, paint)
                canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
                yPos += 30f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            canvas.drawText((index + 1).toString(), colSn + 5, yPos, paint)
            canvas.drawText(shortenText(item.title, 20), colTitle + 5, yPos, paint)
            canvas.drawText(item.category.name, colCategory + 5, yPos, paint)
            canvas.drawText(TimeAndDateUtils.formatShortDateTime(item.createdAt), colDate + 5, yPos, paint)
            canvas.drawText(item.createdRole.name, colAuthor + 5, yPos, paint)
            
            canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
            yPos += 30f
        }

        pdfDocument.finishPage(page)
        savePdf(pdfDocument)
    }

    private fun shortenText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) text.substring(0, maxLength - 3) + "..." else text
    }

    private fun savePdf(pdfDocument: PdfDocument) {
        val fileName = "Announcements_Report_${System.currentTimeMillis()}.pdf"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        pdfDocument.writeTo(outputStream)
                        outputStream.close()
                        Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
                        openPdf(uri)
                    }
                }
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(directory, fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                Toast.makeText(this, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                
                val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
                openPdf(uri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun openPdf(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }
}
