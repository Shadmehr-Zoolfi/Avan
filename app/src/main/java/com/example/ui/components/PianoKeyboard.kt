package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NoteFeedbackType
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NoteCorrectGreen
import com.example.ui.theme.NoteWrongRed
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.PianoBlackKey
import com.example.ui.theme.PianoKeyBorder
import com.example.ui.theme.PianoWhiteKey
import com.example.util.PersianUtils

@Composable
fun PianoKeyboard(
    modifier: Modifier = Modifier,
    activeNoteFeedback: Pair<Int, NoteFeedbackType>? = null,
    highlightedMidi: Int? = null,
    onNotePressed: (Int) -> Unit
) {
    // Current base octave: 60 = Middle C (C4)
    var baseMidi by remember { mutableIntStateOf(60) }

    // White key semitone offsets in an octave: C(0), D(2), E(4), F(5), G(7), A(9), B(11)
    val whiteOffsets = listOf(0, 2, 4, 5, 7, 9, 11)

    // Two octaves of white keys (14 white keys total)
    val totalWhiteKeys = 14
    val whiteKeyMidis = remember(baseMidi) {
        val list = mutableListOf<Int>()
        for (oct in 0..1) {
            for (off in whiteOffsets) {
                list.add(baseMidi + (oct * 12) + off)
            }
        }
        list
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(175.dp)
            .background(ObsidianDeep)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val keyWidth = maxWidth / totalWhiteKeys
            val blackKeyWidth = keyWidth * 0.65f
            val blackKeyHeight = maxHeight * 0.60f

            // 1. Draw White Keys
            Row(modifier = Modifier.fillMaxSize()) {
                whiteKeyMidis.forEachIndexed { index, midi ->
                    val isFeedbackCorrect = activeNoteFeedback?.first == midi && activeNoteFeedback.second == NoteFeedbackType.CORRECT
                    val isFeedbackWrong = activeNoteFeedback?.first == midi && activeNoteFeedback.second == NoteFeedbackType.WRONG_PITCH
                    val isHighlighted = highlightedMidi == midi

                    var isPressed by remember { mutableStateOf(false) }

                    val keyBg by animateColorAsState(
                        targetValue = when {
                            isFeedbackCorrect -> NoteCorrectGreen
                            isFeedbackWrong -> NoteWrongRed
                            isHighlighted -> GoldPrimary.copy(alpha = 0.85f)
                            isPressed -> Color(0xFFCBD5E1)
                            else -> PianoWhiteKey
                        },
                        label = "whiteKeyBg"
                    )

                    Box(
                        modifier = Modifier
                            .width(keyWidth)
                            .fillMaxHeight()
                            .padding(horizontal = 1.dp)
                            .shadow(2.dp, RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                            .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                            .background(keyBg)
                            .border(1.dp, PianoKeyBorder, RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                            .pointerInput(midi) {
                                detectTapGestures(
                                    onPress = {
                                        isPressed = true
                                        onNotePressed(midi)
                                        tryAwaitRelease()
                                        isPressed = false
                                    }
                                )
                            }
                            .testTag("white_key_$midi"),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Persian note label
                        Text(
                            text = PersianUtils.getShortPersianNote(midi),
                            fontSize = 11.sp,
                            color = if (isFeedbackCorrect || isFeedbackWrong || isHighlighted) Color.White else Color(0xFF334155),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            // 2. Draw Black Keys in overlay
            // In an octave, black keys follow white keys at indices:
            // C(0)->C#, D(1)->D#, F(3)->F#, G(4)->G#, A(5)->A#
            val blackKeyIndices = listOf(0, 1, 3, 4, 5, 7, 8, 10, 11, 12)
            blackKeyIndices.forEach { whiteIdx ->
                val baseWhiteMidi = whiteKeyMidis[whiteIdx]
                val blackMidi = baseWhiteMidi + 1
                val xOffset = (keyWidth * (whiteIdx + 1)) - (blackKeyWidth / 2)

                val isFeedbackCorrect = activeNoteFeedback?.first == blackMidi && activeNoteFeedback.second == NoteFeedbackType.CORRECT
                val isFeedbackWrong = activeNoteFeedback?.first == blackMidi && activeNoteFeedback.second == NoteFeedbackType.WRONG_PITCH
                val isHighlighted = highlightedMidi == blackMidi

                var isBlackPressed by remember { mutableStateOf(false) }

                val blackBg by animateColorAsState(
                    targetValue = when {
                        isFeedbackCorrect -> NoteCorrectGreen
                        isFeedbackWrong -> NoteWrongRed
                        isHighlighted -> GoldPrimary
                        isBlackPressed -> Color(0xFF334155)
                        else -> PianoBlackKey
                    },
                    label = "blackKeyBg"
                )

                Box(
                    modifier = Modifier
                        .offset(x = xOffset)
                        .width(blackKeyWidth)
                        .height(blackKeyHeight)
                        .shadow(4.dp, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                        .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(blackBg.copy(alpha = 0.9f), blackBg)
                            )
                        )
                        .border(1.dp, Color(0xFF475569), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                        .pointerInput(blackMidi) {
                            detectTapGestures(
                                onPress = {
                                    isBlackPressed = true
                                    onNotePressed(blackMidi)
                                    tryAwaitRelease()
                                    isBlackPressed = false
                                }
                            )
                        }
                        .testTag("black_key_$blackMidi")
                )
            }
        }

        // Octave Switcher Pills at the top
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 6.dp, top = 4.dp)
                .background(Color(0xCC0F172A), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (baseMidi > 36) baseMidi -= 12 },
                modifier = Modifier.padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "اکتاو پایین‌تر",
                    tint = Color.White
                )
            }

            Text(
                text = "اکتاو ${PersianUtils.toPersianDigits((baseMidi / 12) - 1)}",
                color = GoldPrimary,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(
                onClick = { if (baseMidi < 72) baseMidi += 12 },
                modifier = Modifier.padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "اکتاو بالاتر",
                    tint = Color.White
                )
            }
        }
    }
}
