package com.santoso.mangadex.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import coil.compose.AsyncImage
import com.santoso.mangadex.LocalNavAnimatedContentScope
import com.santoso.mangadex.LocalSharedTransitionScope
import com.santoso.mangadex.domain.model.MangaData
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun MangaCardShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        // Match Box/AsyncImage bounds
        ShimmerEffect(
            modifier = Modifier
                .aspectRatio(0.7f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Match Title Text bounds
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(16.dp),
            shape = RoundedCornerShape(4.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Match Status/Category Chip bounds
        ShimmerEffect(
            modifier = Modifier
                .width(60.dp)
                .height(14.dp),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

@Composable
fun TrendingBannerShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .height(240.dp)
                .fillMaxWidth()
        ) {
            ShimmerEffect(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp)
            )
        }
        
        // Indicators shimmer
        Row(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(5) {
                ShimmerEffect(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp),
                    shape = CircleShape
                )
            }
        }
    }
}

@Composable
fun TrendingBanner(
    mangas: List<MangaData>,
    onMangaClick: (String) -> Unit
) {
    if (mangas.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { mangas.size })

    // Auto-scroll logic
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if (pagerState.pageCount > 0) {
                val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val manga = mangas[page]
            val title = manga.attributes.title["en"]
                ?: manga.attributes.title["ja-ro"]
                ?: manga.attributes.title.values.firstOrNull()
                ?: "Unknown Title"
            
            val coverRel = manga.relationships.find { it.type == "cover_art" }
            val fileName = coverRel?.attributes?.fileName
            val coverUrl = if (fileName != null) {
                "https://uploads.mangadex.org/covers/${manga.id}/$fileName.512.jpg"
            } else {
                "https://via.placeholder.com/512x720.png?text=No+Cover"
            }

            Card(
                modifier = Modifier
                    .graphicsLayer {
                        val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        ).also { scale ->
                            scaleX = scale
                            scaleY = scale
                        }
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .fillMaxWidth()
                    .height(240.dp)
                    .clickable { onMangaClick(manga.id) },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                                translationX = pageOffset * size.width * 0.2f
                            },
                        contentScale = ContentScale.Crop
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                    startY = 300f
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "TRENDING",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-0.5).sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
        Row(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(mangas.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                val width = if (isSelected) 24.dp else 8.dp
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .width(width)
                        .height(8.dp)
                        .animateContentSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MangaCard(
    manga: MangaData,
    showStatus: Boolean = true,
    onClick: () -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedContentScope = LocalNavAnimatedContentScope.current

    val title = manga.attributes.title["en"]
        ?: manga.attributes.title["ja-ro"]
        ?: manga.attributes.title.values.firstOrNull()
        ?: "Unknown Title"

    val coverRel = manga.relationships.find { it.type == "cover_art" }
    val fileName = coverRel?.attributes?.fileName
    val coverUrl = if (!fileName.isNullOrBlank()) {
        "https://uploads.mangadex.org/covers/${manga.id}/$fileName.256.jpg"
    } else {
        "https://via.placeholder.com/256x360.png?text=No+Cover"
    }

    val cardShape = RoundedCornerShape(12.dp)

    if (sharedTransitionScope != null && animatedContentScope != null) {
        with(sharedTransitionScope) {
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .sharedBounds(
                        rememberSharedContentState(key = "container-${manga.id}"),
                        animatedVisibilityScope = animatedContentScope,
                        boundsTransform = { _, _ -> 
                            tween(durationMillis = 600, easing = FastOutSlowInEasing)
                        },
                        clipInOverlayDuringTransition = OverlayClip(cardShape)
                    )
                    .clickable { onClick() },
                shape = cardShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                MangaCardContent(manga, title, coverUrl, showStatus, cardShape, sharedTransitionScope, animatedContentScope)
            }
        }
    } else {
        Card(
            modifier = Modifier
                .padding(4.dp)
                .clickable { onClick() },
            shape = cardShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            MangaCardContent(manga, title, coverUrl, showStatus, cardShape, null, null)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MangaCardContent(
    manga: MangaData,
    title: String,
    coverUrl: String,
    showStatus: Boolean,
    cardShape: androidx.compose.ui.graphics.Shape,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .aspectRatio(0.7f)
                .fillMaxWidth()
                .clip(cardShape)
        ) {
            val imageModifier = Modifier.fillMaxSize()
            if (sharedTransitionScope != null && animatedContentScope != null) {
                with(sharedTransitionScope) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = title,
                        modifier = imageModifier.sharedElement(
                            rememberSharedContentState(key = "image-${manga.id}"),
                            animatedVisibilityScope = animatedContentScope
                        ),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = title,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                            startY = 300f
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val titleModifier = Modifier.padding(horizontal = 12.dp)
        if (sharedTransitionScope != null && animatedContentScope != null) {
            with(sharedTransitionScope) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = titleModifier.sharedElement(
                        rememberSharedContentState(key = "title-${manga.id}"),
                        animatedVisibilityScope = animatedContentScope
                    )
                )
            }
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = titleModifier
            )
        }

        if (showStatus) {
            val status = manga.attributes.status ?: "Unknown"
            val statusModifier = Modifier.padding(12.dp)
            if (sharedTransitionScope != null && animatedContentScope != null) {
                with(sharedTransitionScope) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = statusModifier.sharedElement(
                            rememberSharedContentState(key = "status-${manga.id}"),
                            animatedVisibilityScope = animatedContentScope
                        )
                    ) {
                        Text(
                            text = status.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = statusModifier
                ) {
                    Text(
                        text = status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun MangaSection(
    title: String,
    mangas: List<MangaData>,
    onSeeAllClick: () -> Unit,
    onMangaClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onSeeAllClick) {
                Text("See All", style = MaterialTheme.typography.labelLarge)
            }
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(mangas) { manga ->
                Box(modifier = Modifier.width(150.dp)) {
                    MangaCard(manga = manga) {
                        onMangaClick(manga.id)
                    }
                }
            }
        }
    }
}
