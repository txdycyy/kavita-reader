package com.kavita.reader

import com.kavita.reader.auth.UrlNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UrlNormalizerTest {
    @Test
    fun normalizeAddsHttpsAndTrailingSlash() {
        assertEquals("https://example.com/", UrlNormalizer.normalize("example.com"))
    }

    @Test
    fun normalizePreservesSubPathAndPort() {
        assertEquals(
            "http://example.test:5051/kavita/",
            UrlNormalizer.normalize("http://example.test:5051/kavita/")
        )
    }

    @Test
    fun normalizeRejectsUnsupportedScheme() {
        assertThrows(IllegalArgumentException::class.java) {
            UrlNormalizer.normalize("ftp://example.com")
        }
    }
}
