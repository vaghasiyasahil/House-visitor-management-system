package model

import types.ComplaintStatusType
import com.google.firebase.Timestamp
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ComplaintModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var createdHouseNo: String = "",
    var status: ComplaintStatusType = ComplaintStatusType.Pending,
    var createdAt : Timestamp? = null,
    var updatedAt : Timestamp? = null,
) : Parcelable {

    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "createdHouseNo" to createdHouseNo,
            "status" to status,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
        )
    }

    companion object {
        fun fromHashMap(map: Map<String, Any?>): ComplaintModel {
            return ComplaintModel(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                createdHouseNo = map["createdHouseNo"] as? String ?: "",
                status = ComplaintStatusType.fromString(map["status"] as? String?:"pending")?: ComplaintStatusType.Pending,
                createdAt = map["createdAt"] as? Timestamp,
                updatedAt = map["updatedAt"] as? Timestamp,
            )
        }
    }
}
