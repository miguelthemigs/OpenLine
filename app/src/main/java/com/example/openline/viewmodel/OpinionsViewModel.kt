package com.example.openline.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

private const val BASE_URL = "https://openline-backend.up.railway.app"
private const val TAG = "OpinionsViewModel"

class OpinionsViewModel : ViewModel() {

    /**
     * Posts a reply to an opinion.
     * Expects your backend to handle POST /opinions/{opinionId}/reply
     * with JSON body { "text": "..." }.
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
                    Log.d(TAG, "postReply → POST $url → payload=$payload → responseCode=$code")
                    code in 200..299
                } catch (e: Exception) {
                    Log.e(TAG, "postReply error", e)
                    false
                }
            }

            if (success) {
                Log.d(TAG, "Reply posted successfully")
                // TODO: refresh comments or update UI state
            } else {
                Log.e(TAG, "Failed to post reply")
            }
        }
    }

    /**
     * Sends a like/dislike reaction to an opinion.
     * Expects POST /opinions/{opinionId}/react with JSON body { "like": true/false }.
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
                    Log.d(TAG, "reactToOpinion → POST $url → payload=$payload → responseCode=$code")
                    code in 200..299
                } catch (e: Exception) {
                    Log.e(TAG, "reactToOpinion error", e)
                    false
                }
            }

            if (success) {
                Log.d(TAG, "Reaction posted successfully")
                // TODO: you may want to fetch the updated opinion or adjust local state
            } else {
                Log.e(TAG, "Failed to post reaction")
            }
        }
    }
}
