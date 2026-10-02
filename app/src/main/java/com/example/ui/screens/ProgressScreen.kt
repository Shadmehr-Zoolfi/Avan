package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PianoBookRecommendation
import com.example.model.PracticeSessionRecord
import com.example.ui.components.MetricProgressBar
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldPrimaryLight
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.NoteCorrectGreen
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardStroke
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AvanViewModel
import com.example.util.PersianUtils

@Composable
fun ProgressScreen(
    viewModel: AvanViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val sessions by viewModel.practiceSessions.collectAsState()
    val books by viewModel.recommendedBooks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDeep)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "تحلیل پیشرفت و کارنامه نوازندگی",
            color = GoldPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "این آمار برای ارزیابی مداوم و تنظیم تمرینات توسط معلم خصوصی پیانو ثبت می‌شود.",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Level Summary Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF261D12), MidnightDark)
                    )
                )
                .border(1.dp, GoldPrimary, RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "سطح فعلی نوازندگی",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = userProfile.currentLevel,
                            color = GoldPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33F59E0B))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "تحلیل پیوسته",
                            color = GoldPrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                MetricProgressBar(
                    label = "دقت فرکانسی نت‌ها",
                    percentage = 87,
                    icon = Icons.Default.MusicNote,
                    accentColor = GoldPrimary
                )

                MetricProgressBar(
                    label = "دقت ریتم و ضرب‌آهنگ",
                    percentage = 82,
                    icon = Icons.Default.Timer,
                    accentColor = CyanAccent
                )

                MetricProgressBar(
                    label = "ثبات تمپو و مترونوم درونی",
                    percentage = 76,
                    icon = Icons.Default.Speed,
                    accentColor = NoteCorrectGreen
                )

                MetricProgressBar(
                    label = "کنترل تکنیکی و هماهنگی دو دست",
                    percentage = 80,
                    icon = Icons.Default.Star,
                    accentColor = Color(0xFFA78BFA)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Piano Book Recommendations (Section 12 of brief)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "کتاب‌ها و منابع پیشنهادی آوان",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        books.forEach { book ->
            PianoBookCard(book = book)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Recent Sessions History
        Text(
            text = "جلسات اخیر تمرین و بازخورد معلم",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioCardBg)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "هنوز جلسه تمرین کامل ثبت نشده است. با زدن «شروع تمرین»، اولین جلسه را برگزار کنید.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        } else {
            sessions.take(5).forEach { session ->
                SessionHistoryCard(session = session)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun PianoBookCard(book: PianoBookRecommendation) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StudioCardBg)
            .border(1.dp, StudioCardStroke, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = book.persianTitle,
                    color = GoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22F59E0B))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = book.difficulty,
                        color = GoldPrimaryLight,
                        fontSize = 10.sp
                    )
                }
            }

            Text(
                text = book.author,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = book.description,
                color = TextPrimary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF131B2A))
                    .padding(8.dp)
            ) {
                Text(
                    text = "🎯 دلیل پیشنهاد آوان: ${book.whyRecommended}",
                    color = CyanAccent,
                    fontSize = 11.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(session: PracticeSessionRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StudioCardBg)
            .border(1.dp, StudioCardStroke, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = session.songTitle,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "دقت: ${PersianUtils.formatPercentage(session.accuracyPercent)}",
                    color = NoteCorrectGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = session.teacherFeedback,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "دستور بعدی: ${session.recommendedNextStep}",
                color = GoldPrimaryLight,
                fontSize = 11.sp
            )
        }
    }
}
