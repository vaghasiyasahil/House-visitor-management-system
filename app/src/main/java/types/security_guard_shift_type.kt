package types

import com.example.housevisitormanagementsystem.R

enum class SecurityGuardShiftType {
    Morning,
    Night;

    companion object {
        fun fromString(value: String): SecurityGuardShiftType? {
            return when (value.lowercase().replace(" ","").replace("_","").replace("-","")) {
                "morning" -> Morning
                "night" -> Night
                else -> null
            }
        }

        fun getColor(status: SecurityGuardShiftType): Int {
            return when (status) {
                Morning -> R.color.badge_blue_text
                Night -> R.color.badge_red_text
            }
        }

    }
}