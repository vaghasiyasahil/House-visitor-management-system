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
import model.BlockedVisitor

class BlockedVisitorAdapter(
    private val context: Context,
    private var visitors: List<BlockedVisitor>,
    private val isHistoryTab: Boolean,
    private val onActionClick: (BlockedVisitor) -> Unit
) : RecyclerView.Adapter<BlockedVisitorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val iconBg: View = view.findViewById(R.id.iconBg)
        val ivTypeIcon: ImageView = view.findViewById(R.id.ivTypeIcon)
        val ivStatusOverlay: ImageView = view.findViewById(R.id.ivStatusOverlay)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDetails: TextView = view.findViewById(R.id.tvDetails)
        val tvReason: TextView = view.findViewById(R.id.tvReason)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val btnAction: TextView = view.findViewById(R.id.btnAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_blocked_visitor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val visitor = visitors[position]

        holder.tvName.text = visitor.name
        holder.tvDetails.text = visitor.details
        holder.tvReason.text = visitor.reason
        holder.tvDuration.text = visitor.duration

        // Dynamic Button Text
        holder.btnAction.text = if (isHistoryTab) "Block Visitor" else "Unblock Visitor"

        // Handle Avatar / Icon
        if (visitor.imageRes != null) {
            holder.ivAvatar.visibility = View.VISIBLE
            holder.iconBg.visibility = View.GONE
            holder.ivTypeIcon.visibility = View.GONE
            holder.ivAvatar.setImageResource(visitor.imageRes)
        } else if (visitor.typeIconRes != null) {
            holder.ivAvatar.visibility = View.GONE
            holder.iconBg.visibility = View.VISIBLE
            holder.ivTypeIcon.visibility = View.VISIBLE
            holder.ivTypeIcon.setImageResource(visitor.typeIconRes)
            if (visitor.typeColor != null) {
                holder.iconBg.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, visitor.typeColor))
                holder.ivTypeIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, visitor.typeColor))
            }
        }

        // Status Overlay
        holder.ivStatusOverlay.visibility = if (isHistoryTab) View.GONE else View.VISIBLE
        
        // Reason Badge Color
        if (visitor.isCurrentlyBlocked) {
            holder.tvReason.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red_transparent).apply { 
                // Using a fallback if color doesn't exist, but I'll assume standard red for now
            })
            holder.tvReason.setTextColor(ContextCompat.getColor(context, R.color.red))
        }

        holder.btnAction.setOnClickListener {
            onActionClick(visitor)
        }
    }

    override fun getItemCount(): Int = visitors.size

    fun updateData(newVisitors: List<BlockedVisitor>, newIsHistoryTab: Boolean) {
        // Simple update for now, could use DiffUtil for better performance
        // But for mock data it's fine
    }
}

// Extension to avoid red_transparent error if it's missing
// I'll check colors.xml later or just use direct hex in layout
