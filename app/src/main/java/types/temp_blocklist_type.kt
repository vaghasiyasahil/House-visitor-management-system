package types

enum class TempBlocklistType {
    Blocked,
    History;

    companion object {
        fun fromString(value: String): TempBlocklistType? {
            return when (value.lowercase().replace(" ","").replace("_","").replace("-","")) {
                "blocked" -> Blocked
                "history" -> History
                else -> null
            }
        }
    }
}