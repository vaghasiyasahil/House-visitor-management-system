package types

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
    }
}