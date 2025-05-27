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
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class CommentsViewModel : ViewModel() {

    companion object {
        private const val BASE_URL = "http://10.0.2.2:5000"
        //private const val BASE_URL = "http://android.openline.marijndemul.nl"
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

    /**
     * 1.1) GET /comments/opinion/{opinionId}
     * Returns every Comment whose opinion_id == opinionId
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCommentsByOpinion(opinionId: String): List<Comment>? = withContext(Dispatchers.IO) {
        try {
            val url  = URL("$BASE_URL/comments/opinion/$opinionId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod   = "GET"
                connectTimeout  = 5_000
                readTimeout     = 5_000
            }

            return@withContext if (conn.responseCode == 200) {
                // parses the JSON array into a List<Comment>
                parseCommentList(readBody(conn))

            } else {
                Log.e(TAG, "getCommentsByOpinion: HTTP ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "getCommentsByOpinion error", e)
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
            val url = URL("$BASE_URL/comments/")
            Log.d(TAG, "createComment: POST $url")

            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod   = "POST"
                doOutput        = true
                connectTimeout  = 5_000
                readTimeout     = 5_000
                setRequestProperty("Content-Type", "application/json")
            }

            // Build JSON using camelCase keys
            val payload = JSONObject().apply {
                put("opinionId",        new.opinionId.toString())
                put("userId",           new.userId.toString())
                put("text",             new.text)
                put("timestamp",        new.timestamp.toString())
                put("likes",            new.likes)
                put("dislikes",         new.dislikes)
                new.parentCommentId?.let { put("parentCommentId", it.toString()) }
            }.toString()

            Log.d(TAG, "createComment: payload = $payload")
            conn.outputStream.use { it.write(payload.toByteArray()) }

            val code = conn.responseCode
            // Read the body exactly once
            val body = BufferedReader(InputStreamReader(
                if (code in 200..299) conn.inputStream else conn.errorStream
            )).use { it.readText() }

            Log.d(TAG, "createComment: responseCode = $code")
            Log.d(TAG, "createComment: responseBody = $body")

            if (code == 201) {
                // Supabase returns an array of inserted rows
                val arr = JSONArray(body)
                if (arr.length() > 0) {
                    val obj = arr.getJSONObject(0)
                    return@withContext parseComment(obj)
                }
            }

            Log.e(TAG, "createComment failed: HTTP $code, body=$body")
            return@withContext null

        } catch (e: Exception) {
            Log.e(TAG, "createComment error", e)
            return@withContext null
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

    /**
     * POST /comments/{commentId}/react
     * with JSON { "like": true } or { "like": false }
     * Returns true if server responded 2xx.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun reactToComment(commentId: String, like: Boolean): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/comments/$commentId/react")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod   = "POST"
                    connectTimeout  = 5_000
                    readTimeout     = 5_000
                    doOutput        = true
                    setRequestProperty("Content-Type", "application/json; utf-8")
                }

                val payload = JSONObject().put("like", like).toString()
                OutputStreamWriter(conn.outputStream).use { it.write(payload) }

                val code = conn.responseCode
                Log.d(TAG, "reactToComment → POST $url → payload=$payload → code=$code")
                code in 200..299
            } catch (e: Exception) {
                Log.e(TAG, "reactToComment error", e)
                false
            }
        }

    // ------- Helpers -------

    private fun readBody(conn: HttpURLConnection): String =
        BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseComment(obj: JSONObject): Comment {
        // parse the ISO timestamp with offset and drop the zone
        val localDt = OffsetDateTime
            .parse(obj.getString("timestamp"), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .toLocalDateTime()

        return Comment(
            id              = UUID.fromString(obj.getString("id")),
            opinionId       = UUID.fromString(obj.getString("opinionId")),
            userId          = UUID.fromString(obj.getString("userId")),
            text            = obj.getString("text"),
            timestamp       = localDt,
            likes           = obj.optInt("likes"),
            dislikes        = obj.optInt("dislikes"),
            parentCommentId = obj
                .optString("parentCommentId", "")                                  // default = empty
                .takeIf { it.isNotBlank() && it != "null" }                       // drop blanks & "null"
                ?.let { UUID.fromString(it) }
        )
    }



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
