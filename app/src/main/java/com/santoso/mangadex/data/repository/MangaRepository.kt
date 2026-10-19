package com.santoso.mangadex.data.repository

import com.santoso.mangadex.data.local.*
import com.santoso.mangadex.data.remote.MangaDexApi
import com.santoso.mangadex.domain.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MangaRepository @Inject constructor(
    private val api: MangaDexApi,
    private val dao: MangaDao
) {
    suspend fun getTrendingManga() = api.getMangaList(followedOrder = "desc")
    suspend fun getLatestManga() = api.getMangaList(latestOrder = "desc")
    suspend fun getTopRatedManga() = api.getMangaList(ratingOrder = "desc")
    suspend fun getRecentlyAddedManga() = api.getMangaList(createdOrder = "desc")
    suspend fun searchManga(query: String) = api.getMangaList(title = query)

    suspend fun getMangaDetails(id: String) = api.getMangaById(id)
    suspend fun getMangaChapters(id: String) = api.getMangaFeed(id)
    suspend fun getChapterPages(chapterId: String) = api.getAtHomeServer(chapterId)

    // Local
    fun getFavorites(): Flow<List<FavoriteEntity>> = dao.getAllFavorites()
    suspend fun isFavorite(id: String) = dao.isFavorite(id)
    suspend fun addFavorite(favorite: FavoriteEntity) = dao.insertFavorite(favorite)
    suspend fun removeFavorite(favorite: FavoriteEntity) = dao.deleteFavorite(favorite)

    fun getHistory(): Flow<List<HistoryEntity>> = dao.getAllHistory()
    suspend fun addHistory(history: HistoryEntity) = dao.insertHistory(history)
    suspend fun clearHistory() = dao.clearHistory()

    // Search History
    fun getSearchHistory(): Flow<List<SearchHistoryEntity>> = dao.getSearchHistory()
    suspend fun addSearchQuery(query: String) = dao.insertSearchQuery(SearchHistoryEntity(query))
    suspend fun deleteSearchQuery(query: String) = dao.deleteSearchQuery(query)
    suspend fun clearSearchHistory() = dao.clearSearchHistory()
}
