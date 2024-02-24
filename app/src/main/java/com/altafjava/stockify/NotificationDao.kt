package com.altafjava.stockify

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_data ORDER BY id DESC")
    fun getAll(): LiveData<List<NotificationData>>

    @Insert
    fun insert(notificationData: NotificationData)
}