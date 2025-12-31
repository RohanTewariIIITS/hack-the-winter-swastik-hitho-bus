package com.example.celltowertrackingforbus.NewScreens


import androidx.compose.foundation. background
import androidx.compose.foundation.clickable
import androidx. compose.foundation.layout.*
import androidx. compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose. material.icons.filled.*
import androidx.compose.material.icons. outlined.*
import androidx.compose.material3.*
import androidx. compose.runtime. Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose. ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit. dp
import androidx. compose.ui.unit.sp

data class DrawerItem(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit
)

@Composable
fun NavigationDrawerContent(
    userName: String,
    userEmail: String,
    onHomeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val drawerItems = listOf(
        DrawerItem(Icons.Default.Home, "Home", onHomeClick),
        DrawerItem(Icons.Default.Language, "Language", onLanguageClick),
        DrawerItem(Icons.Default. Feedback, "Send Feedback", onFeedbackClick),
        DrawerItem(Icons.Default.Settings, "Setting", onSettingsClick)
    )

    Column(
        modifier = Modifier
            . fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // Header Section
        Column(
            modifier = Modifier
                . fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier. height(12.dp))

            Text(
                text = userName,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = userEmail,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        Divider(
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Items
        drawerItems.forEach { item ->
            DrawerMenuItem(
                icon = item.icon,
                title = item. title,
                onClick = item.onClick
            )
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title:  String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            . fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier. width(16.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 16.sp
        )
    }
}