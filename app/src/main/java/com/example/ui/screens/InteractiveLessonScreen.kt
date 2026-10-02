package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HandType
import com.example.model.NoteFeedbackType
import com.example.ui.components.FallingNotesView
import com.example.ui.components.PianoKeyboard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldPrimaryLight
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.NoteCorrectGreen
import com.example.ui.theme.NoteWrongRed
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardStroke
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AvanViewModel
import com.example.util.PersianUtils

@Composable
fun InteractiveLessonScreen(
    viewModel: AvanViewModel
) {
    val selectedSong by viewModel.selectedSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentBeat by viewModel.currentBeat.collectAsState()
    val currentBpm by viewModel.currentBpm.collectAsState()
    val selectedHand by viewModel.selectedHand.collectAsState()
    val isMicActive by viewModel.isMicrophoneDetectionActive.collectAsState()
    val activeFeedback by viewModel.activeNoteFeedback.collectAsState()
    val tempoCoachMsg by viewModel.tempoCoachMessage.collectAsState()
    val postPracticeRecord by viewModel.postPracticeAnalysis.collectAsState()

    val totalSongBeats = selectedSong.notes.maxOfOrNull { it.startBeat + it.durationBeats } ?: 32f
    val progress = (currentBeat / totalSongBeats).coerceIn(0f, 1f)
    val currentBar = ((currentBeat / 4).toInt() + 1).coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDeep)
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Song Title & Teacher Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = selectedSong.persianTitle,
                    color = GoldPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${selectedSong.composer} • ${selectedSong.difficulty}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Real-time Mic Listening Switcher
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isMicActive) Color(0x3310B981) else StudioCardBg)
                    .border(1.dp, if (isMicActive) NoteCorrectGreen else StudioCardStroke, RoundedCornerShape(12.dp))
                    .clickable { viewModel.toggleMicrophoneDetection() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("toggle_mic_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isMicActive) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = null,
                        tint = if (isMicActive) NoteCorrectGreen else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMicActive) "شنود پیانوی واقعی" else "نواختن با لمس",
                        color = if (isMicActive) NoteCorrectGreen else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress bar & Bar/Measure indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "میزان ${PersianUtils.toPersianDigits(currentBar)}",
                color = GoldPrimaryLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${PersianUtils.formatPercentage((progress * 100).toInt())} تکمیل شده",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = GoldPrimary,
            trackColor = StudioCardBg
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tempo Coach Notification Banner
        AnimatedVisibility(visible = tempoCoachMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF241E15))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧠", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tempoCoachMsg ?: "",
                        color = GoldPrimaryLight,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Visual Falling Notes Lane
        FallingNotesView(
            notes = selectedSong.notes,
            currentBeat = currentBeat,
            selectedHand = selectedHand,
            activeFeedback = activeFeedback
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Real-time Note Feedback Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (activeFeedback != null) {
                val fbType = activeFeedback!!.second
                val feedbackBg = when (fbType) {
                    NoteFeedbackType.CORRECT -> NoteCorrectGreen
                    NoteFeedbackType.WRONG_PITCH -> NoteWrongRed
                    NoteFeedbackType.TOO_EARLY, NoteFeedbackType.TOO_LATE -> Color(0xFFF59E0B)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(feedbackBg.copy(alpha = 0.25f))
                        .border(1.dp, feedbackBg, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${PersianUtils.getShortPersianNote(activeFeedback!!.first)}: ${fbType.persianLabel}",
                        color = feedbackBg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "منتظر لمس یا شنیدن نت پیانو...",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interactive Piano Keyboard
        PianoKeyboard(
            activeNoteFeedback = activeFeedback,
            onNotePressed = { midi ->
                viewModel.onUserPlayNote(midi)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Playback Controls Row: Play/Pause, Restart, Tempo +/-
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MidnightDark)
                .border(1.dp, StudioCardStroke, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Restart
            IconButton(
                onClick = { viewModel.restartPlayback() },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(StudioCardBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "شروع مجدد",
                    tint = TextPrimary
                )
            }

            // Play / Pause main button
            Button(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .height(48.dp)
                    .testTag("play_pause_button"),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = ObsidianDeep
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlaying) "توقف تمرین" else "شروع تمرین",
                        color = ObsidianDeep,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Tempo Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.setTempo(currentBpm - 5) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "کاهش سرعت", tint = Color.White)
                }

                Text(
                    text = PersianUtils.formatBpm(currentBpm),
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { viewModel.setTempo(currentBpm + 5) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "افزایش سرعت", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hand Selection Tabs (دست راست، دست چپ، هر دو دست)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MidnightDark)
                .border(1.dp, StudioCardStroke, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HandType.values().forEach { hand ->
                val isSel = selectedHand == hand
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) GoldPrimary else Color.Transparent)
                        .clickable { viewModel.setHandSelection(hand) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = hand.persianLabel,
                        color = if (isSel) ObsidianDeep else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Post-Practice Mistake Analysis Dialog (Exact Persian requirement)
    if (postPracticeRecord != null) {
        val session = postPracticeRecord!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissPostPracticeDialog() },
            containerColor = StudioCardBg,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = "تحلیل اجرای شما",
                    color = GoldPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "دقت نت‌ها:", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = PersianUtils.formatPercentage(session.accuracyPercent),
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "دقت ریتم:", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = PersianUtils.formatPercentage(session.rhythmPercent),
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "تعداد خطاها:", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = PersianUtils.toPersianDigits(session.mistakesCount),
                            color = NoteWrongRed,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "بخش نیازمند تمرکز:", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = session.problematicSection,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MidnightDark)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "تمرین پیشنهادی آوان:",
                                color = GoldPrimaryLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = session.teacherFeedback,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "راهکار بعدی: ${session.recommendedNextStep}",
                                color = CyanAccent,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissPostPracticeDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(text = "متوجه شدم", color = ObsidianDeep, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
