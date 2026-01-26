package com.example.housevisitormanagementsystem

import adapter.SecurityGuardAdapter
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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityViewSecurityGuardBinding
import com.example.housevisitormanagementsystem.dialogs.DeleteConfirmationDialogFragment
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import model.SecurityGuardModel
import model.VisitorModel
import services.setBase64Image
import types.SecurityGuardShiftType
import types.UserRole
import types.VisitorStatus
import utils.TimeAndDateUtils
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

class ViewSecurityGuardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewSecurityGuardBinding
    private lateinit var adapter: SecurityGuardAdapter
    private val db = FirebaseFirestore.getInstance()
    private var allGuards = ArrayList<SecurityGuardModel>()
    private var searchText = "";
    private var selectedSecurityGuardFilter: SecurityGuardShiftType? = null;
    private var securityGuardList: List<SecurityGuardModel> = emptyList()
    private var filterSecurityGuardList: List<SecurityGuardModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewSecurityGuardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)
        setupListeners()
        getSecurityGuard()
    }
    private fun setupListeners() {
        binding.main.setOnClickListener {
            KeyboardUtils.hideKeyboard(this);
        }
        binding.btnBack.setOnClickListener { finish() }

        binding.fabAddGuard.setOnClickListener {
            startActivity(Intent(this, AddSecurityGuardsActivity::class.java))
        }
        binding.btnGenerateReport.setOnClickListener {
            generatePDF()
        }

        binding.etSearch.addTextChangedListener {
            setupFilters();
        }
        binding.chipAll.setOnClickListener {
            selectedSecurityGuardFilter=null
            setupFilters()
        }
        binding.chipMorning.setOnClickListener {
            selectedSecurityGuardFilter=SecurityGuardShiftType.Morning
            setupFilters()
        }
        binding.chipNight.setOnClickListener {
            selectedSecurityGuardFilter=SecurityGuardShiftType.Night
            setupFilters()
        }
    }

    private fun getSecurityGuard(){
        FirestoreService().getDocumentsWithEqualToConditions(DatabaseTableName.SECURITY_GUARDS,
            mapOf()
        ) {
                snapshot ->
            val securityList = mutableListOf<SecurityGuardModel>()
            snapshot?.documents?.forEach {
                securityList.add(SecurityGuardModel.fromHashMap(it.data ?: emptyMap()))
            }
            securityGuardList = securityList
            setupFilters()
        };
    }

    private fun setupFilters() {
        val searchText = binding.etSearch.text.toString().trim().lowercase()
        filterSecurityGuardList = securityGuardList.filter {
                securityGuardModel ->
            val statusMatch = selectedSecurityGuardFilter == null || securityGuardModel.shift == selectedSecurityGuardFilter

            val searchMatch =
                searchText.isEmpty() ||
                        securityGuardModel.name.lowercase().contains(searchText) ||
                        securityGuardModel.email.lowercase().contains(searchText) ||
                        securityGuardModel.phoneNumber.lowercase().contains(searchText)

            statusMatch && searchMatch
        }
        displayFilteredSecurityGuard()
    }

    private fun displayFilteredSecurityGuard() {
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

//    private fun fetchGuards() {
//        db.collection(DatabaseTableName.SECURITY_GUARDS)
//            .addSnapshotListener { value, error ->
//                if (error != null) {
//                    Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
//                    return@addSnapshotListener
//                }
//
//                allGuards.clear()
//                if (value != null) {
//                    for (doc in value) {
//                        val guard = doc.toObject(SecurityGuardModel::class.java)
//                        allGuards.add(guard)
//                    }
//                }
//                val currentFilter = idToFilterName(binding.chipGroupFilters.checkedChipId)
//                val currentQuery = binding.etSearch.text.toString()
//                filterList(currentQuery, currentFilter)
//            }
//    }

    private fun showDeleteDialog(guard: SecurityGuardModel) {
        val dialog = DeleteConfirmationDialogFragment.newInstance(
            title = "Delete Guard?",
            message = "Are you sure you want to remove ${guard.name}? This process is irreversible.",
            onConfirm = {
                deleteGuard(guard)
            }
        )
        dialog.show(supportFragmentManager, "DeleteGuardDialog")
    }

    private fun deleteGuard(guard: SecurityGuardModel) {
        db.collection(DatabaseTableName.SECURITY_GUARDS).document(guard.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Guard deleted successfully", Toast.LENGTH_SHORT).show()
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
        val titlePaint = Paint()

        // Page Title
        titlePaint.textSize = 20f
        titlePaint.isFakeBoldText = true
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Security Guards Report", 595f / 2, 50f, titlePaint)

        // Table Constants
        val startX = 20f
        val endX = 575f
        var yPos = 100f
        val rowHeight = 25f
        val padding = 5f

        // Column Configs
        val colSn = startX
        val colName = colSn + 30f
        val colEmail = colName + 110f
        val colPhone = colEmail + 120f
        val colShift = colPhone + 100f
        val colSalary = colShift + 70f

        // 1. Draw Header Background
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawRect(startX, yPos - 18f, endX, yPos + 7f, paint)

        // 2. Draw Header Text
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("#", colSn + padding, yPos, paint)
        canvas.drawText("Name", colName + padding, yPos, paint)
        canvas.drawText("Email", colEmail + padding, yPos, paint)
        canvas.drawText("Phone", colPhone + padding, yPos, paint)
        canvas.drawText("Shift", colShift + padding, yPos, paint)
        canvas.drawText("Salary", colSalary + padding, yPos, paint)

        canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
        
        yPos += rowHeight
        paint.isFakeBoldText = false

        for ((index, item) in allGuards.withIndex()) {
            if (yPos > 800) break

            canvas.drawText((index + 1).toString(), colSn + padding, yPos, paint)
            canvas.drawText(item.name.take(18), colName + padding, yPos, paint)
            canvas.drawText(item.email.take(22), colEmail + padding, yPos, paint)
            canvas.drawText(item.phoneNumber, colPhone + padding, yPos, paint)
            canvas.drawText(item.shift, colShift + padding, yPos, paint)
            canvas.drawText(item.salary.toString(), colSalary + padding, yPos, paint)
            
            canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
            yPos += rowHeight
        }

        // Vertical lines
        canvas.drawLine(startX, 100f - 18f, startX, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colName, 100f - 18f, colName, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colEmail, 100f - 18f, colEmail, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colPhone, 100f - 18f, colPhone, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colShift, 100f - 18f, colShift, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colSalary, 100f - 18f, colSalary, yPos - rowHeight + 7f, paint)
        canvas.drawLine(endX, 100f - 18f, endX, yPos - rowHeight + 7f, paint)

        pdfDocument.finishPage(page)

        val fileName = "Guards_Report_${System.currentTimeMillis()}.pdf"

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