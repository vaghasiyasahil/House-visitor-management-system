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
import model.VisitorLog

class VisitorLogAdapter(
    private val context: Context,
    private val logs: List<VisitorLog>
) : RecyclerView.Adapter<VisitorLogAdapter.VisitorViewHolder>() {

    class VisitorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val iconBg: View = itemView.findViewById(R.id.iconBg)
        val ivTypeIcon: ImageView = itemView.findViewById(R.id.ivTypeIcon)
        val ivActiveIndicator: ImageView = itemView.findViewById(R.id.ivActiveIndicator)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDetails: TextView = itemView.findViewById(R.id.tvDetails)
        val tvStatusBadge: TextView = itemView.findViewById(R.id.tvStatusBadge)
        val tvEntryTime: TextView = itemView.findViewById(R.id.tvEntryTime)
        val tvExitTime: TextView = itemView.findViewById(R.id.tvExitTime)
        val btnCheckout: View = itemView.findViewById(R.id.btnCheckout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitorViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_visitor_log, parent, false)
        return VisitorViewHolder(view)
    }

    override fun onBindViewHolder(holder: VisitorViewHolder, position: Int) {
        val log = logs[position]

        holder.tvName.text = log.name
        holder.tvDetails.text = "${log.type} • ${log.unit}"
        
        // Handle Avatar / Icon
        if (log.avatarRes != null) {
            holder.ivAvatar.visibility = View.VISIBLE
            holder.iconBg.visibility = View.GONE
            holder.ivTypeIcon.visibility = View.GONE
            holder.ivAvatar.setImageResource(log.avatarRes)
        } else if (log.typeIconRes != null) {
            holder.ivAvatar.visibility = View.GONE
            holder.iconBg.visibility = View.VISIBLE
            holder.ivTypeIcon.visibility = View.VISIBLE
            holder.ivTypeIcon.setImageResource(log.typeIconRes)
            if (log.typeColor != null) {
                holder.iconBg.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, log.typeColor))
                holder.ivTypeIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, log.typeColor))
                // Apply a lighter background tint or use a circle_dark_bg as is
            }
        }

        // Active Indicator
        holder.ivActiveIndicator.visibility = if (log.isActive) View.VISIBLE else View.GONE

        // Status Badge
        holder.tvStatusBadge.text = log.status
        if (log.status == "Inside") {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_inside)
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_green))
            holder.tvEntryTime.text = log.entryTime
            holder.tvExitTime.visibility = View.GONE
            holder.btnCheckout.visibility = View.VISIBLE
            holder.btnCheckout.setOnClickListener {
                android.widget.Toast.makeText(context, "Checking out ${log.name}", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_checked_out)
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.tvEntryTime.text = "In: ${log.entryTime}"
            holder.tvExitTime.visibility = if (log.exitTime != null) View.VISIBLE else View.GONE
            holder.tvExitTime.text = "Out: ${log.exitTime}"
            holder.btnCheckout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = logs.size
}
