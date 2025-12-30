package com.example.housevisitormanagementsystem.model

data class Announcement(
    val title: String,
    val description: String,
    val category: String,
    val author: String,
    val timestamp: String,
    val iconRes: Int,
    val showReadMore: Boolean = false,
    val borderColor: Int = 0,
    val iconColor: Int = 0
)
