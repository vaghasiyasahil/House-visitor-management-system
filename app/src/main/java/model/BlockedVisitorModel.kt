package model

import java.io.Serializable

data class BlockedVisitorModel(
    val id: String,
    val name: String,
    val details: String, // Contact or Description (e.g., +1 (555) 019-2834 or Service Provider)
    val reason: String,  // Construction, Vacation Mode, Private Matter
    val duration: String, // Until Oct 15, Ends Nov 01, Indefinite
    val imageRes: Int?,
    val typeIconRes: Int?,
    val typeColor: Int?,
    val isCurrentlyBlocked: Boolean
) : Serializable
