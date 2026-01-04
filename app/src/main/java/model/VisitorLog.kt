package model

data class VisitorLog(
    var id: String = "",
    var name: String = "",
    var mobile: String = "",
    var type: String = "", // Guest, Delivery, Maintenance
    var unit: String = "",
    var status: String = "Inside", // Inside, Checked Out
    var entryTime: String = "",
    var exitTime: String? = null,
    var visitorPhoto: String? = null, // Base64 or URL
    var vehiclePlate: String? = null,
    var entryGate: String? = null,
    var ownerName: String? = null,
    var isActive: Boolean = true
)
