package utils

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
}