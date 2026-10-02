package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldPrimaryLight
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardStroke
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppScreen

@Composable
fun AvanTopBar(
    userProfile: UserProfile,
    onChatClick: () -> Unit,
    onSubscriptionClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightDark, ObsidianDeep)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(GoldPrimary, Color(0xFFD97706))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "آوان",
                        tint = ObsidianDeep,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "آوان",
                        color = GoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "معلم هوشمند پیانو",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Right-side actions: Subscription badge & Teacher Chat icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Subscription badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (userProfile.isPremium) Color(0x33F59E0B) else Color(0x2294A3B8))
                        .border(
                            1.dp,
                            if (userProfile.isPremium) GoldPrimary else Color(0xFF475569),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onSubscriptionClick() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("subscription_badge")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (userProfile.isPremium) GoldPrimaryLight else Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (userProfile.isPremium) "ویژه" else "طرح رایگان",
                            color = if (userProfile.isPremium) GoldPrimaryLight else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Teacher Chat Button
                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(StudioCardBg)
                        .border(1.dp, StudioCardStroke, CircleShape)
                        .testTag("teacher_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "گفتگو با معلم آوان",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AvanBottomBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MidnightDark)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .navigationBarsPadding()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                title = "خانه",
                icon = Icons.Default.Home,
                selected = currentScreen == AppScreen.HOME,
                testTag = "nav_home",
                onClick = { onTabSelected(AppScreen.HOME) }
            )

            BottomNavItem(
                title = "یادگیری",
                icon = Icons.Default.PlayCircle,
                selected = currentScreen == AppScreen.LEARN,
                testTag = "nav_learn",
                onClick = { onTabSelected(AppScreen.LEARN) }
            )

            BottomNavItem(
                title = "تمرین",
                icon = Icons.Default.FitnessCenter,
                selected = currentScreen == AppScreen.PRACTICE,
                testTag = "nav_practice",
                onClick = { onTabSelected(AppScreen.PRACTICE) }
            )

            BottomNavItem(
                title = "قطعات",
                icon = Icons.Default.LibraryMusic,
                selected = currentScreen == AppScreen.SONGS,
                testTag = "nav_songs",
                onClick = { onTabSelected(AppScreen.SONGS) }
            )

            BottomNavItem(
                title = "پیشرفت",
                icon = Icons.Default.ShowChart,
                selected = currentScreen == AppScreen.PROGRESS,
                testTag = "nav_progress",
                onClick = { onTabSelected(AppScreen.PROGRESS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (selected) GoldPrimary else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            color = if (selected) GoldPrimary else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
