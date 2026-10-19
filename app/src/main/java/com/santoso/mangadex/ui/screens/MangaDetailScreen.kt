package com.santoso.mangadex.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.santoso.mangadex.LocalNavAnimatedContentScope
import com.santoso.mangadex.LocalSharedTransitionScope
import com.santoso.mangadex.domain.model.ChapterData
import com.santoso.mangadex.domain.model.MangaData
import com.santoso.mangadex.ui.components.MangaCardShimmer
import com.santoso.mangadex.ui.components.ShimmerEffect
import com.santoso.mangadex.ui.viewmodel.MangaDetailViewModel
import com.shashank.sony.fancytoastlib.FancyToast
import kotlin.math.min

private val HEADER_HEIGHT = 450.dp
private val TOOLBAR_HEIGHT = 56.dp

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MangaDetailScreen(
    mangaId: String,
    onBackClick: () -> Unit,
    onChapterClick: (String) -> Unit,
    viewModel: MangaDetailViewModel = hiltViewModel()
) {
    val manga by viewModel.manga.collectAsState()
    val chapters by viewModel.chapters.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val context = LocalContext.current
    
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No SharedTransitionScope found")
    val animatedContentScope = LocalNavAnimatedContentScope.current
        ?: throw IllegalStateException("No AnimatedContentScope found")

    LaunchedEffect(mangaId) {
        viewModel.loadMangaDetails(mangaId)
    }

    val lazyListState = rememberLazyListState()
    val density = LocalDensity.current
    val headerHeightPx = with(density) { HEADER_HEIGHT.toPx() }
    val toolbarHeightPx = with(density) { TOOLBAR_HEIGHT.toPx() }

    val animationProgress by animatedContentScope.transition.animateFloat(
        label = "containerTransform",
        transitionSpec = { tween(durationMillis = 600, easing = FastOutSlowInEasing) }
    ) { state ->
        if (state == EnterExitState.Visible) 1f else 0f
    }

    val collapseFraction by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex > 0) 1f
            else {
                (lazyListState.firstVisibleItemScrollOffset.toFloat() / (headerHeightPx - toolbarHeightPx))
                    .coerceIn(0f, 1f)
            }
        }
    }

    val content = @Composable {
        Box(modifier = Modifier.fillMaxSize()) {
            DetailContent(
                manga = manga,
                chapters = chapters,
                isFavorite = isFavorite,
                lazyListState = lazyListState,
                onChapterClick = onChapterClick,
                onToggleFavorite = { 
                    viewModel.toggleFavorite()
                    val title = manga?.attributes?.title?.get("en") ?: "Manga"
                    if (!isFavorite) {
                        FancyToast.makeText(context, "Added $title to Favorites", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show()
                    } else {
                        FancyToast.makeText(context, "Removed $title from Favorites", FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show()
                    }
                },
                animationProgress = animationProgress
            )

            if (sharedTransitionScope != null && animatedContentScope != null) {
                CollapsingHeader(
                    manga = manga,
                    collapseFraction = collapseFraction,
                    animationProgress = animationProgress,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(Color.Black.copy(alpha = lerp(0.2f, 0f, collapseFraction)))
            )

            AnimatedToolbar(
                manga = manga,
                collapseFraction = collapseFraction,
                animationProgress = animationProgress,
                onBackClick = onBackClick
            )
        }
    }

    if (sharedTransitionScope != null && animatedContentScope != null) {
        with(sharedTransitionScope) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .sharedBounds(
                        rememberSharedContentState(key = "container-$mangaId"),
                        animatedVisibilityScope = animatedContentScope,
                        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(0.dp))
                    )
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                content()
            }
        }
    } else {
        content()
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CollapsingHeader(
    manga: MangaData?,
    collapseFraction: Float,
    animationProgress: Float,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val title = manga?.attributes?.title?.get("en")
        ?: manga?.attributes?.title?.values?.firstOrNull() ?: ""
    val coverRel = manga?.relationships?.find { it.type == "cover_art" }
    val fileName = coverRel?.attributes?.fileName
    val coverUrl = if (fileName != null) {
        "https://uploads.mangadex.org/covers/${manga?.id}/$fileName.512.jpg"
    } else {
        "https://via.placeholder.com/512x720.png?text=No+Cover"
    }

    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(HEADER_HEIGHT)
                .graphicsLayer {
                    translationY = -collapseFraction * (HEADER_HEIGHT.toPx() - TOOLBAR_HEIGHT.toPx())
                    alpha = animationProgress 
                }
        ) {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElement(
                        rememberSharedContentState(key = "image-${manga?.id}"),
                        animatedVisibilityScope = animatedContentScope,
                        boundsTransform = { _, _ -> 
                            tween(durationMillis = 600, easing = FastOutSlowInEasing)
                        }
                    )
                    .graphicsLayer {
                        scaleX = lerp(1.1f, 1f, animationProgress)
                        scaleY = lerp(1.1f, 1f, animationProgress)
                        
                        val scrollScale = lerp(1f, 1.2f, collapseFraction)
                        scaleX *= scrollScale
                        scaleY *= scrollScale
                        alpha = lerp(1f, 0.2f, collapseFraction)
                    },
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface),
                            startY = 700f
                        )
                    )
                    .graphicsLayer { alpha = animationProgress }
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .graphicsLayer {
                        alpha = lerp(1f, 0f, (collapseFraction * 2f).coerceIn(0f, 1f))
                        translationY = lerp(0f, 50f, collapseFraction)
                    }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "title-${manga?.id}"),
                        animatedVisibilityScope = animatedContentScope,
                        boundsTransform = { _, _ -> 
                            tween(durationMillis = 600, easing = FastOutSlowInEasing)
                        }
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "status-${manga?.id}"),
                        animatedVisibilityScope = animatedContentScope,
                        boundsTransform = { _, _ -> 
                            tween(durationMillis = 600, easing = FastOutSlowInEasing)
                        }
                    )
                ) {
                    Text(
                        text = manga?.attributes?.status?.uppercase() ?: "LOADING",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedToolbar(
    manga: MangaData?,
    collapseFraction: Float,
    animationProgress: Float,
    onBackClick: () -> Unit
) {
    val title = manga?.attributes?.title?.get("en")
        ?: manga?.attributes?.title?.values?.firstOrNull() ?: ""

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(TOOLBAR_HEIGHT)
            .graphicsLayer {
                alpha = lerp(0f, 1f, (animationProgress - 0.8f).coerceIn(0f, 0.2f) * 5f)
            },
        color = MaterialTheme.colorScheme.surface.copy(
            alpha = (collapseFraction * 1f).coerceIn(0f, 1f)
        ),
        shadowElevation = lerp(0.dp, 4.dp, collapseFraction)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(8.dp)
                    .background(
                        color = Color.Black.copy(alpha = lerp(0.3f, 0f, collapseFraction)),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (collapseFraction > 0.8f) MaterialTheme.colorScheme.onSurface else Color.White
                )
            }

            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 56.dp)
                    .graphicsLayer {
                        alpha = ((collapseFraction - 0.7f) * 5f).coerceIn(0f, 1f)
                        translationY = lerp(20f, 0f, (collapseFraction - 0.7f) * 5f).coerceIn(0f, 20f)
                    }
            )
        }
    }
}

