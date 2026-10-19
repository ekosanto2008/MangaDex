package com.santoso.mangadex.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.santoso.mangadex.data.local.SearchHistoryEntity
import com.santoso.mangadex.data.repository.MangaRepository
import com.santoso.mangadex.domain.model.MangaData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val results: List<MangaData>) : SearchUiState
    data class Error(val message: String) : SearchUiState
    object Empty : SearchUiState
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: MangaRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    val searchHistory: StateFlow<List<SearchHistoryEntity>> = repository.getSearchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trendingSearches = listOf("Solo Leveling", "One Piece", "Oshi no Ko", "Chainsaw Man", "Blue Lock")

    val searchUiState: StateFlow<SearchUiState> = _searchQuery
        .debounce(500)
        .filter { it.length >= 2 }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            flow {
                emit(SearchUiState.Loading)
                try {
                    val response = repository.searchManga(query)
                    if (response.data.isEmpty()) {
                        emit(SearchUiState.Empty)
                    } else {
                        emit(SearchUiState.Success(response.data))
                        // Automatically add to history on success
                        repository.addSearchQuery(query)
                    }
                } catch (e: Exception) {
                    emit(SearchUiState.Error(e.message ?: "Unknown error"))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState.Idle)

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onSearchActiveChange(isActive: Boolean) {
        _isSearchActive.value = isActive
    }

    fun onSearch(query: String) {
        _searchQuery.value = query
        _isSearchActive.value = false
        viewModelScope.launch {
            repository.addSearchQuery(query)
        }
    }

    fun deleteHistory(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }
}
