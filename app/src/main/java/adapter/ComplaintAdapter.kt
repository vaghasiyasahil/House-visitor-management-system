package adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.housevisitormanagementsystem.R
import model.Complaint

class ComplaintAdapter(
    private val context: Context,
    private val complaints: List<Complaint>
) : RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder>() {

    class ComplaintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivComplaintIcon)
        val iconBg: View = itemView.findViewById(R.id.iconBg)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvMetaData: TextView = itemView.findViewById(R.id.tvMetaData)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val ivStatusIcon: ImageView = itemView.findViewById(R.id.ivStatusIcon)
        val statusBadge: View = itemView.findViewById(R.id.statusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_complaint, parent, false)
        return ComplaintViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        val complaint = complaints[position]

        holder.tvTitle.text = complaint.title
        holder.tvMetaData.text = "${complaint.houseNo}  •  ${complaint.timestamp}"
        holder.tvDescription.text = complaint.description
        holder.ivIcon.setImageResource(complaint.iconRes)
        
        // Icon Styling
        holder.iconBg.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, complaint.iconBgColor))
        holder.ivIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, complaint.iconTint))

        // Status Styling
        holder.tvStatus.text = complaint.status
        when (complaint.status) {
            "Pending" -> {
                holder.statusBadge.setBackgroundResource(R.drawable.bg_status_pending)
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.orange))
                holder.ivStatusIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.orange))
                holder.ivStatusIcon.setImageResource(android.R.drawable.arrow_down_float)
            }
            "In Progress" -> {
                holder.statusBadge.setBackgroundResource(R.drawable.bg_status_in_progress)
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.bright_azure_blue))
                holder.ivStatusIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.bright_azure_blue))
                holder.ivStatusIcon.setImageResource(android.R.drawable.arrow_down_float)
            }
            "Resolved" -> {
                holder.statusBadge.setBackgroundResource(R.drawable.bg_status_resolved)
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_green))
                holder.ivStatusIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_green))
                holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle)
            }
        }

        holder.statusBadge.setOnClickListener {
            showUpdateStatusDialog(complaint)
        }
    }

    private fun showUpdateStatusDialog(complaint: Complaint) {
        val dialog = android.app.Dialog(context)
        dialog.setContentView(R.layout.dialog_update_complaint_status)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Remove fixed width to allow FrameLayout padding to act as margin
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvDetail = dialog.findViewById<TextView>(R.id.tvComplaintDetail)
        tvDetail.text = "#CMP-${complaint.id} • ${complaint.title}"

        val rbPending = dialog.findViewById<android.widget.RadioButton>(R.id.rbPending)
        val rbInProgress = dialog.findViewById<android.widget.RadioButton>(R.id.rbInProgress)
        val rbResolved = dialog.findViewById<android.widget.RadioButton>(R.id.rbResolved)

        val optionPending = dialog.findViewById<android.view.View>(R.id.optionPending)
        val optionInProgress = dialog.findViewById<android.view.View>(R.id.optionInProgress)
        val optionResolved = dialog.findViewById<android.view.View>(R.id.optionResolved)

        // Initial Selection
        when (complaint.status) {
            "Pending" -> {
                rbPending.isChecked = true
                optionPending.isSelected = true
            }
            "In Progress" -> {
                rbInProgress.isChecked = true
                optionInProgress.isSelected = true
            }
            "Resolved" -> {
                rbResolved.isChecked = true
                optionResolved.isSelected = true
            }
        }

        // Click Listeners for Options
        optionPending.setOnClickListener {
            rbPending.isChecked = true
            rbInProgress.isChecked = false
            rbResolved.isChecked = false
            optionPending.isSelected = true
            optionInProgress.isSelected = false
            optionResolved.isSelected = false
        }

        optionInProgress.setOnClickListener {
            rbPending.isChecked = false
            rbInProgress.isChecked = true
            rbResolved.isChecked = false
            optionPending.isSelected = false
            optionInProgress.isSelected = true
            optionResolved.isSelected = false
        }

        optionResolved.setOnClickListener {
            rbPending.isChecked = false
            rbInProgress.isChecked = false
            rbResolved.isChecked = true
            optionPending.isSelected = false
            optionInProgress.isSelected = false
            optionResolved.isSelected = true
        }

        dialog.findViewById<android.view.View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<android.view.View>(R.id.btnUpdateStatus).setOnClickListener {
            val newStatus = when {
                rbPending.isChecked -> "Pending"
                rbInProgress.isChecked -> "In Progress"
                else -> "Resolved"
            }
            android.widget.Toast.makeText(context, "Status updated to $newStatus", android.widget.Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getItemCount(): Int = complaints.size
}
