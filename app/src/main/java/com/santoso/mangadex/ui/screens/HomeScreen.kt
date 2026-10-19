package com.santoso.mangadex.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.santoso.mangadex.ui.components.*
import com.santoso.mangadex.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMangaClick: (String) -> Unit,
    onSeeAllClick: (String, String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val trendingManga by viewModel.trendingManga.collectAsState()
    val latestManga by viewModel.latestManga.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "MangaDex",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(top = 0.dp) // Dikecilkan lagi
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (trendingManga.isEmpty() && latestManga.isEmpty()) {
                // Perfect Layout Alignment Shimmer
                TrendingBannerShimmer()
                
                repeat(2) { // 2 sections
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        // Section Title Shimmer
                        ShimmerEffect(
                            modifier = Modifier
                                .padding(16.dp)
                                .width(150.dp)
                                .height(24.dp),
                            shape = RoundedCornerShape(4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(3) {
                                Box(modifier = Modifier.width(150.dp)) {
                                    MangaCardShimmer()
                                }
                            }
                        }
                    }
                }
            }

            // Trending Banner
            if (trendingManga.isNotEmpty()) {
                TrendingBanner(
                    mangas = trendingManga.take(5), // Take top 5 for banner
                    onMangaClick = onMangaClick
                )
            }

            if (trendingManga.isNotEmpty()) {
                MangaSection(
                    title = "Trending Now",
                    mangas = trendingManga,
                    onSeeAllClick = { onSeeAllClick("Trending Now", "trending") },
                    onMangaClick = onMangaClick
                )
            }
            
            if (latestManga.isNotEmpty()) {
                MangaSection(
                    title = "Latest Releases",
                    mangas = latestManga,
                    onSeeAllClick = { onSeeAllClick("Latest Releases", "latest") },
                    onMangaClick = onMangaClick
                )
            }
            
            // Footer Credit
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Developed by Eko Santoso",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "Powered by MangaDex API",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}
