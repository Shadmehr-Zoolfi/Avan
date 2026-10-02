package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssessmentResult
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldPrimaryLight
import com.example.ui.theme.NoteCorrectGreen
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardStroke
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianUtils

@Composable
fun MetricProgressBar(
    label: String,
    percentage: Int,
    icon: ImageVector,
    accentColor: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        label = "metricProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = PersianUtils.formatPercentage(percentage),
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F172A))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(accentColor.copy(alpha = 0.7f), accentColor)
                        )
                    )
            )
        }
    }
}

@Composable
fun MusicianProfileCard(
    result: AssessmentResult,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(StudioCardBg, Color(0xFF141D2D))
                )
            )
            .border(1.dp, StudioCardStroke, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header: Overall Level Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "پروفایل نوازندگی شما",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = result.overallLevel,
                        color = GoldPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x33F59E0B))
                        .border(1.dp, GoldPrimaryLight, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "تخمین هوش مصنوعی آوان",
                        color = GoldPrimaryLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metric Progress Bars
            MetricProgressBar(
                label = "دقت فرکانسی نت‌ها",
                percentage = result.pitchAccuracy,
                icon = Icons.Default.MusicNote,
                accentColor = GoldPrimary
            )

            MetricProgressBar(
                label = "دقت ریتم و ضرب‌آهنگ",
                percentage = result.rhythmAccuracy,
                icon = Icons.Default.Timer,
                accentColor = CyanAccent
            )

            MetricProgressBar(
                label = "ثبات و پایداری تمپو",
                percentage = result.tempoStability,
                icon = Icons.Default.Speed,
                accentColor = NoteCorrectGreen
            )

            MetricProgressBar(
                label = "کنترل تکنیکی و لمس کلاویه",
                percentage = result.technicalControl,
                icon = Icons.Default.Star,
                accentColor = Color(0xFFA78BFA)
            )

            MetricProgressBar(
                label = "هماهنگی و استقلال دو دست",
                percentage = result.twoHandCoordination,
                icon = Icons.Default.Info,
                accentColor = Color(0xFFF472B6)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Strengths and Areas to Improve
            if (result.strengths.isNotEmpty()) {
                Text(
                    text = "نقاط قوت شما",
                    color = NoteCorrectGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                result.strengths.forEach { str ->
                    Row(
                        modifier = Modifier.padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NoteCorrectGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = str,
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (result.areasToImprove.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "چیزهایی که باید تقویت شوند",
                    color = Color(0xFFFB923C),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                result.areasToImprove.forEach { imp ->
                    Row(
                        modifier = Modifier.padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFB923C))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = imp,
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
