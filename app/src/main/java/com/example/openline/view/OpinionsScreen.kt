package com.example.openline.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.theme.*
import com.example.openline.R
import com.example.openline.model.Comment
import com.example.openline.model.Opinion
import com.example.openline.ui.screens.CommentItem
import com.example.openline.utils.timeAgo
import java.util.UUID
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OpinionScreen(
    opinion: Opinion,
    allComments: List<Comment>,
    author: String,
    userReaction: Boolean?, // <--- track user's current reaction (null/true/false)
    onBack: () -> Unit,
    onReactOpinion: (String, Boolean) -> Unit,
    onReactComment: (String, Boolean) -> Unit,
    onCommentClick: (Comment) -> Unit,
    onSubmitReply: (String, String) -> Unit
) {
    val topComments = remember(allComments) {
        allComments.filter { it.parentCommentId == null }
    }
    val repliesMap = remember(allComments) {
        allComments.groupBy { it.parentCommentId }
    }

    var selectedTab by remember { mutableStateOf("Top") }
    val displayedComments = remember(selectedTab, topComments) {
        if (selectedTab == "Top")
            topComments.sortedByDescending { it.likes }
        else
            topComments.sortedByDescending { it.timestamp }
    }

    var expandedId by remember { mutableStateOf<UUID?>(null) }
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    LaunchedEffect(selectedTab) {
        listState.scrollToItem(0)
    }

    val haptic = LocalHapticFeedback.current
    var lastTopIndex by remember { mutableStateOf(-1) }

    val scrollOffset = remember {
        derivedStateOf {
            if (listState.layoutInfo.visibleItemsInfo.isNotEmpty()) {
                listState.firstVisibleItemScrollOffset.toFloat()
            } else 0f
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background circles
        Box(
            Modifier
                .size(300.dp)
                .offset(x = -200.dp, y = -200.dp)
                .background(Color(85 / 255f, 138 / 255f, 183 / 255f), CircleShape)
                .alpha(0.6f)
        )
        Box(
            Modifier
                .size(400.dp)
                .offset(x = 200.dp, y = 150.dp)
                .background(Color(105 / 255f, 165 / 255f, 148 / 255f), CircleShape)
                .alpha(0.6f)
        )

        // Main content
        Column(Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Opinion for \"Item\" ") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = ColorPrimary,
                            titleContentColor = ColorOnPrimary,
                            navigationIconContentColor = ColorOnPrimary
                        )
                    )
                },
                containerColor = CardBackground,
                content = { innerPadding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Header card
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    opinion.text,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 24.sp
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "$author • ${opinion.timestamp.toLocalTime()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Agree
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedButton(
                                            onClick = { onReactOpinion(opinion.id.toString(), true) },
                                            border = BorderStroke(1.dp, AgreeGreen),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = AgreeGreen,
                                                containerColor = if (userReaction == true) AgreeGreen.copy(alpha = 0.18f) else Color.Transparent
                                            ),
                                            modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            enabled = userReaction != true // disable if already liked
                                        ) {
                                            Icon(
                                                Icons.Outlined.ThumbUp,
                                                contentDescription = "Agree",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("AGREE", fontSize = 12.sp)
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        Text("${opinion.likes}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    // Disagree
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedButton(
                                            onClick = { onReactOpinion(opinion.id.toString(), false) },
                                            border = BorderStroke(1.dp, DisagreeRed),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = DisagreeRed,
                                                containerColor = if (userReaction == false) DisagreeRed.copy(alpha = 0.18f) else Color.Transparent
                                            ),
                                            modifier = Modifier.defaultMinSize(minHeight = 32.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            enabled = userReaction != false // disable if already disliked
                                        ) {
                                            Icon(
                                                Icons.Outlined.ThumbDown,
                                                contentDescription = "Disagree",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("DISAGREE", fontSize = 12.sp)
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        Text("${opinion.dislikes}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = { showReplyField = !showReplyField },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                                ) {
                                    Text("Reply", color = ColorOnPrimary, fontSize = 14.sp)
                                }
                                if (showReplyField) {
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = replyText,
                                        onValueChange = { replyText = it },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            onSubmitReply(opinion.id.toString(), replyText)
                                            replyText = ""
                                            showReplyField = false
                                        },
                                        modifier = Modifier.align(Alignment.End),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Post", fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        Divider(color = DividerColor, thickness = 1.dp)

                        // Tabs
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            listOf("Top", "Newest").forEach { tab ->
                                val sel = tab == selectedTab
                                TextButton(
                                    onClick = { selectedTab = tab },
                                    modifier = Modifier.defaultMinSize(minHeight = 28.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.textButtonColors(
                                        containerColor = if (sel) ColorPrimary else CardBackground,
                                        contentColor = if (sel) ColorOnPrimary else TextSecondary
                                    )
                                ) {
                                    Text(tab, fontSize = 14.sp)
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                        }

                        // Comments list container
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // MIDDLE LINE CANVAS (behind comments) - clothesline
                            Canvas(
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.6f)
                                    .align(Alignment.BottomCenter)
                            ) {
                                val lineX = size.width / 2f - 30f
                                val waveHeight = 2.dp.toPx()
                                val waveLength = 120.dp.toPx()
                                val animationOffset = scrollOffset.value * 0.003f

                                val path = Path().apply {
                                    moveTo(lineX, 0f)
                                    var y = 0f
                                    while (y <= size.height) {
                                        val waveX = lineX + sin((y / waveLength + animationOffset) * 2 * PI).toFloat() * waveHeight
                                        lineTo(waveX, y)
                                        y += 2f
                                    }
                                }

                                drawPath(
                                    path = path,
                                    color = Color.Gray.copy(alpha = 0.6f),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                )
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 12.dp, end = 32.dp),
                                contentPadding = PaddingValues(bottom = 200.dp)
                            ) {
                                itemsIndexed(displayedComments, key = { _, it -> it.id }) { idx, comment ->
                                    val info = listState.layoutInfo
                                    val itm = info.visibleItemsInfo.firstOrNull { it.index == idx }
                                    val vh = info.viewportSize.height.toFloat()
                                    val offsetY = itm?.offset?.toFloat() ?: 0f
                                    val normPos = (1f - (offsetY / vh)).coerceIn(0f, 1f)

                                    val targetScale = 0.7f + (normPos * 0.3f)
                                    val targetAlpha = 0.5f + (normPos * 0.5f)
                                    val animScale by animateFloatAsState(targetScale)
                                    val animAlpha by animateFloatAsState(targetAlpha)

                                    val maxTilt = 10f
                                    val dir = if (idx % 2 == 0) 1 else -1
                                    val targetRot = (1f - normPos) * maxTilt * dir
                                    val animRot by animateFloatAsState(targetRot)

                                    LaunchedEffect(itm?.offset) {
                                        if (itm?.offset == 0 && lastTopIndex != idx) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            lastTopIndex = idx
                                        }
                                    }

                                    // Wrap in Box for proper overlay positioning
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                    ) {
                                        // Comment item with animations
                                        CommentItem(
                                            comment = comment,
                                            repliesCount = repliesMap[comment.id].orEmpty().size,
                                            onRepliesClick = {
                                                expandedId = if (expandedId == comment.id) null else comment.id
                                            },
                                            onReact = { l -> onReactComment(comment.id.toString(), l) },
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    scaleX = animScale
                                                    scaleY = animScale
                                                    alpha = animAlpha
                                                    rotationZ = animRot
                                                }
                                                .fillMaxWidth()
                                                .clickable { onCommentClick(comment) }
                                        )

                                        // Clothespin overlay - positioned on top center, aligned with clothesline
                                        Image(
                                            painter = painterResource(id = R.drawable.clothespin),
                                            contentDescription = "Clothespin",
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .offset(y = (-10).dp)
                                                .size(46.dp)
                                        )
                                    }

                                    // Expanded replies section
                                    if (expandedId == comment.id) {
                                        val replies = repliesMap[comment.id].orEmpty()
                                        Column(
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(start = 28.dp, end = 12.dp, bottom = 12.dp)
                                        ) {
                                            replies.take(2).forEach { r ->
                                                CommentItem(
                                                    comment = r,
                                                    repliesCount = 0,
                                                    onRepliesClick = null,
                                                    onReact = { onReactComment(r.id.toString(), it) },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 6.dp)
                                                )
                                            }
                                            if (replies.size > 2) {
                                                Text(
                                                    "See more…",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = ColorPrimary,
                                                    modifier = Modifier
                                                        .clickable { onCommentClick(comment) }
                                                        .padding(vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        // RIGHT SIDE WAVY LINE (above everything)
        Canvas(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp)
        ) {
            val lineX = size.width - 2.dp.toPx()
            val waveHeight = 8.dp.toPx()
            val waveLength = 80.dp.toPx()
            val animationOffset = scrollOffset.value * -0.005f // Reverse direction

            val path = Path().apply {
                moveTo(lineX, 0f)

                var y = 0f
                while (y <= size.height) {
                    val waveX = lineX + sin((y / waveLength + animationOffset) * 2 * PI).toFloat() * waveHeight
                    lineTo(waveX, y)
                    y += 3f
                }
            }

            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
