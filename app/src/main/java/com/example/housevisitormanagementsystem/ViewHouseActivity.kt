package com.example.housevisitormanagementsystem

import adapter.HouseAdapter
import android.content.ContentValues
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityViewHouseBinding
import com.example.housevisitormanagementsystem.dialogs.DeleteConfirmationDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import constants.DatabaseTableName
import model.HouseModel
import java.io.File
import java.io.FileOutputStream

class ViewHouseActivity : AppCompatActivity() {

    lateinit var binding: ActivityViewHouseBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var houseAdapter: HouseAdapter
    private var houseList = ArrayList<HouseModel>()

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

        setupRecyclerView()
        setupListeners()
        fetchHouses()
    }

    private fun setupRecyclerView() {
        houseAdapter = HouseAdapter(houseList, 
            onEditClick = { house ->
                val intent = Intent(this, AddHouseActivity::class.java)
                intent.putExtra("houseData", house)
                startActivity(intent)
            },
            onDeleteClick = { house ->
                showDeleteDialog(house)
            }
        )
        binding.rvHouses.layoutManager = LinearLayoutManager(this)
        binding.rvHouses.adapter = houseAdapter
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
                houseAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                houseAdapter.filter.filter(newText)
                return false
            }
        })
    }

    private fun fetchHouses() {
        binding.progressBar.visibility = View.VISIBLE
        
        db.collection(DatabaseTableName.HOUSES)
            .addSnapshotListener { value, error ->
                binding.progressBar.visibility = View.GONE
                if (error != null) {
                    Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                houseList.clear()
                if (value != null) {
                    for (doc in value) {
                        val house = doc.toObject(HouseModel::class.java)
                        houseList.add(house)
                    }
                }
                
                houseAdapter.updateList(houseList)
                updateEmptyState()
            }
    }

    private fun updateEmptyState() {
        if (houseList.isEmpty()) {
            binding.rvHouses.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.tvTotalBadge.text = "0 Total"
        } else {
            binding.rvHouses.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
            binding.tvTotalBadge.text = "${houseList.size} Total"
        }
    }

    private fun showDeleteDialog(house: HouseModel) {
        val dialog = DeleteConfirmationDialogFragment.newInstance(
            title = "Delete House?",
            message = "Are you sure you want to remove House ${house.houseNo}? This process is irreversible.",
            onConfirm = {
                deleteHouse(house)
            }
        )
        dialog.show(supportFragmentManager, "DeleteHouseDialog")
    }

    private fun deleteHouse(house: HouseModel) {
        db.collection(DatabaseTableName.HOUSES).document(house.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "House deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting house: ${e.message}", Toast.LENGTH_SHORT).show()
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