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
    }

    override fun getItemCount(): Int = complaints.size
}
