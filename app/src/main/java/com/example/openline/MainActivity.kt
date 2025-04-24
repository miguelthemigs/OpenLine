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
                    userId    = UUID.randomUUID(),
                    text      = "I think Tung Tung Sahur is better than Trlalero Tralala",
                    timeStamp = LocalDateTime.now().minusHours(2),
                    likes     = 5788,
                    dislikes  = 156
                )

                val sampleComments = listOf(
                    Comment(
                        id        = UUID.randomUUID(),
                        opinionId = sampleOpinion.id,
                        userId    = UUID.randomUUID(),
                        text      = "I think Tung Tung Sahur has an immense backstory…",
                        timeStamp = LocalDateTime.now().minusMinutes(33),
                        likes     = 1256,
                        dislikes  = 3
                    ),
                    Comment(
                        id        = UUID.randomUUID(),
                        opinionId = sampleOpinion.id,
                        userId    = UUID.randomUUID(),
                        text      = "Why is nobody talking about how Trlalero Tralala is bad at combat…",
                        timeStamp = LocalDateTime.now().minusMinutes(45),
                        likes     = 20,
                        dislikes  = 456
                    ),
                    Comment(
                        id        = UUID.randomUUID(),
                        opinionId = sampleOpinion.id,
                        userId    = UUID.randomUUID(),
                        text      = "Sahur and Gusini are my top 2…",
                        timeStamp = LocalDateTime.now().minusMinutes(55),
                        likes     = 8888,
                        dislikes  = 78
                    )
                )

                OpinionScreen(
                    opinion = sampleOpinion,
                    comments = sampleComments,
                    onBack = { finish() },
                    onReactOpinion = { opinionId, like ->
                        // TODO: call your API to react to the opinion
                    },
                    onReactComment = { commentId, like ->
                        // TODO: call your API to react to the comment
                    },
                    onReply = { opinionId ->
                        // TODO: navigate to your “reply” screen
                    },
                    author  = "Ballerina Cappuccina" // TODO: replace with actual author name
                )
            }
        }
    }
}
