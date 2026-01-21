package model

import java.io.Serializable

data class HouseModel(
    var id: String = "",
    var houseNo: String = "",
    var ownerName: String = "",
    var ownerEmail: String = "",
    var password: String = "",
    var members: Int = 0
) : Serializable {

    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "houseNo" to houseNo,
            "ownerName" to ownerName,
            "ownerEmail" to ownerEmail,
            "password" to password,
            "members" to members
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
                members = (map["members"] as? Number)?.toInt() ?: 0
            )
        }
    }
}
