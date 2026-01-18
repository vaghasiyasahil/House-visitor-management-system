package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityViewVisitorBinding
import model.VisitorModel
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.google.android.material.imageview.ShapeableImageView
import com.google.api.LogDescriptor
import com.google.firebase.Timestamp
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import services.setBase64Image
import types.VisitorStatus
import utils.TimeAndDateUtils
import java.util.Calendar
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream
import android.graphics.Typeface
import android.graphics.Color

class ViewVisitorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewVisitorBinding
    private var selectedVisitorFilter: VisitorStatus? = null;
    private var visitorsList: List<VisitorModel> = emptyList()
    private var filterVisitorsList: List<VisitorModel> = emptyList()
    private var selectedDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewVisitorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0

        setupClickListeners()
        getVisitor()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.chipAll.setOnClickListener {
            selectedVisitorFilter=null
            setupFilters()
        }
        binding.chipPending.setOnClickListener {
            selectedVisitorFilter=VisitorStatus.Pending
            setupFilters()
        }
        binding.chipInside.setOnClickListener {
            selectedVisitorFilter=VisitorStatus.Inside
            setupFilters()
        }
        binding.chipCheckedOut.setOnClickListener {
            selectedVisitorFilter=VisitorStatus.CheckedOut
            setupFilters()
        }
        binding.chipExpired.setOnClickListener {
            selectedVisitorFilter=VisitorStatus.Expired
            setupFilters()
        }
        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this)
        }
        binding.visitorLogsSearch.addTextChangedListener {
            setupFilters()
        }
        binding.btnCalendar.setOnClickListener {
            showDatePickerDialog()
        }
        binding.btnGenerateReport.setOnClickListener {
            if (filterVisitorsList.isEmpty()) {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            } else {
                generatePDF()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = selectedDate ?: Calendar.getInstance()
        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                setupFilters()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Add a "Clear" button to the dialog
        datePickerDialog.setButton(android.app.DatePickerDialog.BUTTON_NEUTRAL, "Clear") { _, _ ->
            selectedDate = null
            setupFilters()
        }
        
        datePickerDialog.show()
    }

    private fun getVisitor(){
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.VISITOR, mapOf()) {
                snapshot ->
            val visitorList = mutableListOf<VisitorModel>()
            snapshot?.documents?.forEach {
                visitorList.add(VisitorModel.fromHashMap(it.data ?: emptyMap()))
            }
            visitorsList = visitorList
            setupFilters()
        };
    }

    private fun setupFilters() {
        val searchText = binding.visitorLogsSearch.text.toString().trim().lowercase()
        filterVisitorsList = visitorsList.filter {
                visitorModel ->
            val statusMatch = selectedVisitorFilter == null || visitorModel.status == selectedVisitorFilter
            
            val searchMatch =
                searchText.isEmpty() ||
                        visitorModel.name.lowercase().contains(searchText) ||
                        visitorModel.mobile.lowercase().contains(searchText) ||
                        visitorModel.houseNo.lowercase().contains(searchText)
            
            val dateMatch = selectedDate == null || visitorModel.entryTime?.let {
                val visitorCalendar = Calendar.getInstance()
                visitorCalendar.time = it.toDate()
                visitorCalendar.get(Calendar.YEAR) == selectedDate?.get(Calendar.YEAR) &&
                        visitorCalendar.get(Calendar.MONTH) == selectedDate?.get(Calendar.MONTH) &&
                        visitorCalendar.get(Calendar.DAY_OF_MONTH) == selectedDate?.get(Calendar.DAY_OF_MONTH)
            } ?: false || visitorModel.exitTime?.let {
                val visitorCalendar = Calendar.getInstance()
                visitorCalendar.time = it.toDate()
                visitorCalendar.get(Calendar.YEAR) == selectedDate?.get(Calendar.YEAR) &&
                        visitorCalendar.get(Calendar.MONTH) == selectedDate?.get(Calendar.MONTH) &&
                        visitorCalendar.get(Calendar.DAY_OF_MONTH) == selectedDate?.get(Calendar.DAY_OF_MONTH)
            } ?: false

            statusMatch && searchMatch && dateMatch
        }
        
        if (selectedDate != null) {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            binding.tvDateLabel.text = "Visitors on ${sdf.format(selectedDate!!.time)}"
        } else {
            binding.tvDateLabel.text = "Visitor Logs"
        }
        
        binding.visitorEntriesCount.text = "${filterVisitorsList.size} Entries"
        displayFilteredVisitors()
    }

    private fun displayFilteredVisitors() {
        binding.visitorContainer.removeAllViews()
        if (filterVisitorsList.isEmpty()) {
            binding.visitorContainer.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            return
        }
        binding.visitorContainer.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE
        for (visitor in filterVisitorsList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_all_visitor,
                binding.visitorContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.visitorName).text = visitor.name
            itemView.findViewById<TextView>(R.id.visitorStatus).text = visitor.status.name.uppercase()
            itemView.findViewById<TextView>(R.id.visitorStatus).setTextColor(
                ContextCompat.getColor(itemView.context, VisitorStatus.getColor(visitor.status))
            )
            itemView.findViewById<TextView>(R.id.visitorHouseInfo).text = "House #${visitor.houseNo}"
            itemView.findViewById<TextView>(R.id.visitorPurpose).text = visitor.purpose
            itemView.findViewById<TextView>(R.id.visitorCheckIn).text = TimeAndDateUtils.formatShortDateTime(visitor.entryTime)
            itemView.findViewById<TextView>(R.id.visitorCheckOut).text = TimeAndDateUtils.formatShortDateTime(visitor.exitTime)
            itemView.findViewById<ShapeableImageView>(R.id.visitorImage).setBase64Image(visitor.visitorPhoto)
            binding.visitorContainer.addView(itemView)
        }
    }

    private fun generatePDF() {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = 40f

        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 20f
        titlePaint.color = Color.BLACK
        canvas.drawText("Visitor Management System - Report", 120f, yPos, titlePaint)

        yPos += 30f
        paint.textSize = 12f
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", java.util.Locale.getDefault())
        canvas.drawText("Generated on: ${sdf.format(System.currentTimeMillis())}", 40f, yPos, paint)

        yPos += 20f
        val filterStatus = selectedVisitorFilter?.name ?: "All"
        val filterDate = if (selectedDate != null) {
            val dateSdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            dateSdf.format(selectedDate!!.time)
        } else "All"
        canvas.drawText("Filters - Status: $filterStatus, Date: $filterDate", 40f, yPos, paint)

        yPos += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f
        
        val colName = 20f
        val colMobile = 110f
        val colHouse = 190f
        val colPurpose = 230f
        val colStatus = 290f
        val colInTime = 340f
        val colOutTime = 470f

        canvas.drawText("Name", colName, yPos, paint)
        canvas.drawText("Mobile No", colMobile, yPos, paint)
        canvas.drawText("House", colHouse, yPos, paint)
        canvas.drawText("Purpose", colPurpose, yPos, paint)
        canvas.drawText("Status", colStatus, yPos, paint)
        canvas.drawText("In Time", colInTime, yPos, paint)
        canvas.drawText("Out Time", colOutTime, yPos, paint)

        yPos += 10f
        canvas.drawLine(20f, yPos, 575f, yPos, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        yPos += 20f

        for (visitor in filterVisitorsList) {
            if (yPos > 800f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 40f
            }

            canvas.drawText(shortenText(visitor.name, 15), colName, yPos, paint)
            canvas.drawText(visitor.mobile, colMobile, yPos, paint)
            canvas.drawText(visitor.houseNo, colHouse, yPos, paint)
            canvas.drawText(shortenText(visitor.purpose, 20), colPurpose, yPos, paint)
            canvas.drawText(visitor.status.name, colStatus, yPos, paint)
            canvas.drawText(TimeAndDateUtils.formatShortDateTime(visitor.entryTime).replace(",",""), colInTime, yPos, paint)
            canvas.drawText(TimeAndDateUtils.formatShortDateTime(visitor.exitTime).replace(",",""), colOutTime, yPos, paint)

            yPos += 20f
        }
        pdfDocument.finishPage(page)
        savePdf(pdfDocument)
    }

    private fun shortenText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) text.substring(0, maxLength - 3) + "..." else text
    }

    private fun savePdf(pdfDocument: PdfDocument) {
        val fileName = "Visitor_Report_${System.currentTimeMillis()}.pdf"
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
                    }
                }
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(directory, fileName)
                pdfDocument.writeTo(java.io.FileOutputStream(file))
                Toast.makeText(this, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
