package model

import java.io.Serializable

data class House(
    var id: String = "",
    var houseNo: String = "",
    var ownerName: String = "",
    var ownerEmail: String = "",
    var password: String = "",
    var members: Int = 0
) : Serializable
