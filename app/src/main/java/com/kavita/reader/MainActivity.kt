package com.kavita.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kavita.reader.domain.Book
import com.kavita.reader.domain.DownloadRecord
import com.kavita.reader.domain.DownloadStatus
import com.kavita.reader.domain.Library
import com.kavita.reader.domain.ReaderSettings
import com.kavita.reader.domain.ReaderTheme
import com.kavita.reader.reader.EpubBook
import com.kavita.reader.reader.ProgressCalculator
import com.kavita.reader.ui.theme.KavitaReaderTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KavitaReaderTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                KavitaReaderApp(
                    state = state,
                    actions = AppActions(
                        connect = viewModel::connect,
                        refreshLibraries = viewModel::refreshLibraries,
                        selectLibrary = viewModel::selectLibrary,
                        selectBook = viewModel::selectBook,
                        enqueueDownload = viewModel::enqueueDownload,
                        deleteDownload = viewModel::deleteDownload,
                        openReader = viewModel::openReader,
                        closeReader = viewModel::closeReader,
                        saveProgress = viewModel::saveProgress,
                        updateSettings = viewModel::updateReaderSettings,
                        clearError = viewModel::clearError,
                        disconnect = viewModel::disconnect
                    )
                )
            }
        }
    }
}

data class AppActions(
    val connect: (String, String) -> Unit,
    val refreshLibraries: () -> Unit,
    val selectLibrary: (Int) -> Unit,
    val selectBook: (Int) -> Unit,
    val enqueueDownload: (Int) -> Unit,
    val deleteDownload: (Int) -> Unit,
    val openReader: (Int) -> Unit,
    val closeReader: () -> Unit,
    val saveProgress: (Int, Int, Float) -> Unit,
    val updateSettings: ((ReaderSettings) -> ReaderSettings) -> Unit,
    val clearError: () -> Unit,
    val disconnect: () -> Unit
)

@Composable
private fun KavitaReaderApp(state: MainUiState, actions: AppActions) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            actions.clearError()
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                AppTopBar(
                    state = state,
                    showReader = state.readerBook != null,
                    onBack = actions.closeReader,
                    onSettings = { showSettings = true },
                    onRefresh = actions.refreshLibraries,
                    onDisconnect = actions.disconnect
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when {
                    !state.isConnected -> ConnectScreen(state.busyMessage, actions.connect)
                    state.readerBook != null && state.selectedBook != null -> ReaderScreen(
                        book = state.selectedBook,
                        epubBook = state.readerBook,
                        settings = state.readerSettings,
                        onProgress = actions.saveProgress
                    )
                    else -> LibraryScreen(state, actions)
                }

                state.busyMessage?.let { message ->
                    BusyOverlay(message)
                }
            }
        }
    }

    if (showSettings) {
        ReaderSettingsDialog(
            settings = state.readerSettings,
            onDismiss = { showSettings = false },
            onUpdate = actions.updateSettings
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    state: MainUiState,
    showReader: Boolean,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onRefresh: () -> Unit,
    onDisconnect: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text("Kavita Reader", maxLines = 1, overflow = TextOverflow.Ellipsis)
                state.server?.let {
                    Text(it.baseUrl, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        },
        navigationIcon = {
            if (showReader) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (state.isConnected) {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Reader settings")
                }
                TextButton(onClick = onDisconnect) {
                    Text("Disconnect")
                }
            }
        }
    )
}

@Composable
private fun ConnectScreen(busyMessage: String?, onConnect: (String, String) -> Unit) {
    var serverUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Connect to Kavita", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = serverUrl,
            onValueChange = { serverUrl = it },
            label = { Text("Server URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("Auth Key") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { onConnect(serverUrl, apiKey) },
            enabled = busyMessage == null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test Connection")
        }
    }
}

@Composable
private fun LibraryScreen(state: MainUiState, actions: AppActions) {
    Row(Modifier.fillMaxSize().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LibraryList(
            libraries = state.libraries,
            selectedLibraryId = state.selectedLibraryId,
            onSelect = actions.selectLibrary,
            modifier = Modifier.weight(0.36f)
        )
        BookList(
            books = state.books,
            downloads = state.downloads,
            selectedBook = state.selectedBook,
            onSelect = actions.selectBook,
            onDownload = actions.enqueueDownload,
            onDelete = actions.deleteDownload,
            onOpen = actions.openReader,
            modifier = Modifier.weight(0.64f)
        )
    }
}

