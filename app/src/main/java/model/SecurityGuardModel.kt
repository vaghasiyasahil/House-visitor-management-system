package model

import com.google.firebase.Timestamp
import types.SecurityGuardShiftType
import java.io.Serializable

data class SecurityGuardModel(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var phoneNumber: String = "",
    var salary: String = "",
    var image: String = "",
    var shift: SecurityGuardShiftType = SecurityGuardShiftType.Morning,
    var createdAt : Timestamp? = null,
    var updatedAt : Timestamp? = null
)  : Serializable {
    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "email" to email,
            "password" to password,
            "phoneNumber" to phoneNumber,
            "salary" to salary,
            "shift" to shift,
            "image" to image,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    companion object {
        fun fromHashMap(map: Map<String, Any?>): SecurityGuardModel {
            return SecurityGuardModel(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                email = map["email"] as? String ?: "",
                password = map["password"] as? String ?: "",
                phoneNumber = map["phoneNumber"] as? String ?: "",
                salary = map["salary"] as? String ?: "",
                image = map["image"] as? String ?: "",
                shift = SecurityGuardShiftType.fromString(map["shift"] as? String?:"morning")?: SecurityGuardShiftType.Morning,
                createdAt = map["createdAt"] as? Timestamp,
                updatedAt = map["updatedAt"] as? Timestamp,
            )
        }
    }
}
