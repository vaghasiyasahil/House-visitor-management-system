package model

import com.google.firebase.Timestamp

data class QrCodeModel(
    var id: String = "",
    var visitorId: String = "",
    var visitorName: String = "",
    var visitorMobile: String = "",
    var houseNo: String = "",
    var purpose: String = "",
    var qrData: String = "",
    var createdAt: Timestamp? = null,
    var expiresAt: Timestamp? = null,
    var isUsed: Boolean = false,
    var houseOwnerEmail: String = "",
    var visitorPhoto: String = ""
) {
    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "visitorId" to visitorId,
            "visitorName" to visitorName,
            "visitorMobile" to visitorMobile,
            "houseNo" to houseNo,
            "purpose" to purpose,
            "qrData" to qrData,
            "createdAt" to createdAt,
            "expiresAt" to expiresAt,
            "isUsed" to isUsed,
            "houseOwnerEmail" to houseOwnerEmail,
            "visitorPhoto" to visitorPhoto
        )
    }

    companion object {
        fun fromHashMap(map: Map<String, Any?>): QrCodeModel {
            return QrCodeModel(
                id = map["id"] as? String ?: "",
                visitorId = map["visitorId"] as? String ?: "",
                visitorName = map["visitorName"] as? String ?: "",
                visitorMobile = map["visitorMobile"] as? String ?: "",
                houseNo = map["houseNo"] as? String ?: "",
                purpose = map["purpose"] as? String ?: "",
                qrData = map["qrData"] as? String ?: "",
                createdAt = map["createdAt"] as? Timestamp,
                expiresAt = map["expiresAt"] as? Timestamp,
                isUsed = map["isUsed"] as? Boolean ?: false,
                houseOwnerEmail = map["houseOwnerEmail"] as? String ?: "",
                visitorPhoto = map["visitorPhoto"] as? String ?: ""
            )
        }
    }
}
