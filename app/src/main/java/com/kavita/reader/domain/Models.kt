package com.kavita.reader.domain

enum class DownloadStatus {
    Queued,
    Running,
    Downloaded,
    Failed
}

enum class ReaderTheme {
    Light,
    Dark,
    Sepia,
    Custom
}

data class ServerConfig(
    val id: Long = 1,
    val baseUrl: String,
    val displayName: String = "Kavita",
    val authMode: String = "Rest"
)

data class Library(
    val id: Int,
    val name: String,
    val type: String
)

data class Book(
    val id: Int,
    val libraryId: Int,
    val title: String,
    val sortName: String,
    val coverUrl: String? = null
)

data class DownloadRecord(
    val bookId: Int,
    val status: DownloadStatus,
    val localPath: String? = null,
    val bytesDownloaded: Long = 0,
    val totalBytes: Long? = null,
    val errorMessage: String? = null
)

data class ReadingProgress(
    val bookId: Int,
    val spineIndex: Int,
    val scrollPercent: Float,
    val updatedAtMillis: Long
)

data class ReaderSettings(
    val fontSizeSp: Int = 18,
    val fontFamily: String = "serif",
    val lineHeight: Float = 1.55f,
    val marginDp: Int = 20,
    val theme: ReaderTheme = ReaderTheme.Light,
    val customBackground: String = "#FFFFFF",
    val customForeground: String = "#111111"
)
