package com.example.openline

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import com.example.openline.model.Comment
import com.example.openline.model.Opinion
import com.example.openline.ui.screens.OpinionScreen
import com.example.openline.ui.theme.OpenLineTheme
import com.example.openline.view.RepliesScreen
import java.time.LocalDateTime
import java.util.UUID

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OpenLineTheme {
                // — stable sample data —
                val sampleOpinion by remember {
                    mutableStateOf(
                        Opinion(
                            id        = UUID.randomUUID(),
                            itemId    = UUID.randomUUID(),
                            userId    = UUID.randomUUID(),
                            text      = "I think Tung Tung Sahur is better than Trlalero Tralala",
                            timestamp = LocalDateTime.now().minusHours(2),
                            likes     = 5788,
                            dislikes  = 156
                        )
                    )
                }

                val sampleComments by remember {
                    mutableStateOf(
                        listOf(
                            Comment(
                                id        = UUID.randomUUID(),
                                opinionId = sampleOpinion.id,
                                userId    = UUID.randomUUID(),
                                text      = "I think Tung Tung Sahur has an immense backstory…",
                                timestamp = LocalDateTime.now().minusMinutes(33),
                                likes     = 1256,
                                dislikes  = 3
                            ),
                            Comment(
                                id        = UUID.randomUUID(),
                                opinionId = sampleOpinion.id,
                                userId    = UUID.randomUUID(),
                                text      = "Why is nobody talking about how Trlalero Tralala is bad at combat…",
                                timestamp = LocalDateTime.now().minusMinutes(45),
                                likes     = 20,
                                dislikes  = 456
                            )
                        )
                    )
                }

                val sampleReplies by remember {
                    mutableStateOf(
                        listOf(
                            Comment(
                                id              = UUID.randomUUID(),
                                opinionId       = sampleOpinion.id,
                                userId          = UUID.randomUUID(),
                                text            = "Totally agree with that backstory!",
                                timestamp       = LocalDateTime.now().minusMinutes(31),
                                likes           = 10,
                                dislikes        = 0,
                                parentCommentId = sampleComments[0].id
                            ),
                            Comment(
                                id              = UUID.randomUUID(),
                                opinionId       = sampleOpinion.id,
                                userId          = UUID.randomUUID(),
                                text            = "Combat really is terrible, you nailed it!",
                                timestamp       = LocalDateTime.now().minusMinutes(29),
                                likes           = 5,
                                dislikes        = 2,
                                parentCommentId = sampleComments[1].id
                            )
                        )
                    )
                }

                // Track which comment is tapped
                var selectedComment by remember { mutableStateOf<Comment?>(null) }

                if (selectedComment == null) {
                    OpinionScreen(
                        opinion        = sampleOpinion,
                        comments       = sampleComments,
                                 // pass replies list in
                        author         = "Ballerina Cappuccina",
                        onBack         = { finish() },
                        onReactOpinion = { _, _ -> /* TODO */ },
                        onReactComment = { _, _ -> /* TODO */ },
                        onCommentClick = { selectedComment = it },
                        onSubmitReply  = { _, _ -> /* TODO */ }
                    )
                } else {
                    RepliesScreen(
                        parent         = selectedComment!!,
                        allComments    = sampleComments + sampleReplies,
                        onBack         = { selectedComment = null },
                        onReactComment = { _, _ -> /* TODO */ }
                    )
                }
            }
        }
    }
}
