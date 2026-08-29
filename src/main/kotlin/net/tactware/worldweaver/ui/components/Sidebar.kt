package net.tactware.worldweaver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.ui.navigation.Screen
import net.tactware.worldweaver.ui.session.LocalUser
import net.tactware.worldweaver.ui.theme.DarkNavy
import net.tactware.worldweaver.ui.theme.ThemeMode

@Composable
internal fun Sidebar(
    currentUser: LocalUser,
    currentScreen: Screen,
    activeWorldName: String? = null,
    activeCampaignName: String? = null,
    notificationCount: Int = 0,
    showNotifications: Boolean = false,
    notifications: List<ShellNotification> = emptyList(),
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onCycleThemeMode: () -> Unit = {},
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
    onToggleNotifications: () -> Unit = {},
    onDismissNotifications: () -> Unit = {},
    onNotificationClick: (ShellNotification) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(DarkNavy)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(
                Icons.Default.AutoStories,
                contentDescription = "World Weaver logo",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "World Weaver",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = activeContextLabel(activeWorldName, activeCampaignName),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        NavItem(
            icon = Icons.Default.Home,
            label = "Home",
            contentDescription = "Navigate to Home",
            isSelected = currentScreen == Screen.HOME,
            onClick = { onNavigate(Screen.HOME) }
        )

        NavItem(
            icon = Icons.Default.Public,
            label = "Worlds",
            contentDescription = "Navigate to Worlds",
            isSelected = currentScreen == Screen.WORLDS,
            onClick = { onNavigate(Screen.WORLDS) }
        )

        NavItem(
            icon = Icons.Default.Flag,
            label = "Campaigns",
            contentDescription = "Navigate to Campaigns",
            isSelected = currentScreen == Screen.CAMPAIGNS,
            onClick = { onNavigate(Screen.CAMPAIGNS) }
        )

        NavItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            contentDescription = "Navigate to Settings",
            isSelected = currentScreen == Screen.SETTINGS,
            onClick = { onNavigate(Screen.SETTINGS) }
        )

        Spacer(modifier = Modifier.weight(1f))

        if (showNotifications) {
            NotificationPanel(
                notifications = notifications,
                onDismiss = onDismissNotifications,
                onNotificationClick = onNotificationClick
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleNotifications) {
                BadgedBox(
                    badge = {
                        if (notificationCount > 0) {
                            Badge { Text(notificationCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications, $notificationCount unread",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            IconButton(onClick = onCycleThemeMode) {
                Icon(
                    imageVector = when (themeMode) {
                        ThemeMode.LIGHT -> Icons.Default.LightMode
                        ThemeMode.DARK -> Icons.Default.DarkMode
                        ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                    },
                    contentDescription = "Theme: ${themeMode.label()}. Tap to change.",
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "User account",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentUser.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = currentUser.email,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Sign out",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Sign Out",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun NotificationPanel(
    notifications: List<ShellNotification>,
    onDismiss: () -> Unit,
    onNotificationClick: (ShellNotification) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                TextButton(onClick = onDismiss) {
                    Text("Clear", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }
            if (notifications.isEmpty()) {
                Text(
                    text = "No new notifications",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            } else {
                notifications.take(5).forEach { notification ->
                    Text(
                        text = notification.message,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNotificationClick(notification) }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun activeContextLabel(
    activeWorldName: String?,
    activeCampaignName: String?,
): String {
    return when {
        activeWorldName != null && activeCampaignName != null -> {
            "$activeWorldName · $activeCampaignName"
        }
        activeWorldName != null -> activeWorldName
        else -> "No world selected"
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent
    val textColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(bgColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = textColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}
