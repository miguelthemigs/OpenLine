package com.example.openline.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import com.example.openline.model.Comment
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.util.UUID

class CommentsViewModel : ViewModel() {

    companion object {
        private const val BASE_URL = "https://openline-backend.up.railway.app"
        private const val TAG      = "CommentsViewModel"
    }

    // 1) GET /comments → List all comments
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getComments(): List<Comment>? = withContext(Dispatchers.IO) {
        try {
            val url  = URL("$BASE_URL/comments")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod   = "GET"
                connectTimeout  = 5_000
                readTimeout     = 5_000
            }
            return@withContext if (conn.responseCode == 200) {
                parseCommentList(readBody(conn))
            } else {
                Log.e(TAG, "getComments: HTTP ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getComments error", e)
            null
        }
    }

    // 2) GET /comments/{id}
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getComment(commentId: String): Comment? = withContext(Dispatchers.IO) {
        try {
            val url  = URL("$BASE_URL/comments/$commentId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod   = "GET"
                connectTimeout  = 5_000
                readTimeout     = 5_000
            }
            return@withContext when (conn.responseCode) {
                200 -> parseComment(JSONObject(readBody(conn)))
                404 -> null
                else -> {
                    Log.e(TAG, "getComment: HTTP ${conn.responseCode}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getComment error", e)
            null
        }
    }

    // 3) GET /comments/{id}/replies
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getReplies(parentCommentId: String): List<Comment>? = withContext(Dispatchers.IO) {
        try {
            val url  = URL("$BASE_URL/comments/$parentCommentId/replies")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod   = "GET"
                connectTimeout  = 5_000
                readTimeout     = 5_000
            }
            return@withContext if (conn.responseCode == 200) {
                parseCommentList(readBody(conn))
            } else {
                Log.e(TAG, "getReplies: HTTP ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getReplies error", e)
            null
        }
    }

    // 4) POST /comments
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createComment(new: Comment): Comment? = withContext(Dispatchers.IO) {
        try {
            val url  = URL("$BASE_URL/comments")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod   = "POST"
                doOutput        = true
                connectTimeout  = 5_000
                readTimeout     = 5_000
                setRequestProperty("Content-Type", "application/json")
            }
            // build JSON
            val payload = JSONObject().apply {
                put("id", new.id.toString())
                put("opinion_id", new.opinionId.toString())
                put("user_id", new.userId.toString())
                put("text", new.text)
                put("timestamp", new.timestamp.toString())
                put("likes", new.likes)
                put("dislikes", new.dislikes)
                new.parentCommentId?.let { put("parent_comment_id", it.toString()) }
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            return@withContext if (conn.responseCode == 201) {
                parseComment(JSONObject(readBody(conn)))
            } else {
                Log.e(TAG, "createComment: HTTP ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "createComment error", e)
            null
        }
    }

    // 5) PUT /comments/{id}
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateComment(id: String, updates: Map<String, Any>): Comment? = withContext(Dispatchers.IO) {
        try {
            val url  = URL("$BASE_URL/comments/$id")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod   = "PUT"
                doOutput        = true
                connectTimeout  = 5_000
                readTimeout     = 5_000
                setRequestProperty("Content-Type", "application/json")
            }
            val payload = JSONObject().apply {
                updates.forEach { (k, v) ->
                    val key = if (k == "parentCommentId") "parent_comment_id" else k
                    put(key, v)
                }
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }

            return@withContext if (conn.responseCode == 200) {
                parseComment(JSONObject(readBody(conn)))
            } else {
                Log.e(TAG, "updateComment: HTTP ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateComment error", e)
            null
        }
    }

    // 6) DELETE /comments/{id}
    suspend fun deleteComment(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val url  = URL("$BASE_URL/comments/$id")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod   = "DELETE"
                connectTimeout  = 5_000
                readTimeout     = 5_000
            }
            val ok = conn.responseCode in 200..299
            if (!ok) Log.e(TAG, "deleteComment: HTTP ${conn.responseCode}")
            return@withContext ok
        } catch (e: Exception) {
            Log.e(TAG, "deleteComment error", e)
            false
        }
    }

    // 7) GET /comments/{id}/likes
    suspend fun getCommentLikes(id: String): Int? = withContext(Dispatchers.IO) {
        try {
            val url  = URL("$BASE_URL/comments/$id/likes")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod   = "GET"
                connectTimeout  = 5_000
                readTimeout     = 5_000
            }
            return@withContext if (conn.responseCode == 200) {
                JSONObject(readBody(conn)).optInt("likes")
            } else {
                Log.e(TAG, "getCommentLikes: HTTP ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCommentLikes error", e)
            null
        }
    }

    // 8) GET /comments/{id}/dislikes
    suspend fun getCommentDislikes(id: String): Int? = withContext(Dispatchers.IO) {
        try {
            val url  = URL("$BASE_URL/comments/$id/dislikes")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod   = "GET"
                connectTimeout  = 5_000
                readTimeout     = 5_000
            }
            return@withContext if (conn.responseCode == 200) {
                JSONObject(readBody(conn)).optInt("dislikes")
            } else {
                Log.e(TAG, "getCommentDislikes: HTTP ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCommentDislikes error", e)
            null
        }
    }

    // ------- Helpers -------

    private fun readBody(conn: HttpURLConnection): String =
        BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseComment(obj: JSONObject): Comment = Comment(
        id              = UUID.fromString(obj.getString("id")),
        opinionId       = UUID.fromString(obj.getString("opinion_id")),
        userId          = UUID.fromString(obj.getString("user_id")),
        text            = obj.getString("text"),
        timestamp       = LocalDateTime.parse(obj.getString("timestamp")),
        likes           = obj.optInt("likes"),
        dislikes        = obj.optInt("dislikes"),
        parentCommentId = obj.optString("parent_comment_id", null)
            .takeIf { it.isNotBlank() }
            ?.let { UUID.fromString(it) }
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseCommentList(json: String): List<Comment> {
        val arr = JSONArray(json)
        val list = mutableListOf<Comment>()
        for (i in 0 until arr.length()) {
            list += parseComment(arr.getJSONObject(i))
        }
        return list
    }
}
