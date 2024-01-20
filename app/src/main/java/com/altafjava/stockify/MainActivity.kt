package com.altafjava.stockify

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.altafjava.stockify.ui.theme.StockifyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var notificationDao: NotificationDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNotificationAccessPermission()
        val db = Room.databaseBuilder(
            applicationContext,
            NotificationDatabase::class.java, "notification-database"
        ).build()
        notificationDao = db.notificationDao()
        lateinit var notifications: List<NotificationData>
        CoroutineScope(Dispatchers.IO).launch {
            notifications = notificationDao.getAll()
            withContext(Dispatchers.Main) {
                setContent {
                    StockifyTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            val notificationData = remember { mutableStateListOf<NotificationData>() }
                            notificationData.addAll(notifications)
                            NotificationList(notificationData)
                        }
                    }
                }
            }
        }
    }

    private fun checkNotificationAccessPermission() {
        val enabledListeners =
            Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val packageName = packageName
        if (enabledListeners == null || !enabledListeners.contains(packageName)) {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        }
    }
}

@Composable
fun NotificationList(data: List<NotificationData>) {
    if (data.isEmpty()) {
        Text(text = "Currently there are no notifications.")
    } else {
        LazyColumn {
            items(data) { item ->
                Text(text = "Package: ${item.packageName}\nTitle: ${item.title}\nText: ${item.text}\nTime: ${item.timestamp}\n")
            }
        }
    }
}