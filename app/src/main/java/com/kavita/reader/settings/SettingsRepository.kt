package com.kavita.reader.settings

import com.kavita.reader.data.KavitaDao
import com.kavita.reader.data.toDomain
import com.kavita.reader.data.toEntity
import com.kavita.reader.domain.ReaderSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dao: KavitaDao
) {
    val readerSettings: Flow<ReaderSettings> = dao.observeReaderSettings()
        .map { it?.toDomain() ?: ReaderSettings() }

    suspend fun saveReaderSettings(settings: ReaderSettings) {
        dao.saveReaderSettings(settings.toEntity())
    }

    suspend fun getReaderSettings(): ReaderSettings =
        dao.getReaderSettings()?.toDomain() ?: ReaderSettings()
}
