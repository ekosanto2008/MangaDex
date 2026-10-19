package com.santoso.mangadex.ui.viewmodel

import android.util.Log
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
class HomeViewModel @Inject constructor(
    private val repository: MangaRepository
) : ViewModel() {

    private val _trendingManga = MutableStateFlow<List<MangaData>>(emptyList())
    val trendingManga: StateFlow<List<MangaData>> = _trendingManga

    private val _latestManga = MutableStateFlow<List<MangaData>>(emptyList())
    val latestManga: StateFlow<List<MangaData>> = _latestManga

    init {
        Log.d("MangaDex", "HomeViewModel initialized")
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                Log.d("MangaDex", "Fetching trending manga...")
                val trending = repository.getTrendingManga()
                Log.d("MangaDex", "Trending fetched: ${trending.data.size} items")
                _trendingManga.value = trending.data
                
                Log.d("MangaDex", "Fetching latest manga...")
                val latest = repository.getLatestManga()
                Log.d("MangaDex", "Latest fetched: ${latest.data.size} items")
                _latestManga.value = latest.data
            } catch (e: Exception) {
                Log.e("MangaDex", "Error loading home data", e)
            }
        }
    }
}
