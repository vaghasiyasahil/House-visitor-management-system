package com.example.housevisitormanagementsystem

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.housevisitormanagementsystem.databinding.ActivityViewHouseBinding
import android.widget.Toast
import com.example.housevisitormanagementsystem.dialogs.DeleteConfirmationDialogFragment

class ViewHouseActivity : AppCompatActivity() {

    lateinit var binding: ActivityViewHouseBinding

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

        binding.btnBack.setOnClickListener {
            finish();
        }

        binding.btnGenerateReport.setOnClickListener {
            Toast.makeText(this, "Exporting to PDF...", Toast.LENGTH_SHORT).show()
        }

        binding.fabAddHouse.setOnClickListener {
            intent = Intent(this,AddHouseActivity::class.java)
            startActivity(intent)
        }

        binding.delete1.setOnClickListener {
            showDeleteDialog();
        }
        binding.delete2.setOnClickListener {
            showDeleteDialog();
        }
        binding.delete3.setOnClickListener {
            showDeleteDialog();
        }
        binding.delete4.setOnClickListener {
            showDeleteDialog();
        }
    }

    fun showDeleteDialog(){
        val dialog = DeleteConfirmationDialogFragment.newInstance(
            title = "Delete House?",
            message = "Are you sure you want to remove this house from the system? This process is irreversible.",
            onConfirm = {
                Toast.makeText(this, "House deleted successfully", Toast.LENGTH_SHORT).show()
            }
        )
        dialog.show(supportFragmentManager, "DeleteHouseDialog")
    }

}