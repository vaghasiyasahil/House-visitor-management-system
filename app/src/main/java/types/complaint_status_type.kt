package types

import com.example.housevisitormanagementsystem.R
import types.AnnouncementsType.Event
import types.AnnouncementsType.Maintenance
import types.AnnouncementsType.Other
import types.AnnouncementsType.Urgent

enum class ComplaintStatusType {
    Pending,
    InProgress,
    Resolved;

    companion object {
        fun fromString(value: String): ComplaintStatusType? {
            return when (value.lowercase().replace(" ","").replace("_","").replace("-","")) {
                "pending" -> Pending
                "inprogress" -> InProgress
                "resolved" -> Resolved
                else -> null
            }
        }

        fun getColor(status: ComplaintStatusType): Int {
            return when (status) {
                Pending -> R.color.orange
                InProgress -> R.color.bright_azure_blue
                Resolved -> R.color.status_green
            }
        }

        fun getBackground(status: ComplaintStatusType): Int {
            return when (status) {
                Pending -> R.drawable.bg_status_pending
                InProgress -> R.drawable.bg_status_in_progress
                Resolved -> R.drawable.bg_status_resolved
            }
        }

        fun getIcon(status: ComplaintStatusType): Int {
            return when (status) {
                Pending -> R.drawable.ic_pending_dots
                InProgress -> R.drawable.ic_sync
                Resolved -> R.drawable.ic_check_circle
            }
        }

    }
}