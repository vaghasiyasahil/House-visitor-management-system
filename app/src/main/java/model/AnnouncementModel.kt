package model

import com.google.firebase.Timestamp
import types.AnnouncementsType
import types.VisitorStatus
import java.io.Serializable

data class AnnouncementModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var category: AnnouncementsType = AnnouncementsType.Other,
    var author: String = "",
    var date: String = "",
    var time: String = "",
    var timestamp: Timestamp? = null
) : Serializable {

    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "category" to category,
            "author" to author,
            "date" to date,
            "time" to time,
            "timestamp" to timestamp
        )
    }

    companion object {
        fun fromHashMap(map: Map<String, Any?>): AnnouncementModel {
            return AnnouncementModel(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                category = AnnouncementsType.fromString(map["category"] as? String?:"other")?: AnnouncementsType.Other,
                author = map["author"] as? String ?: "",
                date = map["date"] as? String ?: "",
                time = map["time"] as? String ?: "",
                timestamp = map["timestamp"] as? Timestamp
            )
        }
    }
}
