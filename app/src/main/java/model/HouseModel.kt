package model

import com.google.firebase.Timestamp
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HouseModel(
    var id: String = "",
    var houseNo: String = "",
    var ownerName: String = "",
    var ownerEmail: String = "",
    var password: String = "",
    var members: Int = 0,
    var createdAt : Timestamp? = null,
    var updatedAt : Timestamp? = null
) : Parcelable {

    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "houseNo" to houseNo,
            "ownerName" to ownerName,
            "ownerEmail" to ownerEmail,
            "password" to password,
            "members" to members,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromHashMap(map: Map<String, Any?>): HouseModel {
            return HouseModel(
                id = map["id"] as? String ?: "",
                houseNo = map["houseNo"] as? String ?: "",
                ownerName = map["ownerName"] as? String ?: "",
                ownerEmail = map["ownerEmail"] as? String ?: "",
                password = map["password"] as? String ?: "",
                members = (map["members"] as? Number)?.toInt() ?: 0,
                createdAt = map["createdAt"] as? Timestamp,
                updatedAt = map["updatedAt"] as? Timestamp,
            )
        }
    }
}
