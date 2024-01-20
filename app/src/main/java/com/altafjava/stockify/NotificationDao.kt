package com.altafjava.stockify

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_data ORDER BY id DESC")
    fun getAll(): List<NotificationData>

    @Insert
    fun insert(notificationData: NotificationData)
}