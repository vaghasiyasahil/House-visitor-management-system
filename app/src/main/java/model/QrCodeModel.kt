package model

import com.google.firebase.Timestamp

data class QrCodeModel(
    var id: String = "",
    var visitorId: String = "",
    var qrData: String = "",
    var createdAt: Timestamp? = null,
    var expiresAt: Timestamp? = null,
    var isUsed: Boolean = false,
    var houseOwnerEmail: String = ""
) {
    fun toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "visitorId" to visitorId,
            "qrData" to qrData,
            "createdAt" to createdAt,
            "expiresAt" to expiresAt,
            "isUsed" to isUsed,
            "houseOwnerEmail" to houseOwnerEmail
        )
    }

    companion object {
        fun fromHashMap(map: Map<String, Any?>): QrCodeModel {
            return QrCodeModel(
                id = map["id"] as? String ?: "",
                visitorId = map["visitorId"] as? String ?: "",
                qrData = map["qrData"] as? String ?: "",
                createdAt = map["createdAt"] as? Timestamp,
                expiresAt = map["expiresAt"] as? Timestamp,
                isUsed = map["isUsed"] as? Boolean ?: false,
                houseOwnerEmail = map["houseOwnerEmail"] as? String ?: ""
            )
        }
    }
}
