package com.santoso.mangadex.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.santoso.mangadex.ui.components.MangaCard
import com.santoso.mangadex.ui.components.MangaCardShimmer
import com.santoso.mangadex.ui.viewmodel.ExploreViewModel
import com.santoso.mangadex.ui.viewmodel.SearchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onMangaClick: (String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchUiState by viewModel.searchUiState.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val trendingSearches = viewModel.trendingSearches

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Explore",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                },
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(top = 0.dp),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onQueryChange,
                onSearch = viewModel::onSearch,
                active = isSearchActive,
                onActiveChange = viewModel::onSearchActiveChange,
                placeholder = { Text("Search manga, author, artist...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (isSearchActive && searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isSearchActive) 0.dp else 16.dp)
                    .padding(top = 4.dp, bottom = 8.dp) // Jarak atas dikecilkan sesuai permintaan (sekitar 4-7dp)
            ) {
                // Suggestions / History / Trending
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (searchQuery.isEmpty()) {
                        // Recent Search History
                        if (searchHistory.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Recent Searches", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                    TextButton(onClick = viewModel::clearAllHistory) {
                                        Text("Clear All")
                                    }
                                }
                            }
                            items(searchHistory) { history ->
                                ListItem(
                                    headlineContent = { Text(history.query) },
                                    leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.deleteHistory(history.query) }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Delete")
                                        }
                                    },
                                    modifier = Modifier.clickable { viewModel.onSearch(history.query) }
                                )
                            }
                        }

                        // Trending Searches
                        item {
                            Text(
                                "Trending Searches",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(trendingSearches) { trending ->
                            ListItem(
                                headlineContent = { Text(trending) },
                                leadingContent = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                                modifier = Modifier.clickable { viewModel.onSearch(trending) }
                            )
                        }
                    }
                }
            }

            // Results Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = searchUiState) {
                    is SearchUiState.Idle -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                                Text("Find your next favorite manga", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                    is SearchUiState.Loading -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(12) { MangaCardShimmer() }
                        }
                    }
                    is SearchUiState.Success -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.results) { manga ->
                                MangaCard(manga = manga, showStatus = false) {
                                    onMangaClick(manga.id)
                                }
                            }
                        }
                    }
                    is SearchUiState.Empty -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No manga found for \"$searchQuery\"", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                    is SearchUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
