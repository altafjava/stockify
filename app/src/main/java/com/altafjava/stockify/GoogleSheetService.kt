package com.altafjava.stockify

import android.content.Context
import android.util.Log
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.InsertDimensionRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class GoogleSheetService(context: Context) {
    private val spreadsheetId = "1unv-aisfMANO9zZafEfcdsAR6IxE7-lhy-LSzg6eGhM"
    private val sheetsService: Sheets

    init {
        val credentials: GoogleCredentials
        context.resources.openRawResource(R.raw.google_service_account)
            .use { inputStream ->
                credentials = GoogleCredentials.fromStream(inputStream)
            }
        val httpTransport = NetHttpTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        sheetsService =
            Sheets.Builder(httpTransport, jsonFactory, HttpCredentialsAdapter(credentials))
                .setApplicationName("Stockify")
                .build()
    }

    suspend fun insertRow(sheetId: Int, rowIndex: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val requests = listOf(
                Request().setInsertDimension(
                    InsertDimensionRequest()
                        .setRange(
                            DimensionRange()
                                .setSheetId(sheetId)
                                .setDimension("ROWS")
                                .setStartIndex(rowIndex)
                                .setEndIndex(rowIndex + 1)
                        )
                        .setInheritFromBefore(false)
                )
            )
            val body = BatchUpdateSpreadsheetRequest().setRequests(requests)
            sheetsService.spreadsheets().batchUpdate(spreadsheetId, body).execute()
            true
        } catch (e: IOException) {
            Log.e("GoogleSheetService", "Failed to insert row", e)
            false
        }
    }

    suspend fun writeRow(range: String, values: List<Any>): Boolean = withContext(
        Dispatchers.IO
    ) {
        try {
            val body = ValueRange().setValues(listOf(values))
            sheetsService.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute()
            true
        } catch (e: IOException) {
            Log.e("GoogleSheetService", "Failed to write row", e)
            false
        }
    }

    suspend fun updateRow(range: String, values: List<Any>): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = ValueRange().setValues(listOf(values))
            sheetsService.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute()
            true
        } catch (e: IOException) {
            Log.e("GoogleSheetService", "Failed to update row", e)
            false
        }
    }

}
