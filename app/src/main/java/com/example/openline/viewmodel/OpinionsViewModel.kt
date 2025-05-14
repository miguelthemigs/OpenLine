package com.example.openline.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.openline.model.Opinion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.UUID


class OpinionsViewModel : ViewModel() {
    companion object {
        private const val TAG      = "OpinionsViewModel"
        //private const val BASE_URL = "http://10.0.2.2:5000"
        private const val BASE_URL = "http://android.openline.marijndemul.nl"
    }


    /**
     * Fetches a single opinion by ID, then calls onResult with the parsed Opinion or null.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getOpinion(opinionId: String, onResult: (Opinion?) -> Unit) {
        viewModelScope.launch {
            val op = withContext(Dispatchers.IO) {
                try {
                    val url = URL("$BASE_URL/opinions/$opinionId")
                    Log.d(TAG, "GET $url")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod  = "GET"
                        connectTimeout = 5_000
                        readTimeout    = 5_000
                    }

                    if (conn.responseCode != 200) {
                        Log.e(TAG, "Non-200 response: ${conn.responseCode}")
                        return@withContext null
                    }

                    val raw = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    Log.d(TAG, "Raw opinion JSON: $raw")

                    // **use camelCase keys here**
                    val json = JSONObject(raw)
                    val rawTs = json.getString("timestamp")
// parse the offset‐aware timestamp and drop the zone:
                    val timestamp = OffsetDateTime.parse(rawTs).toLocalDateTime()
                    Opinion(
                        id        = UUID.fromString(json.getString("id")),
                        itemId    = UUID.fromString(json.getString("itemId")),
                        userId    = UUID.fromString(json.getString("userId")),
                        text      = json.getString("text"),
                        timestamp = timestamp,
                        likes     = json.optInt("likes", 0),
                        dislikes  = json.optInt("dislikes", 0)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching opinion", e)
                    null
                }
            }
            Log.d(TAG, "Emitting opinion = $op")
            onResult(op)
        }
    }

    /**
     * Posts a reply to an opinion.
     * Calls POST /opinions/{opinionId}/reply with { "text": "..." }.
     */
    fun postReply(opinionId: String, text: String) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val url = URL("$BASE_URL/opinions/$opinionId/reply")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod  = "POST"
                        connectTimeout = 5_000
                        readTimeout    = 5_000
                        doOutput       = true
                        setRequestProperty("Content-Type", "application/json; utf-8")
                    }
                    val payload = JSONObject().put("text", text).toString()
                    OutputStreamWriter(conn.outputStream).use { it.write(payload) }
                    val code = conn.responseCode
                    Log.d(TAG, "postReply → POST $url → payload=$payload → code=$code")
                    code in 200..299
                } catch (e: Exception) {
                    Log.e(TAG, "postReply error", e)
                    false
                }
            }
            if (success) Log.d(TAG, "Reply posted successfully")
            else       Log.e(TAG, "Failed to post reply")
        }
    }

    /**
     * Sends a like/dislike reaction to an opinion.
     * Calls POST /opinions/{opinionId}/react with { "like": true/false }.
     */
    fun reactToOpinion(opinionId: String, like: Boolean) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val url = URL("$BASE_URL/opinions/$opinionId/react")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod  = "POST"
                        connectTimeout = 5_000
                        readTimeout    = 5_000
                        doOutput       = true
                        setRequestProperty("Content-Type", "application/json; utf-8")
                    }
                    val payload = JSONObject().put("like", like).toString()
                    OutputStreamWriter(conn.outputStream).use { it.write(payload) }
                    val code = conn.responseCode
                    Log.d(TAG, "reactToOpinion → POST $url → payload=$payload → code=$code")
                    code in 200..299
                } catch (e: Exception) {
                    Log.e(TAG, "reactToOpinion error", e)
                    false
                }
            }
            if (success) Log.d(TAG, "Reaction posted successfully")
            else       Log.e(TAG, "Failed to post reaction")
        }
    }
}
