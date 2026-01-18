package types
import com.example.housevisitormanagementsystem.R

enum class AnnouncementsType {
    Urgent,
    Maintenance,
    Event,
    Other;

    companion object {
        fun fromString(value: String): AnnouncementsType? {
            return when (value.lowercase().replace(" ","").replace("_","").replace("-","")) {
                "urgent" -> Urgent
                "maintenance" -> Maintenance
                "event" -> Event
                "other" -> Other
                else -> null
            }
        }

        fun getColor(status: AnnouncementsType): Int {
            return when (status) {
                Urgent -> R.color.red
                Maintenance -> R.color.orange
                Event -> R.color.bright_azure_blue
                Other -> R.color.teal
            }
        }

        fun getBackground(status: AnnouncementsType): Int {
            return when (status) {
                Urgent -> R.drawable.bg_badge_urgent
                Maintenance -> R.drawable.bg_badge_urgent
                Event -> R.drawable.bg_badge_event
                Other -> R.drawable.bg_badge_green
            }
        }

        fun getIcon(status: AnnouncementsType): Int {
            return when (status) {
                Urgent -> R.drawable.ic_warning_triangle
                Maintenance -> R.drawable.ic_maintenance
                Event -> R.drawable.ic_calendar
                Other -> R.drawable.ic_announcement
            }
        }
    }
}