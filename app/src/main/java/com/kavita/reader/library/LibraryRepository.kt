package com.kavita.reader.library

import com.kavita.reader.auth.AuthRepository
import com.kavita.reader.data.BookEntity
import com.kavita.reader.data.KavitaClientFactory
import com.kavita.reader.data.KavitaDao
import com.kavita.reader.data.LibraryEntity
import com.kavita.reader.data.OpdsClient
import com.kavita.reader.data.SeriesRequestDto
import com.kavita.reader.data.toDomain
import com.kavita.reader.domain.Book
import com.kavita.reader.domain.Library
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val dao: KavitaDao,
    private val clientFactory: KavitaClientFactory,
    private val opdsClient: OpdsClient
) {
    val libraries: Flow<List<Library>> = dao.observeLibraries()
        .map { entities -> entities.map { it.toDomain() } }

    fun books(libraryId: Int): Flow<List<Book>> = dao.observeBooks(libraryId)
        .map { entities -> entities.map { it.toDomain() } }

    suspend fun refreshLibraries() {
        val server = authRepository.serverConfig()
        val apiKey = authRepository.apiKey()
        val libraries = if (server.authMode == "Opds") {
            opdsClient.libraries(server.baseUrl, apiKey)
                .map { LibraryEntity(id = it.id, name = it.title, type = "OPDS") }
        } else {
            clientFactory.create(server.baseUrl)
                .libraries(apiKey)
                .map { dto ->
                    LibraryEntity(
                        id = dto.id,
                        name = dto.name,
                        type = dto.type.toLibraryTypeLabel()
                    )
                }
        }
        dao.saveLibraries(libraries)
    }

    suspend fun refreshBooks(libraryId: Int) {
        val server = authRepository.serverConfig()
        val apiKey = authRepository.apiKey()
        if (server.authMode == "Opds") {
            dao.saveBooks(
                opdsClient.books(server.baseUrl, apiKey, libraryId).map { entry ->
                    BookEntity(
                        id = entry.id,
                        libraryId = libraryId,
                        title = entry.title,
                        sortName = entry.title,
                        coverUrl = null
                    )
                }
            )
            return
        }

        val client = clientFactory.create(server.baseUrl)
        val seriesList = client.series(apiKey, request = SeriesRequestDto(id = libraryId))
        dao.saveBooks(
            seriesList.map { series ->
                BookEntity(
                    id = series.id,
                    libraryId = libraryId,
                    title = series.localizedName ?: series.name,
                    sortName = series.sortName ?: series.name,
                    coverUrl = series.coverImage
                )
            }
        )
    }

    suspend fun book(bookId: Int): Book? = dao.getBook(bookId)?.toDomain()

    private fun Int?.toLibraryTypeLabel(): String = when (this) {
        0 -> "Manga"
        1 -> "Comic"
        2 -> "Book"
        3 -> "Image"
        4 -> "Light Novel"
        5 -> "Comic"
        else -> "Unknown"
    }
}
