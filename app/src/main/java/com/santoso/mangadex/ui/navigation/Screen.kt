package com.santoso.mangadex.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Explore : Screen("explore", "Explore", Icons.Default.Search)
    object Favorites : Screen("favorites", "Favorites", Icons.Default.Favorite)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    
    object Details : Screen("details/{mangaId}", "Details") {
        fun createRoute(mangaId: String) = "details/$mangaId"
    }
    
    object MangaList : Screen("mangaList?title={title}&type={type}", "Manga List") {
        fun createRoute(title: String, type: String) = "mangaList?title=$title&type=$type"
    }
    
    object Reader : Screen("reader/{chapterId}/{mangaId}", "Reader") {
        fun createRoute(chapterId: String, mangaId: String) = "reader/$chapterId/$mangaId"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Explore,
    Screen.Favorites,
    Screen.History,
    Screen.Settings
)
