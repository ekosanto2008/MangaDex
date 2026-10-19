package com.santoso.mangadex.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {
    // Menggunakan sumber gambar yang lebih stabil (Pinterest CDN seringkali lebih lancar di ISP Indonesia)
    val randomImages = listOf(
        "https://i.pinimg.com/originals/81/15/3b/81153b3b44b82d04a621e263b383bc41.jpg", // Solo Leveling
        "https://i.pinimg.com/originals/a7/77/42/a77742b1d30a48368453d67b2cb759fc.jpg", // Frieren
        "https://i.pinimg.com/originals/80/15/13/801513ba7124bc4f7b7f9b99051e539d.jpg", // Oshi no Ko
        "https://i.pinimg.com/originals/d8/a3/56/d8a356069da8468c85e855df65fc6638.jpg"  // One Piece
    )
    val randomImage = remember { randomImages.random() }
    val logoAlpha = remember { Animatable(0f) }
    var isImageLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, animationSpec = tween(1500))
        delay(2500) // Beri waktu load lebih lama
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6650a4), // Purple Base
                        Color(0xFF121212)  // Dark Base
                    )
                )
            )
    ) {
        // Tampilkan gambar dengan fade-in jika sukses
        AsyncImage(
            model = randomImage,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onSuccess = { isImageLoaded = true },
            alpha = if (isImageLoaded) 0.6f else 0f
        )
        
        // Cinematic Overlay agar teks selalu terbaca
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .alpha(logoAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tampilkan indikator loading halus jika gambar belum muncul
            if (!isImageLoaded) {
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp).padding(bottom = 24.dp),
                    strokeWidth = 2.dp
                )
            }
            
            Text(
                text = "MangaDex",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-1.5).sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "by Eko Santoso",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Light
                )
            )
        }
    }
}
