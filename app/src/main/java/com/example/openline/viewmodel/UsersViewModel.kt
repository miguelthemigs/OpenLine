package com.example.openline.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private const val BASE_URL = "https://openline-backend.up.railway.app"
private const val TAG = "UsersViewModel"

class UsersViewModel : ViewModel() {

    /**
     * Fetches a user’s name from the backend.
     * Call this from a coroutine scope (e.g. in a Composable’s LaunchedEffect or in another ViewModel).
     */
    suspend fun fetchUserName(userId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/users/$userId/name")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod  = "GET"
                connectTimeout = 5_000
                readTimeout    = 5_000
            }

            return@withContext if (conn.responseCode == 200) {
                val rawBody = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                JSONObject(rawBody).optString("name", null)
            } else {
                Log.e(TAG, "fetchUserName: non-200 response ${conn.responseCode}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchUserName error", e)
            null
        }
    }
}
