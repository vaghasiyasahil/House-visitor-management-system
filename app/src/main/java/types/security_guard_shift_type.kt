package types

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
    }
}