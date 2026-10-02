package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.HandType
import com.example.model.NoteFeedbackType
import com.example.model.PianoNote
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GoldPrimaryLight
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.NoteCorrectGreen
import com.example.ui.theme.NoteWrongRed

@Composable
fun FallingNotesView(
    modifier: Modifier = Modifier,
    notes: List<PianoNote>,
    currentBeat: Float,
    selectedHand: HandType = HandType.BOTH,
    activeFeedback: Pair<Int, NoteFeedbackType>? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MidnightDark)
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Target hit line (where user should play the note) at 85% of height
            val hitLineY = canvasHeight * 0.85f

            // Draw subtle vertical grid columns representing pitch lanes (MIDI 48 to 84 = 36 notes)
            val minMidi = 48
            val maxMidi = 84
            val midiRange = maxMidi - minMidi

            for (m in minMidi..maxMidi step 2) {
                val x = ((m - minMidi).toFloat() / midiRange) * canvasWidth
                drawLine(
                    color = Color(0x1538BDF8),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 1f
                )
            }

            // Draw horizontal measure lines (e.g. every 4 beats)
            val beatsVisible = 6.0f // Visible lookahead window in beats
            val pixelsPerBeat = hitLineY / beatsVisible

            for (beat in 0..64 step 2) {
                val relativeBeat = beat - currentBeat
                if (relativeBeat in -1.0f..beatsVisible) {
                    val y = hitLineY - (relativeBeat * pixelsPerBeat)
                    drawLine(
                        color = Color(0x33475569),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = if (beat % 4 == 0) 2f else 1f
                    )
                }
            }

            // Draw Hit Line Baseline with glow
            drawLine(
                color = GoldPrimary,
                start = Offset(0f, hitLineY),
                end = Offset(canvasWidth, hitLineY),
                strokeWidth = 3f
            )

            // Draw active notes
            notes.forEach { note ->
                if (selectedHand == HandType.BOTH || selectedHand == note.hand) {
                    val relativeStartBeat = note.startBeat - currentBeat
                    val relativeEndBeat = (note.startBeat + note.durationBeats) - currentBeat

                    // Only draw notes that are inside or near the viewport
                    if (relativeEndBeat >= -0.5f && relativeStartBeat <= beatsVisible) {
                        val bottomY = hitLineY - (relativeStartBeat * pixelsPerBeat)
                        val topY = hitLineY - (relativeEndBeat * pixelsPerBeat)
                        val noteHeight = maxOf(bottomY - topY, 18f)

                        val noteX = ((note.midi - minMidi).toFloat() / midiRange) * canvasWidth
                        val noteWidth = maxOf(canvasWidth / 24f, 18f)

                        val isRightHand = note.hand == HandType.RIGHT
                        val isNearHit = Math.abs(currentBeat - note.startBeat) < 0.4f

                        val noteColor = when {
                            isNearHit && activeFeedback?.second == NoteFeedbackType.CORRECT -> NoteCorrectGreen
                            isNearHit && activeFeedback?.second == NoteFeedbackType.WRONG_PITCH -> NoteWrongRed
                            isRightHand -> GoldPrimaryLight
                            else -> CyanAccent
                        }

                        // Draw falling pill block
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(noteColor.copy(alpha = 0.95f), noteColor.copy(alpha = 0.65f)),
                                startY = topY,
                                endY = bottomY
                            ),
                            topLeft = Offset(noteX - noteWidth / 2, topY),
                            size = Size(noteWidth, noteHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )

                        // Highlight border
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.6f),
                            topLeft = Offset(noteX - noteWidth / 2, topY),
                            size = Size(noteWidth, noteHeight),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
                        )
                    }
                }
            }

            // Glow flare on hit line if correct note was just hit
            if (activeFeedback?.second == NoteFeedbackType.CORRECT) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NoteCorrectGreen, Color.Transparent),
                        center = Offset(canvasWidth / 2, hitLineY),
                        radius = 80f
                    ),
                    center = Offset(canvasWidth / 2, hitLineY),
                    radius = 80f
                )
            }
        }
    }
}
