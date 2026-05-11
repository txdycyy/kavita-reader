package com.kavita.reader

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kavita.reader.auth.AuthRepository
import com.kavita.reader.downloads.DownloadRepository
import com.kavita.reader.domain.Book
import com.kavita.reader.domain.DownloadRecord
import com.kavita.reader.domain.Library
import com.kavita.reader.domain.ReaderSettings
import com.kavita.reader.domain.ServerConfig
import com.kavita.reader.library.LibraryRepository
import com.kavita.reader.reader.EpubBook
import com.kavita.reader.reader.ReaderRepository
import com.kavita.reader.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val readerRepository: ReaderRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val selectedLibraryId = MutableStateFlow<Int?>(null)
    private val selectedBookId = MutableStateFlow<Int?>(null)
    private val readerBook = MutableStateFlow<EpubBook?>(null)
    private val busyMessage = MutableStateFlow<String?>(null)
    private val errorMessage = MutableStateFlow<String?>(null)
    private var booksJob: Job? = null

    val uiState: StateFlow<MainUiState> = combine(
        authRepository.server,
        libraryRepository.libraries,
        selectedLibraryId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else libraryRepository.books(id)
        },
        downloadRepository.downloads,
        settingsRepository.readerSettings,
        selectedLibraryId,
        selectedBookId,
        readerBook,
        busyMessage,
        errorMessage
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val server = values[0] as ServerConfig?
        val libraries = values[1] as List<Library>
        val books = values[2] as List<Book>
        val downloads = values[3] as List<DownloadRecord>
        val settings = values[4] as ReaderSettings
        val libraryId = values[5] as Int?
        val bookId = values[6] as Int?
        val openBook = values[7] as EpubBook?
        val busy = values[8] as String?
        val error = values[9] as String?

        MainUiState(
            server = server,
            libraries = libraries,
            selectedLibraryId = libraryId,
            books = books,
            downloads = downloads.associateBy { it.bookId },
            selectedBook = books.firstOrNull { it.id == bookId },
            readerBook = openBook,
            readerSettings = settings,
            busyMessage = busy,
            errorMessage = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    fun connect(serverUrl: String, apiKey: String) {
        viewModelScope.launch {
            runBusy("Testing connection") {
                authRepository.connect(serverUrl, apiKey).getOrThrow()
                libraryRepository.refreshLibraries()
            }
        }
    }

    fun refreshLibraries() {
        viewModelScope.launch {
            runBusy("Refreshing libraries") { libraryRepository.refreshLibraries() }
        }
    }

    fun selectLibrary(libraryId: Int) {
        selectedLibraryId.value = libraryId
        booksJob?.cancel()
        booksJob = viewModelScope.launch {
            runBusy("Loading books") { libraryRepository.refreshBooks(libraryId) }
        }
    }

    fun selectBook(bookId: Int) {
        readerBook.value = null
        selectedBookId.value = bookId
    }

    fun enqueueDownload(bookId: Int) {
        downloadRepository.enqueue(bookId)
    }

    fun deleteDownload(bookId: Int) {
        viewModelScope.launch {
            runBusy("Deleting download") { downloadRepository.delete(context, bookId) }
        }
    }

    fun openReader(bookId: Int) {
        viewModelScope.launch {
            runBusy("Opening EPUB") {
                selectedBookId.value = bookId
                readerBook.value = readerRepository.open(bookId)
            }
        }
    }

    fun closeReader() {
        readerBook.value = null
    }

    fun saveProgress(bookId: Int, spineIndex: Int, scrollPercent: Float) {
        viewModelScope.launch {
            readerRepository.saveProgress(bookId, spineIndex, scrollPercent)
        }
    }

    fun updateReaderSettings(transform: (ReaderSettings) -> ReaderSettings) {
        viewModelScope.launch {
            settingsRepository.saveReaderSettings(transform(uiState.value.readerSettings))
        }
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun disconnect() {
        viewModelScope.launch {
            authRepository.disconnect()
            selectedLibraryId.value = null
            selectedBookId.value = null
            readerBook.value = null
        }
    }

    private suspend fun runBusy(message: String, block: suspend () -> Unit) {
        busyMessage.value = message
        errorMessage.value = null
        try {
            block()
        } catch (error: Exception) {
            errorMessage.value = error.message ?: "Operation failed"
        } finally {
            busyMessage.value = null
        }
    }
}

data class MainUiState(
    val server: ServerConfig? = null,
    val libraries: List<Library> = emptyList(),
    val selectedLibraryId: Int? = null,
    val books: List<Book> = emptyList(),
    val downloads: Map<Int, DownloadRecord> = emptyMap(),
    val selectedBook: Book? = null,
    val readerBook: EpubBook? = null,
    val readerSettings: ReaderSettings = ReaderSettings(),
    val busyMessage: String? = null,
    val errorMessage: String? = null
) {
    val isConnected: Boolean = server != null
}
