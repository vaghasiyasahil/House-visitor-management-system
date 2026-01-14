package types

import com.example.housevisitormanagementsystem.R

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

        fun getColor(status: VisitorStatus): Int {
            return when (status) {
                Pending -> R.color.status_yellow
                Inside -> R.color.status_green
                Expired -> R.color.status_grey
                CheckedOut -> R.color.red
            }
        }
    }

}