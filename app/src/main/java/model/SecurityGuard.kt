package model

import java.io.Serializable

data class SecurityGuard(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var phoneNumber: String = "",
    var salary: String = "",
    var shift: String = ""
) : Serializable
