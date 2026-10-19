package com.santoso.mangadex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santoso.mangadex.data.local.HistoryEntity
import com.santoso.mangadex.data.repository.MangaRepository
import com.santoso.mangadex.domain.model.AtHomeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repository: MangaRepository
) : ViewModel() {

    private val _pages = MutableStateFlow<List<String>>(emptyList())
    val pages: StateFlow<List<String>> = _pages

    fun loadChapter(chapterId: String, mangaId: String) {
        viewModelScope.launch {
            try {
                // Get chapter info first to get volume/chapter number
                val chaptersResponse = repository.getMangaChapters(mangaId)
                val currentChapter = chaptersResponse.data.find { it.id == chapterId }
                val chapterTitle = if (currentChapter != null) {
                    "Chapter ${currentChapter.attributes.chapter ?: "?"}${if (currentChapter.attributes.title != null) ": ${currentChapter.attributes.title}" else ""}"
                } else {
                    "Chapter"
                }

                val response = repository.getChapterPages(chapterId)
                val baseUrl = response.baseUrl
                val hash = response.chapter.hash
                val pages = response.chapter.data.map { "${baseUrl}/data/${hash}/${it}" }
                _pages.value = pages

                // Save to history with detailed info
                saveToHistory(chapterId, mangaId, chapterTitle)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun saveToHistory(chapterId: String, mangaId: String, chapterTitle: String) {
        try {
            val mangaResponse = repository.getMangaDetails(mangaId)
            val manga = mangaResponse.data
            val title = manga.attributes.title["en"] 
                ?: manga.attributes.title["ja-ro"] 
                ?: manga.attributes.title.values.firstOrNull() 
                ?: "Unknown"
            
            val coverRel = manga.relationships.find { it.type == "cover_art" }
            val fileName = coverRel?.attributes?.fileName
            val coverUrl = if (fileName != null) {
                "https://uploads.mangadex.org/covers/${mangaId}/$fileName.256.jpg"
            } else {
                ""
            }
            
            repository.addHistory(
                HistoryEntity(
                    id = mangaId, // Use mangaId as PK so each manga only has ONE latest entry in history
                    mangaId = mangaId,
                    mangaTitle = title,
                    chapterTitle = chapterTitle,
                    chapterId = chapterId,
                    coverUrl = coverUrl,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
