package com.kavita.reader

import com.kavita.reader.reader.ProgressCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {
    @Test
    fun returnsZeroForEmptyChapter() {
        assertEquals(0f, ProgressCalculator.chapterProgress(10, 0))
    }

    @Test
    fun clampsProgressWithinBounds() {
        assertEquals(1f, ProgressCalculator.chapterProgress(20, 10))
        assertEquals(0f, ProgressCalculator.chapterProgress(-2, 10))
    }

    @Test
    fun calculatesVisibleIndexProgress() {
        assertEquals(0.5f, ProgressCalculator.chapterProgress(5, 10))
    }
}
