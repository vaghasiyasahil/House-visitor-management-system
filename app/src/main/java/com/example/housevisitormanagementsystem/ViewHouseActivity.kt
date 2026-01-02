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
import model.House
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ViewHouseActivity : AppCompatActivity() {

    lateinit var binding: ActivityViewHouseBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var houseAdapter: HouseAdapter
    private var houseList = ArrayList<House>()

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
        
        db.collection("houses")
            .addSnapshotListener { value, error ->
                binding.progressBar.visibility = View.GONE
                if (error != null) {
                    Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                houseList.clear()
                if (value != null) {
                    for (doc in value) {
                        val house = doc.toObject(House::class.java)
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

    private fun showDeleteDialog(house: House) {
        val dialog = DeleteConfirmationDialogFragment.newInstance(
            title = "Delete House?",
            message = "Are you sure you want to remove House ${house.houseNo}? This process is irreversible.",
            onConfirm = {
                deleteHouse(house)
            }
        )
        dialog.show(supportFragmentManager, "DeleteHouseDialog")
    }

    private fun deleteHouse(house: House) {
        db.collection("houses").document(house.houseId)
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
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.textSize = 18f
        canvas.drawText("House Visitor Management System - House Report", 100f, 50f, paint)
        
        paint.textSize = 12f
        var yPosition = 100f
        
        for (house in houseList) {
            canvas.drawText("House No: ${house.houseNo}, Owner: ${house.ownerName} (${house.ownerEmail})", 50f, yPosition, paint)
            yPosition += 30f
            if (yPosition > 800) break // Simple paging check for demo
        }

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