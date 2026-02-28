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
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.housevisitormanagementsystem.databinding.ActivityViewHouseBinding
import com.example.housevisitormanagementsystem.dialogs.DeleteConfirmationDialogFragment
import com.example.housevisitormanagementsystem.utils.Loader
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import kotlinx.coroutines.launch
import model.HouseModel
import java.io.File
import java.io.FileOutputStream
import android.net.Uri
import androidx.core.content.FileProvider

class ViewHouseActivity : AppCompatActivity() {
    lateinit var binding: ActivityViewHouseBinding
    private var houseList: List<HouseModel> = emptyList()
    private var filterHouseList: List<HouseModel> = emptyList()
    private var searchText = "";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewHouseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupListeners()
        getHouses()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.fabAddHouse.setOnClickListener {
            startActivity(Intent(this, AddHouseActivity::class.java))
        }
        binding.btnGenerateReport.setOnClickListener {
            generatePDF()
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchText = query ?: ""
                setupFilters()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                searchText = newText ?: ""
                setupFilters()
                return true
            }
        })
    }

    private fun getHouses(){
        FirestoreService().getDocumentsQuery(DatabaseTableName.HOUSES) {
                snapshot ->
            val tempHouseList = mutableListOf<HouseModel>()
            snapshot?.documents?.forEach {
                tempHouseList.add(HouseModel.fromHashMap(it.data ?: emptyMap()))
            }
            houseList = tempHouseList
            setupFilters()
        };
    }

    private fun setupFilters() {
        filterHouseList = houseList.filter {
                houseModel ->
            val searchMatch =
                searchText.isEmpty() ||
                        houseModel.ownerName.lowercase().contains(searchText) ||
                        houseModel.ownerEmail.lowercase().contains(searchText) ||
                        houseModel.houseNo.lowercase().contains(searchText)
            searchMatch
        }
        binding.tvTotalBadge.text = "${filterHouseList.size} Total"
        displayFilteredHouse()
    }

    private fun displayFilteredHouse() {
        binding.houseContainer.removeAllViews()
        if (filterHouseList.isEmpty()) {
            binding.houseContainer.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
            return
        }
        binding.houseContainer.visibility = View.VISIBLE
        binding.tvEmptyState.visibility = View.GONE
        for (house in filterHouseList) {
            val itemView = layoutInflater.inflate(
                R.layout.item_house,
                binding.houseContainer,
                false
            )
            itemView.findViewById<TextView>(R.id.houseCardHouseNo).text = house.houseNo
            itemView.findViewById<TextView>(R.id.houseCardHouseOwner).text = "Owner by ${house.ownerName}"
            itemView.findViewById<TextView>(R.id.houseCardEmail).text = house.ownerEmail
            itemView.findViewById<TextView>(R.id.houseCardMembers).text = "${house.members} Members"
            itemView.findViewById<FrameLayout>(R.id.houseCardBtnEdit).setOnClickListener {
                val editIntent = Intent(this, AddHouseActivity::class.java)
                editIntent.putExtra("houseData", house)
                startActivity(editIntent)
            }
            itemView.findViewById<FrameLayout>(R.id.houseCardBtnDelete).setOnClickListener {
                showDeleteDialog(house)
            }
            binding.houseContainer.addView(itemView)
        }
    }

    private fun showDeleteDialog(house: HouseModel) {
        val dialog = DeleteConfirmationDialogFragment.newInstance(
            title = "Delete House?",
            message = "Are you sure you want to remove House ${house.houseNo}?",
            onConfirm = {
                deleteHouse(house)
            }
        )
        dialog.show(supportFragmentManager, "DeleteHouseDialog")
    }

    private fun deleteHouse(house: HouseModel) {
        lifecycleScope.launch {
            Loader.showLoader(this@ViewHouseActivity)
            val success  = FirestoreService().deleteDocument(DatabaseTableName.HOUSES, house.id)
            if(success){
                FirestoreService().deleteDocument(DatabaseTableName.USER, house.id)
                Toast.makeText(this@ViewHouseActivity, "House deleted successfully", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@ViewHouseActivity, "House Delete failed", Toast.LENGTH_SHORT).show()
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
        canvas.drawText("House Management Report", 595f / 2, 50f, titlePaint)

        // Table Constants
        val startX = 20f
        val endX = 575f
        var yPos = 100f
        val rowHeight = 25f
        val padding = 5f

        // Column Configs
        val colSn = startX
        val colHouseNo = colSn + 30f
        val colOwner = colHouseNo + 80f
        val colEmail = colOwner + 130f
        val colMembers = colEmail + 160f

        // 1. Draw Header Background
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawRect(startX, yPos - 18f, endX, yPos + 7f, paint)

        // 2. Draw Header Text
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("#", colSn + padding, yPos, paint)
        canvas.drawText("House No", colHouseNo + padding, yPos, paint)
        canvas.drawText("Owner Name", colOwner + padding, yPos, paint)
        canvas.drawText("Owner Email", colEmail + padding, yPos, paint)
        canvas.drawText("Members", colMembers + padding, yPos, paint)

        canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
        
        yPos += rowHeight
        paint.isFakeBoldText = false

        for ((index, item) in houseList.withIndex()) {
            if (yPos > 800) break

            canvas.drawText((index + 1).toString(), colSn + padding, yPos, paint)
            canvas.drawText(item.houseNo, colHouseNo + padding, yPos, paint)
            canvas.drawText(item.ownerName.take(20), colOwner + padding, yPos, paint)
            canvas.drawText(item.ownerEmail.take(25), colEmail + padding, yPos, paint)
            canvas.drawText(item.members.toString(), colMembers + padding, yPos, paint)
            
            canvas.drawLine(startX, yPos + 7f, endX, yPos + 7f, paint)
            yPos += rowHeight
        }

        // Vertical lines
        canvas.drawLine(startX, 100f - 18f, startX, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colHouseNo, 100f - 18f, colHouseNo, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colOwner, 100f - 18f, colOwner, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colEmail, 100f - 18f, colEmail, yPos - rowHeight + 7f, paint)
        canvas.drawLine(colMembers, 100f - 18f, colMembers, yPos - rowHeight + 7f, paint)
        canvas.drawLine(endX, 100f - 18f, endX, yPos - rowHeight + 7f, paint)

        pdfDocument.finishPage(page)

        val fileName = "House_Report_${System.currentTimeMillis()}.pdf"

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