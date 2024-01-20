package com.altafjava.stockify

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NotificationListener : NotificationListenerService() {

    data class NotificationData(
        val packageName: String,
        val title: String,
        val text: String,
        val timestamp: String
    )

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
            val newData = notificationData.value?.toMutableList() ?: mutableListOf()
            newData.add(0, NotificationData(packageName, title, text, timestamp))
            notificationData.postValue(newData)
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