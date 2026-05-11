package com.kavita.reader.downloads

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kavita.reader.data.KavitaDao
import com.kavita.reader.data.toDomain
import com.kavita.reader.domain.DownloadRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val dao: KavitaDao,
    private val workManager: WorkManager
) {
    val downloads: Flow<List<DownloadRecord>> = dao.observeDownloads()
        .map { entities -> entities.map { it.toDomain() } }

    suspend fun downloadFor(bookId: Int): DownloadRecord? = dao.getDownload(bookId)?.toDomain()

    fun enqueue(bookId: Int) {
        val request = OneTimeWorkRequestBuilder<BookDownloadWorker>()
            .setInputData(Data.Builder().putInt(BookDownloadWorker.KEY_BOOK_ID, bookId).build())
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork("download-book-$bookId", ExistingWorkPolicy.REPLACE, request)
    }

    suspend fun delete(context: Context, bookId: Int) {
        workManager.cancelUniqueWork("download-book-$bookId")
        dao.getDownload(bookId)?.localPath?.let { File(it).delete() }
        dao.deleteDownload(bookId)
    }
}
