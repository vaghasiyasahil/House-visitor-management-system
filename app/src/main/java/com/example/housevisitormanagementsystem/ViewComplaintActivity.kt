package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.transition.Visibility
import com.example.housevisitormanagementsystem.databinding.ActivityViewComplaintBinding
import com.google.android.material.card.MaterialCardView
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import model.ComplaintModel
import types.AnnouncementsType
import types.ComplaintStatusType
import types.UserRole
import utils.TimeAndDateUtils
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import android.net.Uri
import androidx.core.content.FileProvider

class ViewComplaintActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewComplaintBinding
    private var allComplaintList: List<ComplaintModel> = emptyList()
    private var filterComplaintList: List<ComplaintModel> = emptyList()
    private  var selectedComplaintStatusType : ComplaintStatusType?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewComplaintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        if(UserRole.fromString(SharedPreferencesService.getUserRole())!=UserRole.HouseOwner){
            binding.fabAddComplaint.visibility=View.GONE;
        }

        setupClickListeners()
        getAllComplaint()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnGenerateReport.setOnClickListener {
            if (filterComplaintList.isEmpty()) {
                android.widget.Toast.makeText(this, "No data to export", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                generatePDF()
            }
        }
        binding.fabAddComplaint.setOnClickListener {
            startActivity(Intent(this,AddComplaintActivity::class.java))
        }
        binding.chipPending.setOnClickListener {
            selectedComplaintStatusType= ComplaintStatusType.Pending;
            setupFilters()
        }
        binding.chipInProgress.setOnClickListener {
            selectedComplaintStatusType=ComplaintStatusType.InProgress;
            setupFilters()
        }
        binding.chipResolved.setOnClickListener {
            selectedComplaintStatusType=ComplaintStatusType.Resolved;
            setupFilters()
        }
        binding.chipAll.setOnClickListener {
            selectedComplaintStatusType=null;
            setupFilters()
        }
        binding.etSearch.addTextChangedListener {
            setupFilters()
        }
    }

    private fun getAllComplaint(){
        binding.progressBar.visibility = View.VISIBLE
        binding.complaintsContainer.visibility = View.GONE
        
        val filter: Map<String, Any>
        if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.HouseOwner){
            filter = mapOf("createdHouseNo" to SharedPreferencesService.getUserHouseNo())
        }else{
            filter = mapOf()
        }
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.COMPLAINT, filter) {
                snapshot ->
            val announcementsList = mutableListOf<ComplaintModel>()
            snapshot?.documents?.forEach {
                val model = ComplaintModel.fromHashMap(it.data ?: emptyMap())
                if (model.title.isNotEmpty() || model.description.isNotEmpty()) {
                    announcementsList.add(model)
                }
            }
            allComplaintList = announcementsList
            setupFilters()
            binding.progressBar.visibility = View.GONE
        };
    }

    private fun setupFilters() {
        val searchText = binding.etSearch.text.toString().trim().lowercase()
        filterComplaintList = allComplaintList.filter {
                announcementModel ->
            val statusMatch = selectedComplaintStatusType == null || announcementModel.status == selectedComplaintStatusType

            val searchMatch =
                searchText.isEmpty() ||
                        announcementModel.title.lowercase().contains(searchText) ||
                        announcementModel.description.lowercase().contains(searchText)

            statusMatch && searchMatch
        }
        displayFilteredComplaint()
        Log.d("","filterComplaintList = ${filterComplaintList.size}")
    }

    private fun displayFilteredComplaint() {
        binding.complaintsContainer.removeAllViews()
        if (filterComplaintList.isEmpty()) {
            binding.complaintsContainer.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            return
        }
        binding.complaintsContainer.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE
        for (complaint in filterComplaintList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_complaint,
                binding.complaintsContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.complaintItemTitle).text = complaint.title
            itemView.findViewById<TextView>(R.id.complaintItemTime).text = TimeAndDateUtils.formatShortDateTime(complaint.createdAt)
            itemView.findViewById<TextView>(R.id.complaintItemDescription).text = complaint.description
            itemView.findViewById<ConstraintLayout>(R.id.complaintItemMain).setOnClickListener {
                if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.Admin){
                    val intent = Intent(this, UpdateComplaintStatusActivity::class.java)
                    intent.putExtra("complaint", complaint)
                    startActivity(intent)
                }
            }
            itemView.findViewById<TextView>(R.id.complaintItemStatus).text = ComplaintStatusType.toDisplayString(complaint.status)
            itemView.findViewById<TextView>(R.id.complaintItemStatus).setTextColor(ContextCompat.getColor(this, ComplaintStatusType.getColor(complaint.status)))
            itemView.findViewById<ImageView>(R.id.ivComplaintIcon).setImageResource(ComplaintStatusType.getIcon(complaint.status))

            binding.complaintsContainer.addView(itemView)
        }
    }

    private fun generatePDF() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint()

        // Page Title
        titlePaint.textSize = 20f
        titlePaint.isFakeBoldText = true
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Complaints Report", 595f / 2, 50f, titlePaint)

        var yPos = 80f
        paint.textSize = 12f
        canvas.drawText("Generated on: ${utils.TimeAndDateUtils.formatShortDateTime(com.google.firebase.Timestamp.now())}", 40f, yPos, paint)

        yPos += 20f
        val filterStatus = selectedComplaintStatusType?.name ?: "All"
        val filterSearch =
            if (binding.etSearch.text.isNullOrBlank())
                "All"
            else
                binding.etSearch.text.toString()
        canvas.drawText("Filters - Status: $filterStatus, Search: $filterSearch", 40f, yPos, paint)

        yPos += 30f

        // Table Constants
        val startX = 20f
        val endX = 575f
        val rowHeight = 25f
        val padding = 5f
        val tableStartY = yPos

        // Column Configs
        val colSn = startX
        val colTitle = colSn + 30f
        val colDesc = colTitle + 140f
        val colDate = colDesc + 160f
        val colStatus = colDate + 130f

        // 1. Draw Header Background
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawRect(startX, yPos - 18f, endX, yPos + 7f, paint)

        // 2. Draw Header Text
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("#", colSn + padding, yPos, paint)
        canvas.drawText("Title", colTitle + padding, yPos, paint)
        canvas.drawText("Description", colDesc + padding, yPos, paint)
        canvas.drawText("Date", colDate + padding, yPos, paint)
        canvas.drawText("Status", colStatus + padding, yPos, paint)

        canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
        
        yPos += rowHeight
        paint.isFakeBoldText = false

        for ((index, item) in filterComplaintList.withIndex()) {
            if (yPos > 800) break

            canvas.drawText((index + 1).toString(), colSn + padding, yPos, paint)
            canvas.drawText(item.title.take(20), colTitle + padding, yPos, paint)
            canvas.drawText(item.description.take(30), colDesc + padding, yPos, paint)
            canvas.drawText(TimeAndDateUtils.formatShortDateTime(item.createdAt), colDate + padding, yPos, paint)
            canvas.drawText(item.status.name, colStatus + padding, yPos, paint)
            
            canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
            yPos += rowHeight
        }

        // Vertical lines
        canvas.drawLine(startX, tableStartY - 18f, startX, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colTitle, tableStartY - 18f, colTitle, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colDesc, tableStartY - 18f, colDesc, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colDate, tableStartY - 18f, colDate, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colStatus, tableStartY - 18f, colStatus, yPos - rowHeight + 7f, paint)
        canvas.drawLine(endX, tableStartY - 18f, endX, yPos - rowHeight + 7f, paint)

        pdfDocument.finishPage(page)

        val fileName = "Complaints_Report_${System.currentTimeMillis()}.pdf"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    android.widget.Toast.makeText(this, "PDF saved to Downloads", android.widget.Toast.LENGTH_LONG).show()
                    openPdf(uri)
                }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                android.widget.Toast.makeText(this, "PDF saved to ${file.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
                val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
                openPdf(uri)
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(this, "Error generating PDF: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            e.printStackTrace()
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
            android.widget.Toast.makeText(this, "No PDF viewer found", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}