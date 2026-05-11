package com.kavita.reader.reader

import com.kavita.reader.data.KavitaDao
import com.kavita.reader.data.toDomain
import com.kavita.reader.data.toEntity
import com.kavita.reader.domain.ReadingProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReaderRepository @Inject constructor(
    private val dao: KavitaDao
) {
    private val parser = EpubParser()

    fun progress(bookId: Int): Flow<ReadingProgress?> =
        dao.observeProgress(bookId).map { it?.toDomain() }

    suspend fun open(bookId: Int): EpubBook {
        val path = dao.getDownload(bookId)?.localPath ?: error("Book is not downloaded")
        return parser.parse(File(path))
    }

    suspend fun saveProgress(bookId: Int, spineIndex: Int, scrollPercent: Float) {
        dao.saveProgress(
            ReadingProgress(
                bookId = bookId,
                spineIndex = spineIndex,
                scrollPercent = scrollPercent.coerceIn(0f, 1f),
                updatedAtMillis = System.currentTimeMillis()
            ).toEntity()
        )
    }
}
