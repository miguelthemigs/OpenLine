package com.example.openline.model

import java.time.LocalDateTime
import java.util.UUID

data class Comment(
    val id: UUID,
    val opinionId: UUID,
    val userId: UUID,
    val text: String,
    val timeStamp: LocalDateTime,
    val likes: Int,
    val dislikes: Int,
)