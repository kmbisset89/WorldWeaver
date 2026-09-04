package io.github.kmbisset89.worldweaver.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.appWindowIcon
import io.github.kmbisset89.worldweaver.ui.navigation.Screen
import io.github.kmbisset89.worldweaver.ui.session.LocalUser
import io.github.kmbisset89.worldweaver.ui.theme.DarkNavy
import io.github.kmbisset89.worldweaver.ui.theme.ThemeMode

@Composable
internal fun Sidebar(
    currentUser: LocalUser,
    currentScreen: Screen,
    activeWorldName: String? = null,
    activeCampaignName: String? = null,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    expanded: Boolean = true,
    onCycleThemeMode: () -> Unit = {},
    onToggleExpanded: () -> Unit = {},
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
) {
    val railWidth = if (expanded) 240.dp else 72.dp
    Column(
        modifier = Modifier
            .width(railWidth)
            .fillMaxHeight()
            .background(DarkNavy)
            .padding(if (expanded) 16.dp else 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Image(
                painter = appWindowIcon(),
                contentDescription = activeContextLabel(activeWorldName, activeCampaignName),
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop,
            )
            if (expanded) {
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
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
        }

        IconButton(
            onClick = onToggleExpanded,
            modifier = Modifier.align(if (expanded) Alignment.End else Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                contentDescription = if (expanded) {
                    "Collapse navigation"
                } else {
                    "Expand navigation"
                },
                tint = Color.White.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            NavDestination.entries.forEach { destination ->
                NavItem(
                    icon = destination.icon,
                    label = destination.label,
                    contentDescription = destination.contentDescription,
                    isSelected = currentScreen == destination.screen ||
                        (destination.screen == Screen.HOME &&
                            (currentScreen == Screen.RUN || currentScreen == Screen.ONE_SHOT_WIZARD)),
                    expanded = expanded,
                    onClick = { onNavigate(destination.screen) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (expanded) {
                Arrangement.SpaceBetween
            } else {
                Arrangement.Center
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = currentUser.displayName,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(32.dp)
            )
            if (expanded) {
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (expanded) {
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
        } else {
            IconButton(
                onClick = onLogout,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign out",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private enum class NavDestination(
    val screen: Screen,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String,
) {
    Home(Screen.HOME, Icons.Default.Home, "Home", "Navigate to Home"),
    Worlds(Screen.WORLDS, Icons.Default.Public, "Worlds", "Navigate to Worlds"),
    Campaigns(Screen.CAMPAIGNS, Icons.Default.Flag, "Campaigns", "Navigate to Campaigns"),
    Locations(Screen.LOCATIONS, Icons.Default.Place, "Locations", "Navigate to Locations"),
    Lore(Screen.LORE, Icons.AutoMirrored.Filled.MenuBook, "Lore", "Navigate to Lore"),
    Calendar(Screen.CALENDAR, Icons.Default.CalendarMonth, "Calendar", "Navigate to Calendar"),
    Factions(Screen.FACTIONS, Icons.Default.AccountBalance, "Factions", "Navigate to Factions"),
    Links(Screen.LINKS, Icons.Default.AccountTree, "Links", "Navigate to Links"),
    Characters(Screen.CHARACTERS, Icons.Default.Groups, "Characters", "Navigate to Characters"),
    Quests(Screen.QUESTS, Icons.AutoMirrored.Filled.Assignment, "Quests", "Navigate to Quests"),
    Sessions(Screen.SESSIONS, Icons.Default.Event, "Sessions", "Navigate to Sessions"),
    Encounters(Screen.ENCOUNTERS, Icons.Default.Security, "Encounters", "Navigate to Encounters"),
    Maps(Screen.MAPS, Icons.Default.Map, "Maps", "Navigate to Maps"),
    Dice(Screen.DICE, Icons.Default.Casino, "Dice", "Navigate to Dice"),
    Settings(Screen.SETTINGS, Icons.Default.Settings, "Settings", "Navigate to Settings"),
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
    expanded: Boolean,
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
            .padding(
                horizontal = if (expanded) 12.dp else 8.dp,
                vertical = 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = textColor,
            modifier = Modifier.size(20.dp)
        )
        if (expanded) {
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
}
