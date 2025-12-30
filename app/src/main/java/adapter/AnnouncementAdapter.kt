package com.example.housevisitormanagementsystem.adapter

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
import com.example.housevisitormanagementsystem.model.Announcement

class AnnouncementAdapter(
    private val context: Context,
    private val announcements: List<Announcement>
) : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftBorder: View = itemView.findViewById(R.id.leftBorder)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvBadge: TextView = itemView.findViewById(R.id.tvBadge)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val tvAuthorInfo: TextView = itemView.findViewById(R.id.tvAuthorInfo)
        val tvReadMore: TextView = itemView.findViewById(R.id.tvReadMore)
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

        // Set icon
        holder.ivIcon.setImageResource(announcement.iconRes)
        if (announcement.iconColor != 0) {
            holder.ivIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, announcement.iconColor))
        }

        // Set border color
        if (announcement.borderColor != 0) {
            holder.leftBorder.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, announcement.borderColor))
        }

        // Handle badge
        when (announcement.category) {
            "Urgent" -> {
                holder.tvBadge.visibility = View.VISIBLE
                holder.tvBadge.text = "Urgent"
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_urgent)
                holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.red))
            }
            "Event" -> {
                holder.tvBadge.visibility = View.VISIBLE
                holder.tvBadge.text = "Event"
                holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_event)
                holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.bright_azure_blue))
            }
            else -> {
                holder.tvBadge.visibility = View.GONE
            }
        }

        // Handle Read More
        if (announcement.showReadMore) {
            holder.tvReadMore.visibility = View.VISIBLE
            holder.tvReadMore.setOnClickListener {
                val intent = Intent(context, AnnouncementDetailsActivity::class.java)
                intent.putExtra("TITLE", announcement.title)
                intent.putExtra("DESCRIPTION", announcement.description)
                intent.putExtra("CATEGORY", announcement.category)
                intent.putExtra("AUTHOR", announcement.author)
                intent.putExtra("TIMESTAMP", announcement.timestamp)
                context.startActivity(intent)
            }
        } else {
            holder.tvReadMore.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = announcements.size
}
