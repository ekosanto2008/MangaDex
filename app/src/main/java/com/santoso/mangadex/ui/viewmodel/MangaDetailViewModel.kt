package com.santoso.mangadex.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santoso.mangadex.data.local.FavoriteEntity
import com.santoso.mangadex.data.repository.MangaRepository
import com.santoso.mangadex.domain.model.ChapterData
import com.santoso.mangadex.domain.model.MangaData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaDetailViewModel @Inject constructor(
    private val repository: MangaRepository
) : ViewModel() {

    private val _manga = MutableStateFlow<MangaData?>(null)
    val manga: StateFlow<MangaData?> = _manga

    private val _chapters = MutableStateFlow<List<ChapterData>>(emptyList())
    val chapters: StateFlow<List<ChapterData>> = _chapters

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite

    fun loadMangaDetails(mangaId: String) {
        viewModelScope.launch {
            try {
                _manga.value = repository.getMangaDetails(mangaId).data
                _chapters.value = repository.getMangaChapters(mangaId).data
                _isFavorite.value = repository.isFavorite(mangaId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun toggleFavorite() {
        val currentManga = _manga.value ?: return
        viewModelScope.launch {
            val title = currentManga.attributes.title["en"] 
                ?: currentManga.attributes.title["ja-ro"]
                ?: currentManga.attributes.title.values.firstOrNull() 
                ?: "Unknown"
            
            val coverRel = currentManga.relationships.find { it.type == "cover_art" }
            val fileName = coverRel?.attributes?.fileName
            val coverUrl = if (fileName != null) {
                "https://uploads.mangadex.org/covers/${currentManga.id}/$fileName.256.jpg"
            } else {
                ""
            }
            
            val favorite = FavoriteEntity(
                id = currentManga.id,
                title = title,
                coverUrl = coverUrl,
                author = null
            )

            if (_isFavorite.value) {
                repository.removeFavorite(favorite)
                _isFavorite.value = false
            } else {
                repository.addFavorite(favorite)
                _isFavorite.value = true
            }
        }
    }
}
