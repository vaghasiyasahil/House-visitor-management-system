package com.example.housevisitormanagementsystem

import SharedPreferencesService
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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import services.base64Service
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.FileProvider
import types.UserRole

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
        binding.chipReject.setOnClickListener {
            selectedVisitorFilter=VisitorStatus.Reject
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
        binding.progressBar.visibility = View.VISIBLE
        binding.visitorContainer.visibility = View.GONE
        
        val filter: Map<String, Any>
        if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.HouseOwner){
            filter = mapOf("houseNo" to SharedPreferencesService.getUserHouseNo())
        }else{
            filter = mapOf()
        }
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.VISITOR, filter) {
                snapshot ->
            val visitorList = mutableListOf<VisitorModel>()
            snapshot?.documents?.forEach {
                visitorList.add(VisitorModel.fromHashMap(it.data ?: emptyMap()))
            }
            visitorsList = visitorList
            setupFilters()
            binding.progressBar.visibility = View.GONE
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
        val pageInfo = PdfDocument.PageInfo.Builder(800, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = 40f

        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 20f
        titlePaint.color = Color.BLACK
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Visitor Management System - Report", 800f / 2, yPos, titlePaint)

        yPos += 30f
        paint.textSize = 12f
        canvas.drawText("Generated on: ${TimeAndDateUtils.formatShortDateTime(Timestamp.now())}", 40f, yPos, paint)

        yPos += 20f
        val filterStatus = selectedVisitorFilter?.name ?: "All"
        val filterSearch =
            if (binding.visitorLogsSearch.text.isNullOrBlank())
                "All"
            else
                binding.visitorLogsSearch.text.toString()
        val filterDate = if (selectedDate != null) {
            val dateSdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            dateSdf.format(selectedDate!!.time)
        } else "All"
        canvas.drawText("Filters - Status: $filterStatus, Date: $filterDate, Search: $filterSearch", 40f, yPos, paint)

        yPos += 30f

        val startX = 20f
        val endX = 780f
        val rowHeight = 35f
        val padding = 5f

        val colPhoto = startX
        val colName = colPhoto + 45f
        val colMobile = colName + 120f
        val colHouse = colMobile + 80f
        val colPurpose = colHouse + 70f
        val colStatus = colPurpose + 70f
        val colInTime = colStatus + 80f
        val colOutTime = colInTime + 130f

        paint.color = Color.LTGRAY
        canvas.drawRect(startX, yPos - 18f, endX, yPos + 7f, paint)

        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 10f

        canvas.drawText("Photo", colPhoto + padding, yPos, paint)
        canvas.drawText("Name", colName + padding, yPos, paint)
        canvas.drawText("Mobile", colMobile + padding, yPos, paint)
        canvas.drawText("House", colHouse + padding, yPos, paint)
        canvas.drawText("Purpose", colPurpose + padding, yPos, paint)
        canvas.drawText("Status", colStatus + padding, yPos, paint)
        canvas.drawText("In Time", colInTime + padding, yPos, paint)
        canvas.drawText("Out Time", colOutTime + padding, yPos, paint)

        canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        yPos += rowHeight

        for (visitor in filterVisitorsList) {
            if (yPos > 800f) {
                // Draw vertical boundaries before concluding the page
                canvas.drawLine(startX, 100f - 18f, startX, yPos - rowHeight + 7f, paint)
                canvas.drawLine(colName, 100f - 18f, colName, yPos - rowHeight + 7f, paint)
                canvas.drawLine(colMobile, 100f - 18f, colMobile, yPos - rowHeight + 7f, paint)
                canvas.drawLine(colHouse, 100f - 18f, colHouse, yPos - rowHeight + 7f, paint)
                canvas.drawLine(colPurpose, 100f - 18f, colPurpose, yPos - rowHeight + 7f, paint)
                canvas.drawLine(colStatus, 100f - 18f, colStatus, yPos - rowHeight + 7f, paint)
                canvas.drawLine(colInTime, 100f - 18f, colInTime, yPos - rowHeight + 7f, paint)
                canvas.drawLine(colOutTime, 100f - 18f, colOutTime, yPos - rowHeight + 7f, paint)
                canvas.drawLine(endX, 100f - 18f, endX, yPos - rowHeight + 7f, paint)

                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
                
                // Redraw table headers on new page
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.LTGRAY
                canvas.drawRect(startX, yPos - 18f, endX, yPos + 7f, paint)
                paint.color = Color.BLACK
                canvas.drawText("Photo", colPhoto + padding, yPos, paint)
                canvas.drawText("Name", colName + padding, yPos, paint)
                canvas.drawText("Mobile", colMobile + padding, yPos, paint)
                canvas.drawText("House", colHouse + padding, yPos, paint)
                canvas.drawText("Purpose", colPurpose + padding, yPos, paint)
                canvas.drawText("Status", colStatus + padding, yPos, paint)
                canvas.drawText("In Time", colInTime + padding, yPos, paint)
                canvas.drawText("Out Time", colOutTime + padding, yPos, paint)
                canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
                
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                yPos += rowHeight
            }

            val visitorBitmap = base64Service.base64ToBitmap(visitor.visitorPhoto)
            if (visitorBitmap != null) {
                // Scale bitmap to fit within the 35f rowHeight
                val scaledBitmap = Bitmap.createScaledBitmap(visitorBitmap, 24, 24, false)
                canvas.drawBitmap(scaledBitmap, colPhoto + padding, yPos - 18f, paint)
            } else {
                canvas.drawText("N/A", colPhoto + padding, yPos, paint)
            }

            canvas.drawText(shortenText(visitor.name, 15), colName + padding, yPos, paint)
            canvas.drawText(visitor.mobile, colMobile + padding, yPos, paint)
            canvas.drawText(visitor.houseNo, colHouse + padding, yPos, paint)
            canvas.drawText(shortenText(visitor.purpose ?: "-", 10), colPurpose + padding, yPos, paint)
            canvas.drawText(visitor.status.name, colStatus + padding, yPos, paint)
            canvas.drawText(TimeAndDateUtils.formatShortDateTime(visitor.entryTime).replace(",",""), colInTime + padding, yPos, paint)
            canvas.drawText(TimeAndDateUtils.formatShortDateTime(visitor.exitTime).replace(",","").takeIf { it.isNotEmpty() && it != "N/A" } ?: "-", colOutTime + padding, yPos, paint)

            canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
            yPos += rowHeight
        }

        // Draw vertical lines for the last page
        val tableTopY = if(filterVisitorsList.isEmpty()) 100f else (yPos - (filterVisitorsList.size * rowHeight) - rowHeight)
        val limitY = if(tableTopY < 0f) 50f else tableTopY
        
        canvas.drawLine(startX, limitY - 18f, startX, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colName, limitY - 18f, colName, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colMobile, limitY - 18f, colMobile, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colHouse, limitY - 18f, colHouse, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colPurpose, limitY - 18f, colPurpose, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colStatus, limitY - 18f, colStatus, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colInTime, limitY - 18f, colInTime, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colOutTime, limitY - 18f, colOutTime, yPos - rowHeight + 7f, paint)
        canvas.drawLine(endX, limitY - 18f, endX, yPos - rowHeight + 7f, paint)

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
                        openPdf(uri)
                    }
                }
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(directory, fileName)
                pdfDocument.writeTo(java.io.FileOutputStream(file))
                Toast.makeText(this, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                
                val uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    file
                )
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
