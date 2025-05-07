package com.example.openline.ui.screens

import UserName
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.openline.model.Comment
import com.example.openline.utils.timeAgo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CoPresent
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import com.example.app.ui.theme.TextPrimary
import com.example.app.ui.theme.TextSecondary
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.openline.viewmodel.UsersViewModel

/**
 * Displays a single comment, with:
 *  • avatar icon, username, timestamp
 *  • comment text
 *  • a row below with:
 *     – left: “<count> replies >” (clickable if onRepliesClick != null)
 *     – right: thumbs-up & count, thumbs-down & count
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentItem(
    comment: Comment,
    onReact: (like: Boolean) -> Unit,
    repliesCount: Int = 0,
    onRepliesClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val usersViewModel: UsersViewModel = viewModel()
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            // Header: avatar, username, time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CoPresent,
                    contentDescription = "User avatar",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                )
                Spacer(Modifier.width(8.dp))

                // user name loader
                UserName(
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

            // Comment text
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))
            Divider()

            // Footer row: replies link on left, reactions on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onRepliesClick != null) {
                    Text(
                        text = when {
                            repliesCount > 99 -> "99+ replies ›"
                            repliesCount > 0  -> "$repliesCount replies ›"
                            else              -> "No replies"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        modifier = Modifier
                            .clickable { onRepliesClick() }
                    )
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Spacer(Modifier.weight(1f))

                // Thumbs-up + count
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
                    Text(
                        text = comment.likes.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Thumbs-down + count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ThumbDown,
                        contentDescription = "Dislike",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onReact(false) }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = comment.dislikes.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
