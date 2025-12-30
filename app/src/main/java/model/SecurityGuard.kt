package com.example.housevisitormanagementsystem.model

data class SecurityGuard(
    val name: String,
    val guardId: String,
    val shift: String, // "Morning Shift", "Night Shift", "Off Duty"
    val location: String, // "Block A", "Block B", etc.
    val status: String, // "Active", "Night", "Off"
    val profileImage: Int // Resource ID for now
)
