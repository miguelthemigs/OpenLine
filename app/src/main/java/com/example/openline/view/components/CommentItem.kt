// com/example/openline/ui/screens/CommentItem.kt
package com.example.openline.ui.screens

import UserName
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.openline.model.Comment
import com.example.openline.utils.timeAgo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import com.example.app.ui.theme.TextPrimary
import com.example.app.ui.theme.TextSecondary

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentItem(
    comment: Comment,
    onReact: (like: Boolean) -> Unit,
    onClick: (() -> Unit)? = null,     // ← new
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .then(
                if (onClick != null)
                    Modifier.clickable { onClick() }
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CoPresent,
                    contentDescription = "User avatar",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))

                UserName(                          // ← your existing UserName
                    userId = comment.userId.toString(),
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = timeAgo(comment.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.ThumbUp,
                    contentDescription = "Like",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onReact(true) }
                )
                Spacer(Modifier.width(4.dp))
                Text(comment.likes.toString(), color = TextSecondary)

                Spacer(Modifier.width(16.dp))

                Icon(
                    Icons.Outlined.ThumbDown,
                    contentDescription = "Dislike",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onReact(false) }
                )
                Spacer(Modifier.width(4.dp))
                Text(comment.dislikes.toString(), color = TextSecondary)
            }
        }
    }
}
