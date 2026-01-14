package utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeAndDateUtils{
    fun getTimeAgo(diffMillis: Long): String {
        val seconds = diffMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val months = days / 30
        val years = days / 365
        return when {
            seconds < 60 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 30 -> "${days}d ago"
            months < 12 -> "${months}mo ago"
            else -> "${years}y ago"
        }
    }

    fun formatFullDateTime(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val sdf = SimpleDateFormat(
            "dd MMMM yyyy 'at' HH:mm:ss 'UTC'XXX",
            Locale.ENGLISH
        )
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(timestamp.toDate())
    }

    fun formatShortDateTime(timestamp: Timestamp?): String {
        if (timestamp == null) return ""

        val sdf = SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.ENGLISH
        )
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(timestamp.toDate())
    }


}