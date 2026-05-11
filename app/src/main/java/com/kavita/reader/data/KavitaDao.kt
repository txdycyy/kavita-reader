package com.kavita.reader.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KavitaDao {
    @Query("SELECT * FROM servers WHERE id = 1")
    fun observeServer(): Flow<ServerEntity?>

    @Query("SELECT * FROM servers WHERE id = 1")
    suspend fun getServer(): ServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveServer(server: ServerEntity)

    @Query("DELETE FROM servers")
    suspend fun clearServers()

    @Query("SELECT * FROM libraries ORDER BY name")
    fun observeLibraries(): Flow<List<LibraryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLibraries(libraries: List<LibraryEntity>)

    @Query("SELECT * FROM books WHERE libraryId = :libraryId ORDER BY sortName")
    fun observeBooks(libraryId: Int): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBook(bookId: Int): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBooks(books: List<BookEntity>)

    @Query("SELECT * FROM downloads")
    fun observeDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE bookId = :bookId")
    suspend fun getDownload(bookId: Int): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE bookId = :bookId")
    suspend fun deleteDownload(bookId: Int)

    @Query("SELECT * FROM reading_progress WHERE bookId = :bookId")
    fun observeProgress(bookId: Int): Flow<ReadingProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: ReadingProgressEntity)

    @Query("SELECT * FROM reader_settings WHERE id = 1")
    fun observeReaderSettings(): Flow<ReaderSettingsEntity?>

    @Query("SELECT * FROM reader_settings WHERE id = 1")
    suspend fun getReaderSettings(): ReaderSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReaderSettings(settings: ReaderSettingsEntity)
}
