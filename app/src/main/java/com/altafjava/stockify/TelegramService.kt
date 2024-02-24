package com.altafjava.stockify

import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class TelegramService {

    companion object {
        const val TOKEN = "6889174929:AAHfBgzLwFL2OK-bST6JsALh-bEMHUtzkGM"
        const val group_id = -1002117249896
    }

    fun sendMessage(text: String) {
        val url = "https://api.telegram.org/bot$TOKEN/sendMessage"
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("chat_id", group_id.toString())
            .add("text", text)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    println(response.body!!.string())
                }
            }
        })
    }
}