package com.example.housevisitormanagementsystem

import android.content.ContentValues
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityViewSecurityGuardBinding
import com.example.housevisitormanagementsystem.dialogs.DeleteConfirmationDialogFragment
import com.example.housevisitormanagementsystem.utils.KeyboardUtils
import com.example.housevisitormanagementsystem.utils.Loader
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.firestore.FirebaseFirestore
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.SecurityGuardModel
import services.setBase64Image
import types.SecurityGuardShiftType
import types.VisitorStatus
import java.io.File
import java.io.FileOutputStream
import android.net.Uri
import androidx.core.content.FileProvider

class ViewSecurityGuardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewSecurityGuardBinding
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
            if (filterSecurityGuardList.isEmpty()) {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            } else {
                generatePDF()
            }
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
        binding.securityGuardsContainer.removeAllViews()
        if (filterSecurityGuardList.isEmpty()) {
            binding.securityGuardsContainer.visibility = View.GONE
            binding.securityGuardsEmptyState.visibility = View.VISIBLE
            return
        }
        binding.securityGuardsContainer.visibility = View.VISIBLE
        binding.securityGuardsEmptyState.visibility = View.GONE
        for (securityGuard in filterSecurityGuardList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_security_guard,
                binding.securityGuardsContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.securityGuardName).text = securityGuard.name
            itemView.findViewById<TextView>(R.id.securityGuardEmail).text = securityGuard.email
            itemView.findViewById<TextView>(R.id.securityGuardShift).text = securityGuard.shift.name
            itemView.findViewById<TextView>(R.id.securityGuardShift).setTextColor(
                ContextCompat.getColor(itemView.context, SecurityGuardShiftType.getColor(securityGuard.shift))
            )
            itemView.findViewById<TextView>(R.id.securityGuardMobile).text = "• ${securityGuard.phoneNumber}"
            itemView.findViewById<ShapeableImageView>(R.id.securityGuardProfile).setBase64Image(securityGuard.image)

            itemView.findViewById<android.widget.ImageView>(R.id.btnMore).setOnClickListener { view ->
                val popup = android.widget.PopupMenu(this, view)
                popup.menuInflater.inflate(R.menu.menu_guard_options, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            val intent = Intent(this, AddSecurityGuardsActivity::class.java)
                            intent.putExtra("guardData", securityGuard)
                            startActivity(intent)
                            true
                        }
                        R.id.action_delete -> {
                            showDeleteDialog(securityGuard)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }

            binding.securityGuardsContainer.addView(itemView)
        }
    }

    private fun showDeleteDialog(guard: SecurityGuardModel) {
        val dialog = DeleteConfirmationDialogFragment.newInstance(
            title = "Delete Guard?",
            message = "Are you sure you want to remove ${guard.name}?",
            onConfirm = {
                deleteGuard(guard)
            }
        )
        dialog.show(supportFragmentManager, "DeleteGuardDialog")
    }

    private fun deleteGuard(securityGuard: SecurityGuardModel) {
        lifecycleScope.launch {
            Loader.showLoader(this@ViewSecurityGuardActivity)
            val success  = FirestoreService().deleteDocument(DatabaseTableName.SECURITY_GUARDS, securityGuard.id)
            if(success){
                FirestoreService().deleteDocument(DatabaseTableName.USER, securityGuard.id)
                Toast.makeText(this@ViewSecurityGuardActivity, "Security Guard deleted successfully", Toast.LENGTH_SHORT).show()
                getSecurityGuard() // Refresh the list
            }else{
                Toast.makeText(this@ViewSecurityGuardActivity, "Security Guard Delete failed", Toast.LENGTH_SHORT).show()
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

        // Page Title
        titlePaint.textSize = 20f
        titlePaint.isFakeBoldText = true
        titlePaint.textAlign = Paint.Align.CENTER
        canvas.drawText("Security Guards Report", 595f / 2, 50f, titlePaint)

        var yPos = 80f
        paint.textSize = 12f
        canvas.drawText("Generated on: ${utils.TimeAndDateUtils.formatShortDateTime(com.google.firebase.Timestamp.now())}", 40f, yPos, paint)

        yPos += 20f
        val filterShift = selectedSecurityGuardFilter?.name ?: "All"
        val filterSearch =
            if (binding.etSearch.text.isNullOrBlank())
                "All"
            else
                binding.etSearch.text.toString()
        canvas.drawText("Filters - Shift: $filterShift, Search: $filterSearch", 40f, yPos, paint)

        yPos += 30f

        // Table Constants
        val startX = 20f
        val endX = 575f
        val rowHeight = 25f
        val padding = 5f
        val tableStartY = yPos

        // Column Configs
        val colSn = startX
        val colName = colSn + 30f
        val colEmail = colName + 110f
        val colPhone = colEmail + 140f
        val colShift = colPhone + 90f
        val colSalary = colShift + 60f

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

        for ((index, item) in filterSecurityGuardList.withIndex()) {
            if (yPos > 800) break

            canvas.drawText((index + 1).toString(), colSn + padding, yPos, paint)
            canvas.drawText(item.name.take(18), colName + padding, yPos, paint)
            canvas.drawText(item.email.take(22), colEmail + padding, yPos, paint)
            canvas.drawText(item.phoneNumber, colPhone + padding, yPos, paint)
            canvas.drawText(item.shift.name, colShift + padding, yPos, paint)
            canvas.drawText(item.salary.toString(), colSalary + padding, yPos, paint)
            
            canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
            yPos += rowHeight
        }

        // Vertical lines
        canvas.drawLine(startX, tableStartY - 18f, startX, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colName, tableStartY - 18f, colName, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colEmail, tableStartY - 18f, colEmail, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colPhone, tableStartY - 18f, colPhone, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colShift, tableStartY - 18f, colShift, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colSalary, tableStartY - 18f, colSalary, yPos - rowHeight + 7f, paint)
        canvas.drawLine(endX, tableStartY - 18f, endX, yPos - rowHeight + 7f, paint)

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
                    openPdf(uri)
                }
            } else {
                val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                pdfDocument.writeTo(FileOutputStream(file))
                Toast.makeText(this, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                
                val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
                openPdf(uri)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }
}