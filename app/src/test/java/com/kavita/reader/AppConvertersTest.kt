package com.kavita.reader

import com.kavita.reader.data.AppConverters
import com.kavita.reader.domain.DownloadStatus
import com.kavita.reader.domain.ReaderTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class AppConvertersTest {
    private val converters = AppConverters()

    @Test
    fun roundTripsDownloadStatus() {
        val value = converters.downloadStatusToString(DownloadStatus.Downloaded)
        assertEquals(DownloadStatus.Downloaded, converters.stringToDownloadStatus(value))
    }

    @Test
    fun roundTripsReaderTheme() {
        val value = converters.readerThemeToString(ReaderTheme.Sepia)
        assertEquals(ReaderTheme.Sepia, converters.stringToReaderTheme(value))
    }
}
