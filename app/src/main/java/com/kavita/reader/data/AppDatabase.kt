package com.kavita.reader.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kavita.reader.domain.DownloadStatus
import com.kavita.reader.domain.ReaderTheme

@Database(
    entities = [
        ServerEntity::class,
        LibraryEntity::class,
        BookEntity::class,
        DownloadEntity::class,
        ReadingProgressEntity::class,
        ReaderSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun kavitaDao(): KavitaDao
}

class AppConverters {
    @TypeConverter
    fun downloadStatusToString(value: DownloadStatus): String = value.name

    @TypeConverter
    fun stringToDownloadStatus(value: String): DownloadStatus = DownloadStatus.valueOf(value)

    @TypeConverter
    fun readerThemeToString(value: ReaderTheme): String = value.name

    @TypeConverter
    fun stringToReaderTheme(value: String): ReaderTheme = ReaderTheme.valueOf(value)
}
