package com.example.openline.model

import java.time.LocalDateTime
import java.util.UUID

data class Comment(
    var id: UUID,
    val opinionId: UUID,
    val userId: UUID,
    val text: String,
    val timestamp: LocalDateTime,
    val likes: Int,
    val dislikes: Int,
    val parentCommentId: UUID? = null,
)