package com.example.housevisitormanagementsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.housevisitormanagementsystem.databinding.ActivityViewQrCodeBinding
import com.example.housevisitormanagementsystem.databinding.ItemQrCodeBinding

class ViewQrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewQrCodeBinding

    data class VisitorPass(
        val name: String,
        val purpose: String,
        val validity: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val dummyData = listOf(
            VisitorPass("Guest: Alice Johnson", "Personal Visit", "Valid until: 12 Jan, 2024"),
            VisitorPass("Guest: Mark Smith", "Delivery", "Valid until: 10 Jan, 2024"),
            VisitorPass("Guest: Sarah Wilson", "Maintenance", "Valid until: 15 Jan, 2024"),
            VisitorPass("Guest: James Brown", "Personal Visit", "Valid until: 20 Jan, 2024")
        )

        binding.rvQrCodes.layoutManager = LinearLayoutManager(this)
        binding.rvQrCodes.adapter = QrCodeAdapter(dummyData) { pass ->
            val intent = android.content.Intent(this, ViewQrCodeDetailsActivity::class.java).apply {
                putExtra("VISITOR_NAME", pass.name)
                putExtra("VISITOR_PURPOSE", pass.purpose)
                putExtra("VISITOR_VALIDITY", pass.validity)
            }
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            
            // Set status bar color to match background
            window.statusBarColor = getColor(R.color.dark_navy_bg)
            
            insets
        }
    }

    class QrCodeAdapter(
        private val passes: List<VisitorPass>,
        private val onItemClick: (VisitorPass) -> Unit
    ) : RecyclerView.Adapter<QrCodeAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemQrCodeBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemQrCodeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val pass = passes[position]
            holder.binding.tvVisitorName.text = pass.name
            holder.binding.tvVisitorPurpose.text = pass.purpose
            holder.binding.tvValidity.text = pass.validity

            holder.itemView.setOnClickListener {
                onItemClick(pass)
            }
        }

        override fun getItemCount() = passes.size
    }
}