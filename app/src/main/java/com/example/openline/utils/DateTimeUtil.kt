package com.example.openline.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Returns a human-friendly "time ago" label like:
 *   • "Just now"
 *   • "5 min ago"
 *   • "3 h ago"
 *   • "2 d ago"
 */
@RequiresApi(Build.VERSION_CODES.O)
fun timeAgo(from: LocalDateTime): String {
    val now = LocalDateTime.now(ZoneId.systemDefault())
    val dur = Duration.between(from, now)

    val minutes = dur.toMinutes()
    return when {
        minutes < 1    -> "Just now"
        minutes < 60   -> "${minutes} min ago"
        minutes < 1_440 -> "${minutes / 60} h ago"        // 60 * 24
        else           -> "${minutes / 1_440} d ago"
    }
}
