package model

data class Complaint(
    val id: String,
    val title: String,
    val houseNo: String,
    val timestamp: String,
    val description: String,
    val status: String, // Pending, In Progress, Resolved
    val iconRes: Int,
    val iconBgColor: Int,
    val iconTint: Int
)
