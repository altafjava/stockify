package com.altafjava.stockify

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NotificationListener : NotificationListenerService() {
    private lateinit var notificationDao: NotificationDao
    override fun onCreate() {
//        super.onCreate()
//        val db = Room.databaseBuilder(
//            applicationContext, NotificationDatabase::class.java, "notification-database"
//        )
//            .allowMainThreadQueries()
//            .build()
//        notificationDao = db.notificationDao()
        super.onCreate()
        val db = Room.databaseBuilder(
            applicationContext,
            NotificationDatabase::class.java, "notification-database"
        ).build()
        CoroutineScope(Dispatchers.IO).launch {
            notificationDao = db.notificationDao()
        }
    }

    companion object {
        val notificationData = MutableLiveData<List<NotificationData>>(emptyList())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
        val timestamp = getCurrentTimeInIST()
        if (title != null && text != null) {
            val notificationData = NotificationData(
                packageName = packageName, title = title, text = text, timestamp = timestamp
            )
//            notificationDao.insert(notificationData)
            CoroutineScope(Dispatchers.IO).launch {
                notificationDao.insert(notificationData)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // This is called when a notification is dismissed
    }

    private fun getCurrentTimeInIST(): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val dateFormat = SimpleDateFormat("hh:mm:ss a  dd-MM-yyyy", Locale.ENGLISH)
        dateFormat.timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        return dateFormat.format(calendar.time)
    }
}