package com.example.housevisitormanagementsystem

import SharedPreferencesService
import android.content.Intent
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
import com.yourpackage.services.FirestoreService
import constants.DatabaseTableName
import model.QrCodeModel
import java.text.SimpleDateFormat
import java.util.Locale

class ViewQrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewQrCodeBinding
    private var qrCodeList = mutableListOf<QrCodeModel>()
    private lateinit var adapter: QrCodeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityViewQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.rvQrCodes.layoutManager = LinearLayoutManager(this)
        adapter = QrCodeAdapter(qrCodeList) { qrCode ->
            val intent = Intent(this, ViewQrCodeDetailsActivity::class.java).apply {
                putExtra("QR_DATA", qrCode.qrData)
                putExtra("QR_CODE_ID", qrCode.id)
                putExtra("VISITOR_NAME", qrCode.visitorName)
                putExtra("VISITOR_PURPOSE", qrCode.purpose)
                putExtra("VISITOR_MOBILE", qrCode.visitorMobile)
                putExtra("VISITOR_PHOTO", qrCode.visitorPhoto)
                putExtra("HOUSE_NO", qrCode.houseNo)
                putExtra("IS_USED", qrCode.isUsed)
                qrCode.expiresAt?.let {
                    val sdf = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.getDefault())
                    putExtra("VISITOR_VALIDITY", "Valid until: ${sdf.format(it.toDate())}")
                }
            }
            startActivity(intent)
        }
        binding.rvQrCodes.adapter = adapter

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            window.statusBarColor = getColor(R.color.dark_navy_bg)
            insets
        }

        fetchQrCodes()
    }

    private fun fetchQrCodes() {
        val userEmail = SharedPreferencesService.getUserEmail()

        FirestoreService().getDocumentsWithEqualToConditions(
            DatabaseTableName.QR_CODES,
            mapOf("houseOwnerEmail" to userEmail)
        ) { snapshot ->
            qrCodeList.clear()
            snapshot?.documents?.forEach { doc ->
                val qrCode = QrCodeModel.fromHashMap(doc.data ?: emptyMap())
                qrCodeList.add(qrCode)
            }

            // Sort by creation date (newest first)
            qrCodeList.sortByDescending { it.createdAt?.toDate()?.time ?: 0 }

            adapter.notifyDataSetChanged()

            if (qrCodeList.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvQrCodes.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvQrCodes.visibility = View.VISIBLE
            }
        }
    }

    class QrCodeAdapter(
        private val qrCodes: List<QrCodeModel>,
        private val onItemClick: (QrCodeModel) -> Unit
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
            val qrCode = qrCodes[position]
            holder.binding.tvVisitorName.text = qrCode.visitorName
            holder.binding.tvVisitorPurpose.text = qrCode.purpose

            // Show validity or status
            val now = com.google.firebase.Timestamp.now()
            when {
                qrCode.isUsed -> {
                    holder.binding.tvValidity.text = "Used"
                    holder.binding.tvValidity.setTextColor(
                        holder.itemView.context.getColor(R.color.text_grey)
                    )
                }
                qrCode.expiresAt != null && qrCode.expiresAt!!.toDate().before(now.toDate()) -> {
                    holder.binding.tvValidity.text = "Expired"
                    holder.binding.tvValidity.setTextColor(
                        holder.itemView.context.getColor(R.color.red)
                    )
                }
                else -> {
                    qrCode.expiresAt?.let {
                        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        holder.binding.tvValidity.text = "Valid until: ${sdf.format(it.toDate())}"
                    }
                    holder.binding.tvValidity.setTextColor(
                        holder.itemView.context.getColor(R.color.text_grey)
                    )
                }
            }

            // Hide edit/delete buttons (not needed for this flow)
            holder.binding.btnEdit.visibility = View.GONE
            holder.binding.btnDelete.visibility = View.GONE

            holder.itemView.setOnClickListener {
                onItemClick(qrCode)
            }
        }

        override fun getItemCount() = qrCodes.size
    }
}