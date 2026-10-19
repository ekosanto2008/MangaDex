package com.santoso.mangadex

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.santoso.mangadex.ui.navigation.Screen
import com.santoso.mangadex.ui.navigation.bottomNavItems
import com.santoso.mangadex.ui.screens.*
import com.santoso.mangadex.ui.theme.MangaDexTheme
import com.santoso.mangadex.ui.viewmodel.ThemeMode
import com.santoso.mangadex.ui.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalNavAnimatedContentScope = compositionLocalOf<AnimatedContentScope?> { null }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MangaDexTheme(darkTheme = darkTheme) {
                MainScreen(themeViewModel)
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val showBottomBar = currentDestination?.route != Screen.Splash.route 
            && currentDestination?.route != Screen.Reader.route
            && currentDestination?.route?.contains("details") == false // Hide bar during detail transition

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(it, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        SharedTransitionLayout {
            CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Splash.route,
                    modifier = Modifier.padding(if (showBottomBar) innerPadding else PaddingValues(0.dp))
                ) {
                    composable(Screen.Splash.route) {
                        SplashScreen(onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        })
                    }
                    composable(Screen.Home.route) {
                        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
                            HomeScreen(
                                onMangaClick = { mangaId ->
                                    navController.navigate(Screen.Details.createRoute(mangaId))
                                },
                                onSeeAllClick = { title, type ->
                                    val encodedTitle = Uri.encode(title)
                                    navController.navigate(Screen.MangaList.createRoute(encodedTitle, type))
                                }
                            )
                        }
                    }
                    composable(Screen.Explore.route) {
                        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
                            ExploreScreen(onMangaClick = { mangaId ->
                                navController.navigate(Screen.Details.createRoute(mangaId))
                            })
                        }
                    }
                    composable(Screen.Favorites.route) {
                        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
                            FavoritesScreen(onMangaClick = { mangaId ->
                                navController.navigate(Screen.Details.createRoute(mangaId))
                            })
                        }
                    }
                    composable(Screen.History.route) {
                        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
                            HistoryScreen(onMangaClick = { mangaId ->
                                navController.navigate(Screen.Details.createRoute(mangaId))
                            })
                        }
                    }
                    composable(Screen.Settings.route) { SettingsScreen(themeViewModel) }
                    
                    composable(
                        route = Screen.MangaList.route,
                        arguments = listOf(
                            navArgument("title") { defaultValue = "" },
                            navArgument("type") { defaultValue = "" }
                        )
                    ) { backStackEntry ->
                        val title = backStackEntry.arguments?.getString("title") ?: ""
                        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
                            MangaListScreen(
                                title = title,
                                onBackClick = { navController.popBackStack() },
                                onMangaClick = { mangaId ->
                                    navController.navigate(Screen.Details.createRoute(mangaId))
                                }
                            )
                        }
                    }
                    
                    composable(Screen.Details.route) { backStackEntry ->
                        val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
                            MangaDetailScreen(
                                mangaId = mangaId,
                                onBackClick = { navController.popBackStack() },
                                onChapterClick = { chapterId ->
                                    navController.navigate(Screen.Reader.createRoute(chapterId, mangaId))
                                }
                            )
                        }
                    }
                    
                    composable(Screen.Reader.route) { backStackEntry ->
                        val chapterId = backStackEntry.arguments?.getString("chapterId") ?: ""
                        val mangaId = backStackEntry.arguments?.getString("mangaId") ?: ""
                        ReaderScreen(
                            chapterId = chapterId,
                            mangaId = mangaId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Surface {
        Text(text = "Placeholder for $name")
    }
}
