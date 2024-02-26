package com.altafjava.stockify

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class NotificationListener : NotificationListenerService() {
    private lateinit var notificationDao: NotificationDao
    private lateinit var telegramService: TelegramService
    private lateinit var googleSheetService: GoogleSheetService
    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(
            applicationContext,
            NotificationDatabase::class.java, "notification-database"
        ).build()
        CoroutineScope(Dispatchers.IO).launch {
            notificationDao = db.notificationDao()
        }
        telegramService = TelegramService()
        googleSheetService = GoogleSheetService(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val packageName = sbn.packageName
        val extras = sbn.notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
        val timestamp = getCurrentTimeInIST()
        var shouldProcessNotification = false
        if (packageName.startsWith("com.wave.pl") && text != null && text.contains("EQUITIES")) {
            shouldProcessNotification = true
        }
        if (shouldProcessNotification) {
            CoroutineScope(Dispatchers.IO).launch {
                telegramService.sendMessage("$text")
            }
            if (text != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val dataPartRange = "Sheet3!B2:G2"
                    val formulaPartRange = "Sheet3!H2"
                    val dataPart = createDataPart(text)
                    val headers = listOf(
                        "ID",
                        "Recommendation On",
                        "Valid Till",
                        "Symbol",
                        "Buy Price",
                        "Target",
                        "Stop Loss",
                        "LTP",
                        "Amount",
                        "Quantity",
                        "Profit/Loss",
                        "Cur Status %",
                        "Trade Status",
                        "Target/StopLoss"
                    )
                    val formulaPart = createFormulaPart(headers, 2)
                    val sheetId = 2053744588
                    googleSheetService.insertRow(sheetId, 1)
                    googleSheetService.writeRow(dataPartRange, dataPart)
                    googleSheetService.updateRow(formulaPartRange, formulaPart)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    val notificationData = NotificationData(
                        packageName = packageName,
                        title = title ?: "",
                        text = text,
                        timestamp = timestamp
                    )
                    notificationDao.insert(notificationData)
                }
            }
        }
    }

    private fun createDataPart(text: String): List<Any> {
        val symbolRegex = "NSE EQUITIES (.*?) EQ".toRegex()
        val symbol = "NSE:" + (symbolRegex.find(text)?.groupValues?.get(1) ?: "")
        val buyPriceRegex = "@ (.*?);".toRegex()
        val buyPrice = buyPriceRegex.find(text)?.groupValues?.get(1)?.toDouble() ?: 0.0
        val targetRegex = "Target of (.*?);".toRegex()
        val target = targetRegex.find(text)?.groupValues?.get(1)?.toDouble() ?: 0.0
        val stopLossRegex = "Stop Loss @ (.*?)$".toRegex()
        val stopLoss = stopLossRegex.find(text)?.groupValues?.get(1)?.toDouble() ?: 0.0
        val formatter = DateTimeFormatter.ofPattern("yy-MM-dd hh:mm a")
        val recommendationOn = LocalDateTime.now().format(formatter)
        val validTill = "26-02-24"
        return listOfNotNull(recommendationOn, validTill, symbol, buyPrice, target, stopLoss)
    }

    private fun createFormulaPart(headers: List<String>, row: Int): List<Any> {
        val ltpColumnLetter = columnToLetter(headers.indexOf("LTP") + 1)
        val quantityColumnLetter = columnToLetter(headers.indexOf("Quantity") + 1)
        val symbolColumnLetter = columnToLetter(headers.indexOf("Symbol") + 1)
        val amountColumnLetter = columnToLetter(headers.indexOf("Amount") + 1)
        val buyPriceColumnLetter = columnToLetter(headers.indexOf("Buy Price") + 1)
        val targetPriceColumnLetter = columnToLetter(headers.indexOf("Target") + 1)
        val stopLossPriceColumnLetter = columnToLetter(headers.indexOf("Stop Loss") + 1)

        val ltp = "=GOOGLEFINANCE($symbolColumnLetter$row, \"price\")"
        val amount = 10000
        val quantity = "=$amountColumnLetter$row/$buyPriceColumnLetter$row"
        val profitLoss =
            "=ROUND(($ltpColumnLetter$row - $buyPriceColumnLetter$row) * $quantityColumnLetter$row, 2)"
        val curStatus =
            "=ROUND((($ltpColumnLetter$row - $buyPriceColumnLetter$row) / ($targetPriceColumnLetter$row - $buyPriceColumnLetter$row)) * 100, 2)"
        val tradeStatus =
            "=IF($ltpColumnLetter$row <= $stopLossPriceColumnLetter$row, \"StopLoss\", IF($ltpColumnLetter$row >= $targetPriceColumnLetter$row, \"Target\", \"Progress\"))"
        return listOfNotNull(ltp, amount, quantity, profitLoss, curStatus, tradeStatus)
    }

    private fun columnToLetter(column: Int): String {
        var temp: Int
        var letter = ""
        var col = column
        while (col > 0) {
            temp = (col - 1) % 26
            letter = (temp + 65).toChar() + letter
            col = (col - temp - 1) / 26
        }
        return letter
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