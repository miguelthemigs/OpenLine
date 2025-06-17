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

class AuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
        private const val BASE_URL = "https://openline-android-backend.onrender.com"
    }

    var token: String? = null
    var role: String? = null
    var name: String? = null

    fun register(name: String, email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = URL("$BASE_URL/auth/register")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 5000
                        readTimeout = 5000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json; utf-8")
                    }

                    val payload = JSONObject().apply {
                        put("name", name)
                        put("email", email)
                        put("password", password)
                    }.toString()

                    OutputStreamWriter(conn.outputStream).use { it.write(payload) }

                    val code = conn.responseCode
                    Log.d(TAG, "register → POST $url → payload=$payload → code=$code")

                    if (code == 201) {
                        val raw = conn.inputStream.bufferedReader().readText()
                        val json = JSONObject(raw)
                        token = json.getString("token")
                        role = json.getString("role")
                        null
                    } else {
                        "Failed to register: $code"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "register error", e)
                    e.message
                }
            }

            if (result == null) {
                onSuccess()
            } else {
                onError(result)
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val url = URL("$BASE_URL/auth/login")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        connectTimeout = 5000
                        readTimeout = 5000
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json; utf-8")
                    }

                    val payload = JSONObject().apply {
                        put("email", email)
                        put("password", password)
                    }.toString()

                    OutputStreamWriter(conn.outputStream).use { it.write(payload) }

                    val code = conn.responseCode
                    Log.d(TAG, "login → POST $url → payload=$payload → code=$code")

                    if (code == 200) {
                        val raw = conn.inputStream.bufferedReader().readText()
                        val json = JSONObject(raw)
                        token = json.getString("token")
                        role = json.getString("role")
                        name = json.getString("name")
                        null
                    } else {
                        "Invalid credentials: $code"
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "login error", e)
                    e.message
                }
            }

            if (result == null) {
                onSuccess()
            } else {
                onError(result)
            }
        }
    }
}
