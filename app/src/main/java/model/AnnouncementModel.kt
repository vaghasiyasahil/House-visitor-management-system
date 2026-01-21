package model

import com.google.firebase.Timestamp
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import types.AnnouncementsType
import types.UserRole

@Parcelize
data class AnnouncementModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var category: AnnouncementsType = AnnouncementsType.Other,
    var createdRole: UserRole = UserRole.Admin,
    var createdAt : Timestamp? = null,
    var updatedAt : Timestamp? = null
) : Parcelable {

    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "category" to category,
            "createdRole" to createdRole,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromHashMap(map: Map<String, Any?>): AnnouncementModel {
            return AnnouncementModel(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                category = AnnouncementsType.fromString(map["category"] as? String?:"other")?: AnnouncementsType.Other,
                createdRole= UserRole.fromString(map["createdRole"] as? String ?: "")?:UserRole.Admin,
                createdAt = map["createdAt"] as? Timestamp,
                updatedAt = map["updatedAt"] as? Timestamp,
            )
        }
    }
}
