package model

import java.io.Serializable

data class AnnouncementModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var category: String = "",
    var author: String = "",
    var date: String = "",
    var time: String = "",
    var timestamp: String = "", // For display like "2 hours ago" or just use date/time
    var iconRes: Int = 0,
    var showReadMore: Boolean = false,
    var borderColor: Int = 0,
    var iconColor: Int = 0
) : Serializable
