package adapter

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.housevisitormanagementsystem.AnnouncementDetailsActivity
import com.example.housevisitormanagementsystem.R
import model.AnnouncementModel

class AnnouncementAdapter(
    private val context: Context,
    private var announcements: List<AnnouncementModel>,
    private val onEditClick: (AnnouncementModel) -> Unit,
    private val onDeleteClick: (AnnouncementModel) -> Unit
) : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftBorder: View = itemView.findViewById(R.id.leftBorder)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvBadge: TextView = itemView.findViewById(R.id.tvBadge)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvAuthorInfo: TextView = itemView.findViewById(R.id.tvAuthorInfo)
        val tvReadMore: TextView = itemView.findViewById(R.id.tvReadMore)
        val btnOptions: ImageView = itemView.findViewById(R.id.btnOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_announcement, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcements[position]

        holder.tvTitle.text = announcement.title
        holder.tvDescription.text = announcement.description
        holder.tvAuthorInfo.text = "${announcement.author} • ${announcement.timestamp}"

        // Dynamic Styling based on Category
        val (iconRes, colorRes, badgeRes) = when (announcement.category) {
            "Urgent" -> Triple(R.drawable.ic_warning_triangle, R.color.red, R.drawable.bg_badge_urgent)
            "Maintenance" -> Triple(R.drawable.ic_maintenance, R.color.orange, R.drawable.bg_badge_urgent)
            "Event" -> Triple(R.drawable.ic_calendar, R.color.bright_azure_blue, R.drawable.bg_badge_event)
            else -> Triple(R.drawable.ic_announcement, R.color.bright_azure_blue, R.drawable.bg_badge_event)
        }

        holder.ivIcon.setImageResource(iconRes)
        holder.ivIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
        holder.leftBorder.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))

        holder.tvBadge.visibility = View.VISIBLE
        holder.tvBadge.text = announcement.category
        holder.tvBadge.setBackgroundResource(badgeRes)
        holder.tvBadge.setTextColor(ContextCompat.getColor(context, colorRes))

        // Handle Read More
        if (announcement.showReadMore) {
            holder.tvReadMore.visibility = View.VISIBLE
            holder.tvReadMore.setOnClickListener {
                openDetails(announcement)
            }
        } else {
            holder.tvReadMore.visibility = View.GONE
        }

        // Handle Options Menu
        holder.btnOptions.setOnClickListener { view ->
            val popup = android.widget.PopupMenu(context, view)
            // Reusing guard options menu which has action_edit and action_delete
            popup.menuInflater.inflate(R.menu.menu_guard_options, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        onEditClick(announcement)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(announcement)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun openDetails(announcement: AnnouncementModel) {
        val intent = Intent(context, AnnouncementDetailsActivity::class.java)
        intent.putExtra("TITLE", announcement.title)
        intent.putExtra("DESCRIPTION", announcement.description)
        intent.putExtra("CATEGORY", announcement.category)
        intent.putExtra("AUTHOR", announcement.author)
        intent.putExtra("TIMESTAMP", announcement.timestamp)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int = announcements.size

    fun updateList(newList: List<AnnouncementModel>) {
        announcements = newList
        notifyDataSetChanged()
    }
}
