package com.example.openline.viewmodel
import android.content.ContentValues.TAG
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject  // <-- add this import

// Use your actual backend URL; on Android emulator 10.0.2.2 → localhost
private const val BASE_URL = "https://8401-217-105-38-113.ngrok-free.app"
private const val tag = "UsersViewModel"

suspend fun fetchUserName(userId: String): String? {
    return try {
        val url = URL("$BASE_URL/users/$userId/name")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod  = "GET"
            connectTimeout = 5_000
            readTimeout    = 5_000
        }

        val code = conn.responseCode
        Log.d(tag, "URL: $url → responseCode = $code")

        if (code == 200) {
            // Read the entire response body into a String
            val rawBody = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            Log.d(tag, "Raw body: $rawBody")

            // Parse it as JSON and extract the "name" field
            JSONObject(rawBody).optString("name", null)
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e(tag, "fetchUserName error", e)
        null
    }
}

