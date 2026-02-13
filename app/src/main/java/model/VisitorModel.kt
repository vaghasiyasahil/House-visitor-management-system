package model

import com.google.firebase.Timestamp
import types.VisitorStatus

data class VisitorModel(
    var id: String = "",
    var name: String = "",
    var mobile: String = "",
    var status: VisitorStatus = VisitorStatus.Pending,
    var entryTime: Timestamp? = null,
    var exitTime: Timestamp? = null,
    var visitorPhoto: String = "",
    var houseNo: String = "",
    var purpose: String = "",
    var houseOwnerFcmToken: String = "",
    var securityGuardFcmToken: String = "",
) {

    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "mobile" to mobile,
            "status" to status.name,
            "entryTime" to entryTime,
            "exitTime" to exitTime,
            "visitorPhoto" to visitorPhoto,
            "houseNo" to houseNo,
            "purpose" to purpose,
            "houseOwnerFcmToken" to houseOwnerFcmToken,
            "securityGuardFcmToken" to securityGuardFcmToken,
        )
    }

    companion object {
        fun fromHashMap(map: Map<String, Any?>): VisitorModel {
            return VisitorModel(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                mobile = map["mobile"] as? String ?: "",
                status = VisitorStatus.fromString(map["status"] as? String?:"pending")?:VisitorStatus.Pending,
                entryTime = map["entryTime"] as? Timestamp,
                exitTime = map["exitTime"] as? Timestamp,
                visitorPhoto = map["visitorPhoto"] as? String ?: "",
                houseNo = map["houseNo"] as? String ?: "",
                purpose = map["purpose"] as? String?:"",
                houseOwnerFcmToken = map["houseOwnerFcmToken"] as? String?:"",
                securityGuardFcmToken = map["securityGuardFcmToken"] as? String?:""
            )
        }
    }
}
