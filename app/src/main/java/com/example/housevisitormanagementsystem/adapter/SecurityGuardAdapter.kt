package com.example.housevisitormanagementsystem.adapter

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
import com.example.housevisitormanagementsystem.model.SecurityGuard

class SecurityGuardAdapter(
    private val context: Context,
    private val guardList: List<SecurityGuard>
) : RecyclerView.Adapter<SecurityGuardAdapter.GuardViewHolder>() {

    class GuardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile: ImageView = itemView.findViewById(R.id.ivProfile)
        val viewStatusDot: View = itemView.findViewById(R.id.viewStatusDot)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvId: TextView = itemView.findViewById(R.id.tvId)
        val tvShiftBadge: TextView = itemView.findViewById(R.id.tvShiftBadge)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        val btnMore: ImageView = itemView.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_security_guard, parent, false)
        return GuardViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuardViewHolder, position: Int) {
        val guard = guardList[position]

        holder.tvName.text = guard.name
        holder.tvId.text = "ID: ${guard.guardId}"
        holder.tvShiftBadge.text = guard.shift
        holder.tvLocation.text = "• ${guard.location}"
        
        // Load profile image (using a placeholder if 0 or invalid, assuming resource ID here)
        // In a real app with URLs, you'd use Glide/Picasso.
        if (guard.profileImage != 0) {
            holder.ivProfile.setImageResource(guard.profileImage)
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_launcher_background) // Fallback
        }

        // Status Logic
        when (guard.status) {
            "Active" -> {
                holder.viewStatusDot.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_green))
            }
            "Night" -> {
                holder.viewStatusDot.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_yellow))
            }
            "Off" -> {
                holder.viewStatusDot.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_grey))
            }
            else -> {
                holder.viewStatusDot.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_grey))
            }
        }

        // Shift Badge Logic
        when (guard.shift) {
            "Morning Shift" -> {
                holder.tvShiftBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.badge_blue_bg))
                holder.tvShiftBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_blue_text))
            }
            "Night Shift" -> {
                holder.tvShiftBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.badge_purple_bg))
                holder.tvShiftBadge.setTextColor(ContextCompat.getColor(context, R.color.badge_purple_text))
            }
            "Off Duty" -> {
                 holder.tvShiftBadge.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.card_dark_bg)) // Or a specific grey bg
                 holder.tvShiftBadge.setTextColor(ContextCompat.getColor(context, R.color.text_grey))
            }
        }
        
        // Handle "Off Duty" opacity/disabled look if needed
        if (guard.status == "Off") {
            holder.itemView.alpha = 0.7f
        } else {
            holder.itemView.alpha = 1.0f
        }
    }

    override fun getItemCount(): Int {
        return guardList.size
    }
}
