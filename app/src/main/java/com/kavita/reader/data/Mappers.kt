package com.kavita.reader.data

import com.kavita.reader.domain.Book
import com.kavita.reader.domain.DownloadRecord
import com.kavita.reader.domain.Library
import com.kavita.reader.domain.ReaderSettings
import com.kavita.reader.domain.ReadingProgress
import com.kavita.reader.domain.ServerConfig

fun ServerEntity.toDomain() = ServerConfig(id = id, baseUrl = baseUrl, displayName = displayName, authMode = authMode)
fun ServerConfig.toEntity() = ServerEntity(id = id, baseUrl = baseUrl, displayName = displayName, authMode = authMode)

fun LibraryEntity.toDomain() = Library(id = id, name = name, type = type)
fun Library.toEntity() = LibraryEntity(id = id, name = name, type = type)

fun BookEntity.toDomain() = Book(id = id, libraryId = libraryId, title = title, sortName = sortName, coverUrl = coverUrl)
fun Book.toEntity() = BookEntity(id = id, libraryId = libraryId, title = title, sortName = sortName, coverUrl = coverUrl)

fun DownloadEntity.toDomain() = DownloadRecord(
    bookId = bookId,
    status = status,
    localPath = localPath,
    bytesDownloaded = bytesDownloaded,
    totalBytes = totalBytes,
    errorMessage = errorMessage
)

fun DownloadRecord.toEntity() = DownloadEntity(
    bookId = bookId,
    status = status,
    localPath = localPath,
    bytesDownloaded = bytesDownloaded,
    totalBytes = totalBytes,
    errorMessage = errorMessage
)

fun ReadingProgressEntity.toDomain() = ReadingProgress(
    bookId = bookId,
    spineIndex = spineIndex,
    scrollPercent = scrollPercent,
    updatedAtMillis = updatedAtMillis
)

fun ReadingProgress.toEntity() = ReadingProgressEntity(
    bookId = bookId,
    spineIndex = spineIndex,
    scrollPercent = scrollPercent,
    updatedAtMillis = updatedAtMillis
)

fun ReaderSettingsEntity.toDomain() = ReaderSettings(
    fontSizeSp = fontSizeSp,
    fontFamily = fontFamily,
    lineHeight = lineHeight,
    marginDp = marginDp,
    theme = theme,
    customBackground = customBackground,
    customForeground = customForeground
)

fun ReaderSettings.toEntity() = ReaderSettingsEntity(
    fontSizeSp = fontSizeSp,
    fontFamily = fontFamily,
    lineHeight = lineHeight,
    marginDp = marginDp,
    theme = theme,
    customBackground = customBackground,
    customForeground = customForeground
)
