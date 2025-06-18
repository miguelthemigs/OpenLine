package com.example.openline.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max
import kotlin.math.min

data class NavBarItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun CustomBottomNavBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onNewClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onAccountClick: () -> Unit = {}
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp) // give breathing room
            .height(80.dp)
    ) {
        val density = LocalDensity.current
        val screenWidth = with(density) { maxWidth.toPx() }
        val horizontalPadding = with(density) {
            min(max(screenWidth * 0.05f, 20.dp.toPx()), 60.dp.toPx()).toDp()
        }

        val navItems = listOf(
            NavBarItem(Icons.Filled.Home, "Home", onHomeClick),
            NavBarItem(Icons.Filled.Search, "Search", onSearchClick),
            NavBarItem(Icons.Filled.Add, "New", onNewClick),
            NavBarItem(Icons.Filled.Bookmark, "Saved", onSaveClick),
            NavBarItem(Icons.Filled.AccountCircle, "Account", onAccountClick)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .align(Alignment.Center)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
                .background(
                    color = Color.White.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(32.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0x33007AFF), // semi-transparent iOS-style blue
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(vertical = 10.dp, horizontal = 20.dp)
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    NavBarItemComposable(item.icon, item.label, item.onClick)
                }
            }
        }
    }
}

@Composable
private fun NavBarItemComposable(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF007AFF), // Apple blue
            modifier = Modifier.size(26.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF007AFF),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
