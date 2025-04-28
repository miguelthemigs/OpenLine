package com.example.openline.model

import java.time.LocalDateTime
import java.util.UUID

data class Opinion(
    val id: UUID,
    val itemId: UUID,
    val userId: UUID,
    val text: String,
    val timestamp: LocalDateTime,
    val likes: Int,
    val dislikes: Int,
)