@Composable
fun DetailContent(
    manga: MangaData?,
    chapters: List<ChapterData>,
    isFavorite: Boolean,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    onChapterClick: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    animationProgress: Float
) {
    if (manga == null) {
        // Full Page Detail Shimmer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ShimmerEffect(modifier = Modifier.fillMaxWidth().height(HEADER_HEIGHT))
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerEffect(modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp))
                ShimmerEffect(modifier = Modifier.width(60.dp).height(48.dp), shape = RoundedCornerShape(12.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            repeat(3) {
                ShimmerEffect(modifier = Modifier.padding(16.dp).fillMaxWidth(if (it == 2) 0.6f else 1f).height(16.dp), shape = RoundedCornerShape(4.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            repeat(5) {
                ShimmerEffect(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth().height(70.dp), shape = RoundedCornerShape(12.dp))
            }
        }
    } else {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = lerp(0f, 1f, (animationProgress - 0.6f).coerceIn(0f, 0.4f) * 2.5f)
                },
            userScrollEnabled = animationProgress > 0.95f
        ) {
            item {
                Spacer(modifier = Modifier.height(HEADER_HEIGHT))
            }

            item {
                MangaActionRow(
                    isFavorite = isFavorite,
                    onToggleFavorite = onToggleFavorite
                )
            }

            item {
                Text(
                    text = manga.attributes.description?.get("en") ?: "No description",
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            item {
                Text(
                    text = "Chapters (${chapters.size})",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                )
            }

            if (chapters.isEmpty()) {
                items(5) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        ShimmerEffect(modifier = Modifier.fillMaxWidth().height(70.dp), shape = RoundedCornerShape(12.dp))
                    }
                }
            } else {
                items(chapters) { chapter ->
                    ChapterItem(chapter = chapter) {
                        onChapterClick(chapter.id)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun MangaActionRow(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { /* Start Reading */ },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Read Now", fontWeight = FontWeight.Bold)
        }
        
        OutlinedButton(
            onClick = onToggleFavorite,
            modifier = Modifier.width(60.dp),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ChapterItem(chapter: ChapterData, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Chapter ${chapter.attributes.chapter ?: "?"}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (!chapter.attributes.title.isNullOrBlank()) {
                    Text(
                        chapter.attributes.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            Text(
                "${chapter.attributes.pages} pages",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
