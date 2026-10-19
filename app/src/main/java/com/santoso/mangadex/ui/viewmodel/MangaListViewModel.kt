package com.santoso.mangadex.ui.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santoso.mangadex.data.repository.MangaRepository
import com.santoso.mangadex.domain.model.MangaData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaListViewModel @Inject constructor(
    private val repository: MangaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _mangaList = MutableStateFlow<List<MangaData>>(emptyList())
    val mangaList: StateFlow<List<MangaData>> = _mangaList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        val type: String? = savedStateHandle["type"]
        Log.d("MangaDex", "MangaListViewModel INIT: type received = $type")
        if (type != null) {
            loadMangaByType(type)
        }
    }

    private fun loadMangaByType(type: String) {
        viewModelScope.launch {
            Log.d("MangaDex", "MangaListViewModel: Fetching data for type: $type")
            _isLoading.value = true
            try {
                val response = when (type) {
                    "trending" -> repository.getTrendingManga()
                    "latest" -> repository.getLatestManga()
                    else -> repository.getTrendingManga()
                }
                Log.d("MangaDex", "MangaListViewModel: Success! Items count: ${response.data.size}")
                _mangaList.value = response.data
            } catch (e: Exception) {
                Log.e("MangaDex", "MangaListViewModel: API Error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
