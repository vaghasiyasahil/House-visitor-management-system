package types

enum class VisitorStatus {
    Inside,
    CheckedOut;

    companion object {
        fun fromString(value: String): VisitorStatus? {
            return when (value.lowercase().replace(" ","").replace("_","").replace("-","")) {
                "inside" -> Inside
                "checkedout" -> CheckedOut
                else -> null
            }
        }
    }

}