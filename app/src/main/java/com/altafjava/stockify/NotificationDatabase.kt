package com.altafjava.stockify

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [NotificationData::class], version = 1)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}