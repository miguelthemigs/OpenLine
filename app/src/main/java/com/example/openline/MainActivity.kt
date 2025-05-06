package com.example.openline

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.openline.model.Comment
import com.example.openline.model.Opinion
import com.example.openline.ui.screens.OpinionScreen
import com.example.openline.ui.theme.OpenLineTheme
import java.time.LocalDateTime
import java.util.UUID

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OpenLineTheme {
                // sample data
                val sampleOpinion = Opinion(
                    id        = UUID.randomUUID(),
                    itemId    = UUID.randomUUID(),
                    userId    = UUID.fromString("707cc6b0-6e15-4f44-867e-26118c11bc73"),
                    text      = "I think Tung Tung Sahur is better than Trlalero Tralala",
                    timestamp = LocalDateTime.now().minusHours(2),
                    likes     = 5788,
                    dislikes  = 156
                )

                val sampleComments = listOf(
                    Comment(
                        id        = UUID.randomUUID(),
                        opinionId = sampleOpinion.id,
                        userId    = UUID.fromString("a69b8063-e6c4-4170-a9aa-eee50c40bff5"),
                        text      = "I think Tung Tung Sahur has an immense backstory…",
                        timestamp = LocalDateTime.now().minusMinutes(33),
                        likes     = 1256,
                        dislikes  = 3
                    ),
                    Comment(
                        id        = UUID.randomUUID(),
                        opinionId = sampleOpinion.id,
                        userId    = UUID.randomUUID(),
                        text      = "Why is nobody talking about how Tralalero Tralala is bad at combat…",
                        timestamp = LocalDateTime.now().minusMinutes(45),
                        likes     = 20,
                        dislikes  = 456
                    ),
                    Comment(
                        id        = UUID.randomUUID(),
                        opinionId = sampleOpinion.id,
                        userId    = UUID.randomUUID(),
                        text      = "Sahur and Gusini are my top 2…",
                        timestamp = LocalDateTime.now().minusMinutes(55),
                        likes     = 8888,
                        dislikes  = 78
                    )
                )

                OpinionScreen(
                    opinion = sampleOpinion,
                    comments = sampleComments,
                    author = "Ballerina Cappuccina",  // replace with viewModel or Intent data
                    onBack = { finish() },
                    onReactOpinion = { opinionId, like ->
                        // TODO: viewModel.reactToOpinion(opinionId, like)
                    },
                    onReactComment = { commentId, like ->
                        // TODO: viewModel.reactToComment(commentId, like)
                    },
                    onReply = { opinionId ->
                        // we’re handling replies inline, so nothing special here
                    },
                    onSubmitReply = { opinionId, replyText ->
                        // TODO: viewModel.postReply(opinionId, replyText)
                    }
                )
            }
        }
    }
}