@Composable
private fun LibraryList(
    libraries: List<Library>,
    selectedLibraryId: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text("Libraries", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(libraries, key = { it.id }) { library ->
                FilterChip(
                    selected = library.id == selectedLibraryId,
                    onClick = { onSelect(library.id) },
                    label = {
                        Column {
                            Text(library.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(library.type, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BookList(
    books: List<Book>,
    downloads: Map<Int, DownloadRecord>,
    selectedBook: Book?,
    onSelect: (Int) -> Unit,
    onDownload: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onOpen: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text("Books", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
        if (books.isEmpty()) {
            EmptyState("Select a library to load series.")
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(books, key = { it.id }) { book ->
                    BookCard(
                        book = book,
                        download = downloads[book.id],
                        selected = selectedBook?.id == book.id,
                        onSelect = onSelect,
                        onDownload = onDownload,
                        onDelete = onDelete,
                        onOpen = onOpen
                    )
                }
            }
        }
    }
}

@Composable
private fun BookCard(
    book: Book,
    download: DownloadRecord?,
    selected: Boolean,
    onSelect: (Int) -> Unit,
    onDownload: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onOpen: (Int) -> Unit
) {
    val colors = if (selected) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    } else {
        CardDefaults.cardColors()
    }
    Card(onClick = { onSelect(book.id) }, colors = colors, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Book, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    Text("Series ID ${book.id}", style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                DownloadStatusChip(download)
                Spacer(Modifier.weight(1f))
                if (download?.status == DownloadStatus.Downloaded) {
                    IconButton(onClick = { onOpen(book.id) }) {
                        Icon(Icons.Default.Book, contentDescription = "Read")
                    }
                    IconButton(onClick = { onDelete(book.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                } else {
                    IconButton(onClick = { onDownload(book.id) }) {
                        Icon(Icons.Default.Download, contentDescription = "Download")
                    }
                }
            }
            if (download?.status == DownloadStatus.Running) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            download?.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DownloadStatusChip(download: DownloadRecord?) {
    val label = when (download?.status) {
        DownloadStatus.Queued -> "Queued"
        DownloadStatus.Running -> "Downloading"
        DownloadStatus.Downloaded -> "Offline"
        DownloadStatus.Failed -> "Failed"
        null -> "Not downloaded"
    }
    AssistChip(onClick = {}, label = { Text(label) })
}

@Composable
private fun ReaderScreen(
    book: Book,
    epubBook: EpubBook,
    settings: ReaderSettings,
    onProgress: (Int, Int, Float) -> Unit
) {
    var chapterIndex by remember(book.id) { mutableStateOf(0) }
    val chapters = epubBook.chapters
    val chapter = chapters.getOrNull(chapterIndex) ?: return
    val paragraphs = remember(chapter) { chapter.body.split(Regex("\n{2,}")).filter { it.isNotBlank() } }
    val listState = rememberLazyListState()
    val readerColors = settings.readerColors()

    LaunchedEffect(book.id, chapterIndex, paragraphs.size) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { firstVisible ->
                onProgress(book.id, chapterIndex, ProgressCalculator.chapterProgress(firstVisible, paragraphs.size))
            }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(readerColors.background)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(epubBook.title, color = readerColors.foreground, modifier = Modifier.weight(1f), maxLines = 1)
            Text("${chapterIndex + 1}/${chapters.size}", color = readerColors.foreground)
        }
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(settings.marginDp.dp),
            verticalArrangement = Arrangement.spacedBy((settings.fontSizeSp / 2).dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                Text(
                    chapter.title,
                    color = readerColors.foreground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = settings.fontFamilyValue()
                )
                Spacer(Modifier.height(16.dp))
            }
            items(paragraphs) { paragraph ->
                Text(
                    paragraph,
                    color = readerColors.foreground,
                    fontSize = settings.fontSizeSp.sp,
                    lineHeight = (settings.fontSizeSp * settings.lineHeight).sp,
                    fontFamily = settings.fontFamilyValue()
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { chapterIndex = (chapterIndex - 1).coerceAtLeast(0) }, enabled = chapterIndex > 0) {
                Text("Previous")
            }
            TextButton(onClick = { chapterIndex = (chapterIndex + 1).coerceAtMost(chapters.lastIndex) }, enabled = chapterIndex < chapters.lastIndex) {
                Text("Next")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReaderSettingsDialog(
    settings: ReaderSettings,
    onDismiss: () -> Unit,
    onUpdate: ((ReaderSettings) -> ReaderSettings) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        title = { Text("Reader Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SettingSlider("Font size", settings.fontSizeSp, 14f..28f) {
                    onUpdate { current -> current.copy(fontSizeSp = it.toInt()) }
                }
                SettingSlider("Line height", (settings.lineHeight * 100).toInt(), 120f..220f) {
                    onUpdate { current -> current.copy(lineHeight = it / 100f) }
                }
                SettingSlider("Margins", settings.marginDp, 8f..40f) {
                    onUpdate { current -> current.copy(marginDp = it.toInt()) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReaderTheme.entries.forEach { theme ->
                        FilterChip(
                            selected = settings.theme == theme,
                            onClick = { onUpdate { current -> current.copy(theme = theme) } },
                            label = { Text(theme.name) }
                        )
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("serif", "sans", "mono").forEach { family ->
                        FilterChip(
                            selected = settings.fontFamily == family,
                            onClick = { onUpdate { current -> current.copy(fontFamily = family) } },
                            label = { Text(family) }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun SettingSlider(label: String, value: Int, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column {
        Text("$label: $value", style = MaterialTheme.typography.labelMedium)
        Slider(value = value.toFloat(), onValueChange = onChange, valueRange = range)
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BusyOverlay(message: String) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Card(shape = RoundedCornerShape(8.dp)) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator()
                Spacer(Modifier.width(16.dp))
                Text(message)
            }
        }
    }
}

private data class ReaderColors(val background: Color, val foreground: Color)

private fun ReaderSettings.readerColors(): ReaderColors = when (theme) {
    ReaderTheme.Light -> ReaderColors(Color(0xFFFFFCF6), Color(0xFF171715))
    ReaderTheme.Dark -> ReaderColors(Color(0xFF171717), Color(0xFFEDEBE4))
    ReaderTheme.Sepia -> ReaderColors(Color(0xFFF2E4C8), Color(0xFF34281D))
    ReaderTheme.Custom -> ReaderColors(parseColor(customBackground), parseColor(customForeground))
}

private fun ReaderSettings.fontFamilyValue(): FontFamily = when (fontFamily) {
    "sans" -> FontFamily.SansSerif
    "mono" -> FontFamily.Monospace
    else -> FontFamily.Serif
}

private fun parseColor(value: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(value))
}.getOrElse { Color.White }
