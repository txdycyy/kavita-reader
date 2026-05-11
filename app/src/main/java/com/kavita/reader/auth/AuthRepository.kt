package com.kavita.reader.auth

import com.kavita.reader.data.KavitaClientFactory
import com.kavita.reader.data.KavitaDao
import com.kavita.reader.data.OpdsClient
import com.kavita.reader.data.ServerEntity
import com.kavita.reader.data.toDomain
import com.kavita.reader.domain.ServerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val dao: KavitaDao,
    private val credentialStore: CredentialStore,
    private val clientFactory: KavitaClientFactory,
    private val opdsClient: OpdsClient
) {
    val server: Flow<ServerConfig?> = dao.observeServer().map { it?.toDomain() }

    suspend fun connect(serverUrl: String, apiKey: String): Result<Unit> = runCatching {
        val normalizedUrl = UrlNormalizer.normalize(serverUrl)
        val sanitizedKey = apiKey.trim()
        require(sanitizedKey.isNotBlank()) { "Auth Key is required" }

        val restResult = runCatching {
            clientFactory.create(normalizedUrl).libraries(sanitizedKey)
            "Rest"
        }
        val authMode = restResult.getOrElse { restError ->
            runCatching {
                opdsClient.validate(normalizedUrl, sanitizedKey)
                "Opds"
            }.getOrElse { opdsError ->
                throw IllegalStateException(
                    buildString {
                        append("Connection failed. ")
                        append("REST: ${restError.toConnectionMessage()}. ")
                        append("OPDS: ${opdsError.toConnectionMessage()}.")
                    }
                )
            }
        }
        credentialStore.saveApiKey(sanitizedKey)
        dao.saveServer(ServerEntity(baseUrl = normalizedUrl, authMode = authMode))
    }

    suspend fun apiKey(): String = credentialStore.readApiKey()
        ?: error("Missing Kavita Auth Key")

    suspend fun serverConfig(): ServerConfig = dao.getServer()?.toDomain()
        ?: error("Missing Kavita server")

    suspend fun disconnect() {
        credentialStore.clear()
        dao.clearServers()
    }

    private fun Throwable.toConnectionMessage(): String = when (this) {
        is HttpException -> "HTTP ${code()} ${message()}".trim()
        else -> message ?: javaClass.simpleName
    }
}
