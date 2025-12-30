package model

data class VisitorLog(
    val id: String,
    val name: String,
    val type: String, // Guest, Delivery, Maintenance
    val unit: String,
    val status: String, // Inside, Checked Out
    val entryTime: String,
    val exitTime: String? = null,
    val avatarRes: Int? = null,
    val typeIconRes: Int? = null,
    val typeColor: Int? = null,
    val isActive: Boolean = false
)
