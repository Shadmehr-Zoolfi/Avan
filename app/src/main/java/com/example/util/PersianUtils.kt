package com.example.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object PersianUtils {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun toPersianDigits(text: String): String {
        val sb = java.lang.StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toPersianDigits(number: Int): String {
        return toPersianDigits(number.toString())
    }

    fun toPersianDigits(number: Long): String {
        return toPersianDigits(number.toString())
    }

    fun formatPriceToman(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,###")
        val formatted = formatter.format(amount)
        return "${toPersianDigits(formatted)} تومان"
    }

    fun formatPercentage(percent: Int): String {
        return "${toPersianDigits(percent)}٪"
    }

    fun formatBpm(bpm: Int): String {
        return "${toPersianDigits(bpm)} BPM"
    }

    // Persian musical note names mapping
    fun getPersianNoteName(midiNumber: Int): String {
        val noteNames = arrayOf("دو", "دو دیز", "ر", "ر دیز", "می", "فا", "فا دیز", "سل", "سل دیز", "لا", "لا دیز", "سی")
        val englishNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val noteIndex = (midiNumber % 12)
        val octave = (midiNumber / 12) - 1
        return "${noteNames[noteIndex]} ${toPersianDigits(octave)} (${englishNames[noteIndex]}$octave)"
    }

    fun getShortPersianNote(midiNumber: Int): String {
        val noteNames = arrayOf("دو", "دو♯", "ر", "ر♯", "می", "فا", "فا♯", "سل", "سل♯", "لا", "لا♯", "سی")
        val noteIndex = (midiNumber % 12)
        return noteNames[noteIndex]
    }

    fun getEnglishNoteName(midiNumber: Int): String {
        val englishNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        val noteIndex = (midiNumber % 12)
        val octave = (midiNumber / 12) - 1
        return "${englishNames[noteIndex]}$octave"
    }
}
