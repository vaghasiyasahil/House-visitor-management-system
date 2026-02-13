package types

enum class UserRole {
    Admin,
    HouseOwner,
    SecurityGuard;

    companion object {
        fun fromString(value: String): UserRole? {
            return when (value.lowercase().replace(" ","").replace("_","").replace("-","")) {
                "admin" -> Admin
                "houseowner" -> HouseOwner
                "securityguard" -> SecurityGuard
                else -> null
            }
        }
    }

    override fun toString(): String {
        return when (this) {
            Admin -> "Admin"
            HouseOwner -> "House Owner"
            SecurityGuard -> "Security Guard"
        }
    }

}