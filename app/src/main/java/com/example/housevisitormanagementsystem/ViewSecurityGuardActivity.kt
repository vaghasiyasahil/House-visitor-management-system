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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.housevisitormanagementsystem.databinding.ActivityViewSecurityGuardBinding
import com.example.housevisitormanagementsystem.dialogs.DeleteConfirmationDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import constants.DatabaseTableName
import model.SecurityGuard
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class ViewSecurityGuardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewSecurityGuardBinding
    private lateinit var adapter: SecurityGuardAdapter
    private val db = FirebaseFirestore.getInstance()
    private var allGuards = ArrayList<SecurityGuard>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewSecurityGuardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.night_blue)

        setupRecyclerView()
        setupListeners()
        fetchGuards()
    }

    private fun setupRecyclerView() {
        adapter = SecurityGuardAdapter(this, allGuards,
            onEditClick = { guard ->
                val intent = Intent(this, AddSecurityGuardsActivity::class.java)
                intent.putExtra("guardData", guard)
                startActivity(intent)
            },
            onDeleteClick = { guard ->
                showDeleteDialog(guard)
            }
        )
        binding.rvGuards.layoutManager = LinearLayoutManager(this)
        binding.rvGuards.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.fabAddGuard.setOnClickListener {
            startActivity(Intent(this, AddSecurityGuardsActivity::class.java))
        }

        binding.btnGenerateReport.setOnClickListener {
            generatePDF()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter.filter(s)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Chip Filters
        binding.chipGroupFilters.setOnCheckedChangeListener { _, checkedId ->
            val query = binding.etSearch.text.toString()
            filterList(query, idToFilterName(checkedId))
        }
    }
    
    private fun idToFilterName(id: Int): String {
        return when (id) {
            R.id.chipMorning -> "Morning"
            R.id.chipNight -> "Night"
            else -> "All"
        }
    }

    private fun filterList(query: String, filterType: String) {
        val filtered = allGuards.filter { guard ->
            val matchesSearch = guard.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT)) ||
                                guard.email.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
            
            val matchesFilter = when (filterType) {
                "Morning" -> guard.shift.equals("Morning", ignoreCase = true)
                "Night" -> guard.shift.equals("Night", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesFilter
        }
        
        if (filtered.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvGuards.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvGuards.visibility = View.VISIBLE
        }
        
        adapter.updateList(ArrayList(filtered))
    }

    private fun fetchGuards() {
        db.collection(DatabaseTableName.SECURITY_GUARDS)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                allGuards.clear()
                if (value != null) {
                    for (doc in value) {
                        val guard = doc.toObject(SecurityGuard::class.java)
                        allGuards.add(guard)
                    }
                }
                val currentFilter = idToFilterName(binding.chipGroupFilters.checkedChipId)
                val currentQuery = binding.etSearch.text.toString()
                filterList(currentQuery, currentFilter)
            }
    }

    private fun showDeleteDialog(guard: SecurityGuard) {
        val dialog = DeleteConfirmationDialogFragment.newInstance(
            title = "Delete Guard?",
            message = "Are you sure you want to remove ${guard.name}? This process is irreversible.",
            onConfirm = {
                deleteGuard(guard)
            }
        )
        dialog.show(supportFragmentManager, "DeleteGuardDialog")
    }

    private fun deleteGuard(guard: SecurityGuard) {
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

        paint.textSize = 18f
        canvas.drawText("Security Guards Report", 100f, 50f, paint)
        
        paint.textSize = 12f
        var yPosition = 100f
        
        for (item in allGuards) {
            canvas.drawText("${item.name} (${item.shift}) - ${item.email}", 50f, yPosition, paint)
            yPosition += 30f
            if (yPosition > 800) break
        }

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