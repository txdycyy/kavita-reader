package com.kavita.reader.data

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

interface KavitaApi {
    @GET("api/Library/libraries")
    suspend fun libraries(@Header("x-api-key") apiKey: String): List<KavitaLibraryDto>

    @POST("api/Series/all-v2")
    suspend fun series(
        @Header("x-api-key") apiKey: String,
        @Query("PageNumber") pageNumber: Int = 1,
        @Query("PageSize") pageSize: Int = 500,
        @Body request: SeriesRequestDto
    ): List<SeriesDto>

    @Streaming
    @GET("api/Download/series")
    suspend fun downloadSeries(
        @Header("x-api-key") apiKey: String,
        @Query("seriesId") seriesId: Int
    ): ResponseBody
}

data class KavitaLibraryDto(
    val id: Int = 0,
    val name: String = "",
    val type: Int? = null
)

data class SeriesRequestDto(
    val id: Int,
    val name: String? = null,
    val statements: List<SeriesFilterStatementDto> = emptyList(),
    val combination: Int = 0,
    val sortOptions: SeriesSortOptionDto? = null,
    val entityType: Int = 0,
    val limitTo: Int = 0
)

data class SeriesFilterStatementDto(
    val comparison: Int,
    val value: String,
    val field: Int
)

data class SeriesSortOptionDto(
    val sortField: Int,
    val isAscending: Boolean
)

data class SeriesDto(
    val id: Int = 0,
    val name: String = "",
    val originalName: String? = null,
    val sortName: String? = null,
    val localizedName: String? = null,
    val coverImage: String? = null
)
