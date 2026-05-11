package com.kavita.reader.reader

object ProgressCalculator {
    fun chapterProgress(firstVisibleIndex: Int, totalParagraphs: Int): Float {
        if (totalParagraphs <= 0) return 0f
        return (firstVisibleIndex.toFloat() / totalParagraphs.toFloat()).coerceIn(0f, 1f)
    }
}
