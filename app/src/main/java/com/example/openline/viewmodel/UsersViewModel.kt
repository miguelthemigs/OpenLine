package com.example.openline.viewmodel

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private const val BASE_URL = "http://10.0.2.2:5000"
// Use your actual backend URL; on Android emulator 10.0.2.2 → localhost

suspend fun fetchUserName(userId: String): String? {
    return try {
        // Build the URL to call, e.g. "http://10.0.2.2:5000/users/123/name"
        val url = URL("$BASE_URL/users/$userId/name")

        // Open an HTTP connection and configure it
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod  = "GET"
            connectTimeout = 5_000   // 5 seconds to establish TCP
            readTimeout    = 5_000   // 5 seconds to read data
        }

        // If the server said “200 OK”, read the response body…
        if (conn.responseCode == 200) {
            BufferedReader(InputStreamReader(conn.inputStream)).use { reader ->
                // reader.readText() returns the entire response as a String,
                // and .trim('"') strips the surrounding quotes that jsonify adds.
                reader.readText().trim('"')
            }
            // ← This String from reader.readText().trim('"') is *returned* here,
            //    because it’s the last expression inside the try block.
        } else {
            // If we got any other HTTP code, return null
            null
        }

    } catch (e: Exception) {
        // If anything threw an exception (network error, malformed URL, etc.)
        // we also return null
        null
    }
}

