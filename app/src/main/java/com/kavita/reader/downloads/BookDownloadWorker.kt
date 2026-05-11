package com.kavita.reader.downloads

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.kavita.reader.auth.AuthRepository
import com.kavita.reader.data.DownloadEntity
import com.kavita.reader.data.KavitaClientFactory
import com.kavita.reader.data.KavitaDao
import com.kavita.reader.data.OpdsClient
import com.kavita.reader.domain.DownloadStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class BookDownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val authRepository: AuthRepository,
    private val clientFactory: KavitaClientFactory,
    private val opdsClient: OpdsClient,
    private val dao: KavitaDao
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val bookId = inputData.getInt(KEY_BOOK_ID, -1)
        if (bookId <= 0) return Result.failure()

        return try {
            dao.saveDownload(
                DownloadEntity(
                    bookId = bookId,
                    status = DownloadStatus.Running,
                    localPath = null,
                    bytesDownloaded = 0,
                    totalBytes = null,
                    errorMessage = null
                )
            )

            val server = authRepository.serverConfig()
            val apiKey = authRepository.apiKey()
            val body = if (server.authMode == "Opds") {
                opdsClient.downloadFirstEpub(server.baseUrl, apiKey, bookId)
            } else {
                clientFactory.create(server.baseUrl).downloadSeries(apiKey, bookId)
            }
            val destinationDir = File(context.filesDir, "books").apply { mkdirs() }
            val destination = File(destinationDir, "$bookId.epub")
            val totalBytes = body.contentLength().takeIf { it >= 0 }
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                    }
                }
            }

            dao.saveDownload(
                DownloadEntity(
                    bookId = bookId,
                    status = DownloadStatus.Downloaded,
                    localPath = destination.absolutePath,
                    bytesDownloaded = downloadedBytes,
                    totalBytes = totalBytes,
                    errorMessage = null
                )
            )
            Result.success()
        } catch (error: Exception) {
            dao.saveDownload(
                DownloadEntity(
                    bookId = bookId,
                    status = DownloadStatus.Failed,
                    localPath = null,
                    bytesDownloaded = 0,
                    totalBytes = null,
                    errorMessage = error.message ?: "Download failed"
                )
            )
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_BOOK_ID = "book_id"
    }
}
