package com.kavita.reader.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kavita.reader.domain.DownloadStatus
import com.kavita.reader.domain.ReaderTheme

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey val id: Long = 1,
    val baseUrl: String,
    val displayName: String = "Kavita",
    val authMode: String = "Rest"
)

@Entity(tableName = "libraries")
data class LibraryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String
)

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: Int,
    val libraryId: Int,
    val title: String,
    val sortName: String,
    val coverUrl: String?
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val bookId: Int,
    val status: DownloadStatus,
    val localPath: String?,
    val bytesDownloaded: Long,
    val totalBytes: Long?,
    val errorMessage: String?
)

@Entity(tableName = "reading_progress")
data class ReadingProgressEntity(
    @PrimaryKey val bookId: Int,
    val spineIndex: Int,
    val scrollPercent: Float,
    val updatedAtMillis: Long
)

@Entity(tableName = "reader_settings")
data class ReaderSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val fontSizeSp: Int,
    val fontFamily: String,
    val lineHeight: Float,
    val marginDp: Int,
    val theme: ReaderTheme,
    val customBackground: String,
    val customForeground: String
)
