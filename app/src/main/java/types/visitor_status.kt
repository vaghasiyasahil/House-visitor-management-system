package types

enum class VisitorStatus {
    Pending,
    Inside,
    Expired,
    CheckedOut;

    companion object {
        fun fromString(value: String): VisitorStatus? {
            return when (value.lowercase().replace(" ","").replace("_","").replace("-","")) {
                "pending" -> Pending
                "inside" -> Inside
                "expired" -> Expired
                "checkedout" -> CheckedOut
                else -> null
            }
        }
    }

}