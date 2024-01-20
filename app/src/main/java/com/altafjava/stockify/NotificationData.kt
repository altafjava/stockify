package com.altafjava.stockify

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_data")
data class NotificationData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val title: String,
    val text: String,
    val timestamp: String
)