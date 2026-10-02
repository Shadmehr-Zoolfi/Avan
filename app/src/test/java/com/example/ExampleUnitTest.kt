package com.example

import com.example.util.PersianUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun persianDigitsConversion_isCorrect() {
        assertEquals("۱۲۳۴۵", PersianUtils.toPersianDigits(12345))
        assertEquals("۰", PersianUtils.toPersianDigits(0))
    }

    @Test
    fun priceFormatting_isCorrect() {
        assertEquals("۶۰۰٬۰۰۰ تومان", PersianUtils.formatPriceToman(600000L))
        assertEquals("۴٬۵۰۰٬۰۰۰ تومان", PersianUtils.formatPriceToman(4500000L))
    }

    @Test
    fun noteNaming_isCorrect() {
        // MIDI 60 is Middle C (C4)
        val shortName = PersianUtils.getShortPersianNote(60)
        assertEquals("دو", shortName)

        val fullPersianName = PersianUtils.getPersianNoteName(60)
        assert(fullPersianName.contains("دو"))
        assert(fullPersianName.contains("C4"))
    }
}

