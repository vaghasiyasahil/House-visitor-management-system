package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityViewBlocklistBinding
import com.google.android.material.imageview.ShapeableImageView
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.VisitorModel
import services.setBase64Image
import types.TempBlocklistType
import types.UserRole
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build

class ViewBlocklistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewBlocklistBinding
    private var isHistoryTab = false
    private var selectedTempBlockFilter: TempBlocklistType = TempBlocklistType.Blocked;
    private var visitorsList: List<VisitorModel> = emptyList()
    private var filterVisitorsList: List<VisitorModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBlocklistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Status bar customization
        window.statusBarColor = ContextCompat.getColor(this, R.color.night_blue)
        window.decorView.systemUiVisibility = 0
        getVisitor();
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnGenerateReport.setOnClickListener {
            if (filterVisitorsList.isEmpty()) {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            } else {
                generatePDF()
            }
        }

        binding.etSearch.addTextChangedListener {
            setupFilters()
        }

        binding.tabBlocked.setOnClickListener {
            if (isHistoryTab) {
                isHistoryTab = false
                updateTabUI()
                selectedTempBlockFilter=TempBlocklistType.Blocked
                setupFilters()
            }
        }

        binding.tabHistory.setOnClickListener {
            if (!isHistoryTab) {
                isHistoryTab = true
                updateTabUI()
                selectedTempBlockFilter=TempBlocklistType.History
                setupFilters()
            }
        }
    }

    private fun getVisitor(){
        val filter: Map<String, Any>
        if(UserRole.fromString(SharedPreferencesService.getUserRole())== UserRole.HouseOwner){
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
        };
    }

    private fun setupFilters() {
        val searchText = binding.etSearch.text.toString().trim().lowercase()
        Log.d("","search Text = ${searchText}")
        filterVisitorsList = visitorsList.filter {
                visitorModel ->
            var isBlocked = false;
            if(selectedTempBlockFilter == TempBlocklistType.Blocked){
               isBlocked = true;
            }else{
                isBlocked=false;
            }
            val statusMatch = visitorModel.isBlocked == isBlocked

            val searchMatch =
                searchText.isEmpty() ||
                        visitorModel.name.lowercase().contains(searchText) ||
                        visitorModel.mobile.lowercase().contains(searchText) ||
                        visitorModel.houseNo.lowercase().contains(searchText)

            statusMatch && searchMatch
        }
        displayFilteredVisitors()
    }

    private fun displayFilteredVisitors() {
        binding.tempBlockContainer.removeAllViews()
        if (filterVisitorsList.isEmpty()) {
            binding.tempBlockContainer.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
            return
        }
        binding.tempBlockContainer.visibility = View.VISIBLE
        binding.tvNoData.visibility = View.GONE
        for (visitor in filterVisitorsList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_blocked_visitor,
                binding.tempBlockContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.visitorName).text = visitor.name
            itemView.findViewById<TextView>(R.id.visitorMobile).text = visitor.mobile
            itemView.findViewById<TextView>(R.id.tvReason).text = visitor.blockedReason
            val hasReason = visitor.isBlocked && visitor.blockedReason.isNotEmpty()
            itemView.findViewById<View>(R.id.reasonContainer).visibility = if (hasReason) View.VISIBLE else View.GONE
            itemView.findViewById<View>(R.id.divider).visibility = if (hasReason) View.VISIBLE else View.GONE
            itemView.findViewById<TextView>(R.id.houseNo).text = "House #${visitor.houseNo}"
            if(UserRole.fromString(SharedPreferencesService.getUserRole())==UserRole.HouseOwner){
                itemView.findViewById<TextView>(R.id.houseNo).visibility = View.GONE
            }
            if(visitor.isBlocked){
                itemView.findViewById<TextView>(R.id.btnAction).text = "Unblock Visitor"
            }else{
                itemView.findViewById<TextView>(R.id.btnAction).text = "Block Visitor"
            }
            itemView.findViewById<TextView>(R.id.btnAction).setOnClickListener {
                if (visitor.isBlocked) {
                    lifecycleScope.launch {
                        FirestoreService().updateDocument(DatabaseTableName.VISITOR,visitor.id, mapOf("isBlocked" to false, "blockedReason" to ""));
                    }
                } else {
                    showBlockVisitorDialog(visitor)
                }
            }
            itemView.findViewById<ShapeableImageView>(R.id.visitorImage).setBase64Image(visitor.visitorPhoto)
            binding.tempBlockContainer.addView(itemView)
        }
    }

    private fun updateTabUI() {
        if (isHistoryTab) {
            binding.tabBlocked.setBackgroundResource(android.R.color.transparent)
            binding.tabBlocked.setTextColor(ContextCompat.getColor(this, R.color.text_grey))
            binding.tabHistory.setBackgroundResource(R.drawable.bg_chip_selected)
            binding.tabHistory.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tvSectionTitle.text = "Block History"
        } else {
            binding.tabHistory.setBackgroundResource(android.R.color.transparent)
            binding.tabHistory.setTextColor(ContextCompat.getColor(this, R.color.text_grey))
            binding.tabBlocked.setBackgroundResource(R.drawable.bg_chip_selected)
            binding.tabBlocked.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.tvSectionTitle.text = "Currently Blocked"
        }
    }

    private fun showBlockVisitorDialog(visitor: VisitorModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_block_visitor, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<TextView>(R.id.tvDescription).text = "Are you sure you want to block ${visitor.name}?\nThey will no longer be able to request entry."

        dialogView.findViewById<android.widget.Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.widget.Button>(R.id.btnBlockVisitor).setOnClickListener {
            val reason = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etReason).text.toString().trim()
            if (reason.isEmpty()) {
                Toast.makeText(this, "Please provide a reason", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                FirestoreService().updateDocument(
                    DatabaseTableName.VISITOR,
                    visitor.id,
                    mapOf("isBlocked" to true, "blockedReason" to reason)
                )
                dialog.dismiss()
                Toast.makeText(this@ViewBlocklistActivity, "${visitor.name} blocked", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
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
        canvas.drawText("Blocklist Report", 595f / 2, 50f, titlePaint)

        // Table Constants
        val startX = 20f
        val endX = 575f
        var yPos = 100f
        val rowHeight = 25f
        val padding = 5f

        // Column Configs
        val colSn = startX
        val colName = colSn + 30f
        val colMobile = colName + 110f
        val colHouseNo = colMobile + 110f
        val colReason = colHouseNo + 80f

        // 1. Draw Header Background
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawRect(startX, yPos - 18f, endX, yPos + 7f, paint)

        // 2. Draw Header Text
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("#", colSn + padding, yPos, paint)
        canvas.drawText("Name", colName + padding, yPos, paint)
        canvas.drawText("Mobile", colMobile + padding, yPos, paint)
        canvas.drawText("House No", colHouseNo + padding, yPos, paint)
        canvas.drawText("Reason", colReason + padding, yPos, paint)

        canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
        
        yPos += rowHeight
        paint.isFakeBoldText = false

        for ((index, item) in filterVisitorsList.withIndex()) {
            if (yPos > 800) break

            canvas.drawText((index + 1).toString(), colSn + padding, yPos, paint)
            canvas.drawText(item.name.take(18), colName + padding, yPos, paint)
            canvas.drawText(item.mobile, colMobile + padding, yPos, paint)
            canvas.drawText(item.houseNo, colHouseNo + padding, yPos, paint)
            canvas.drawText(item.blockedReason.take(30), colReason + padding, yPos, paint)
            
            canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
            yPos += rowHeight
        }

        // Vertical lines
        canvas.drawLine(startX, 100f - 18f, startX, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colName, 100f - 18f, colName, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colMobile, 100f - 18f, colMobile, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colHouseNo, 100f - 18f, colHouseNo, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colReason, 100f - 18f, colReason, yPos - rowHeight + 7f, paint)
        canvas.drawLine(endX, 100f - 18f, endX, yPos - rowHeight + 7f, paint)

        pdfDocument.finishPage(page)

        val fileName = "Blocklist_Report_${System.currentTimeMillis()}.pdf"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                    Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
                    openPdf(uri)
                }
            } else {
                val file = java.io.File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), fileName)
                pdfDocument.writeTo(java.io.FileOutputStream(file))
                Toast.makeText(this, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                val uri = androidx.core.content.FileProvider.getUriForFile(this, "${packageName}.provider", file)
                openPdf(uri)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }

    private fun openPdf(uri: android.net.Uri) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }
}