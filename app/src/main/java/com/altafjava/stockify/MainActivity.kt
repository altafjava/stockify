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
import com.altafjava.stockify.ui.theme.StockifyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNotificationAccessPermission()
        setContent {
            StockifyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val notificationData =
                        remember { mutableStateListOf<NotificationListener.NotificationData>() }
                    NotificationListener.notificationData.observe(this) { data ->
                        notificationData.clear()
                        notificationData.addAll(data)
                    }
                    NotificationList(notificationData)
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
fun NotificationList(data: List<NotificationListener.NotificationData>) {
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