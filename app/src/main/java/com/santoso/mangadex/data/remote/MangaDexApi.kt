package com.santoso.mangadex.data.remote

import com.santoso.mangadex.domain.model.*
import retrofit2.http.*

interface MangaDexApi {
    @GET("manga")
    suspend fun getMangaList(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("title") title: String? = null,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author", "artist"),
        @Query("contentRating[]") contentRating: List<String> = listOf("safe", "suggestive"),
        @Query("order[latestUploadedChapter]") latestOrder: String? = null,
        @Query("order[rating]") ratingOrder: String? = null,
        @Query("order[followedCount]") followedOrder: String? = null,
        @Query("order[createdAt]") createdOrder: String? = null
    ): MangaListResponse

    @GET("manga/{id}")
    suspend fun getMangaById(
        @Path("id") id: String,
        @Query("includes[]") includes: List<String> = listOf("cover_art", "author", "artist")
    ): MangaResponse

    @GET("manga/{id}/feed")
    suspend fun getMangaFeed(
        @Path("id") id: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("translatedLanguage[]") languages: List<String> = listOf("en"),
        @Query("order[chapter]") chapterOrder: String = "desc"
    ): ChapterListResponse

    @GET("at-home/server/{chapterId}")
    suspend fun getAtHomeServer(
        @Path("chapterId") chapterId: String
    ): AtHomeResponse

    companion object {
        const val BASE_URL = "https://api.mangadex.org/"
    }
}
