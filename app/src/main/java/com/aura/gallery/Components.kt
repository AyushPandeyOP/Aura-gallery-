@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.aura.gallery

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import dev.chrisbanes.haze.*
import kotlinx.coroutines.launch
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.abs
import kotlin.math.roundToInt

// ==================== ADVANCED LIQUID GLASS BLUR ====================

@Composable
fun AdvancedLiquidBlur(
    hazeState: HazeState,
    blurVal: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Dynamic blur based on scroll position
    val animatedBlur by animateFloatAsState(blurVal, animationSpec = spring(dampingRatio = 0.8f), label = "blur")
    
    Box(modifier = modifier.haze(state = hazeState)) {
        content()
    }
}

// ==================== PANDEY JI GLOW ====================

@Composable
fun PandeyJiGlow() {
    val anim = rememberInfiniteTransition(label = "glow")
    val alpha by anim.animateFloat(
        0.2f, 1f,
        infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "alpha"
    )
    Text(
        "Developed By Pandey Ji 👑",
        color = Color(0xFFFFD700).copy(alpha = alpha),
        style = TextStyle(shadow = Shadow(Color(0xFFFFD700), blurRadius = 30f * alpha)),
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

// ==================== ADVANCED TOP BAR ====================

@Composable
fun TopBar(
    screen: String,
    hazeState: HazeState,
    blurVal: Float,
    isScrolling: Boolean,
    modifier: Modifier,
    onProfileClick: () -> Unit,
    onSearchChange: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val offsetY by animateDpAsState(
        targetValue = if (isScrolling) (-100).dp else 0.dp,
        animationSpec = tween(300),
        label = "topBarSlide"
    )
    
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .padding(top = 40.dp, start = 24.dp, end = 24.dp)
            .fillMaxWidth()
            .height(55.dp)
            .shadow(10.dp, RoundedCornerShape(25.dp))
            .hazeChild(
                hazeState,
                RoundedCornerShape(25.dp),
                HazeStyle(
                    tint = Color.White.copy(alpha = 0.12f),
                    blurRadius = blurVal.dp,
                    noiseFactor = 0.05f
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            TextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearchChange(it)
                },
                placeholder = { Text("Smart Search...", color = Color.Gray, fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                singleLine = true
            )
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onProfileClick()
                }
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = if (screen == "SETTINGS") Color(0xFFFFD700) else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ==================== ALBUM FILTER CHIPS ====================

@Composable
fun AlbumFilterChips(
    currentMode: ViewMode,
    onModeChange: (ViewMode) -> Unit,
    hazeState: HazeState,
    blurVal: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            Pair(ViewMode.ALL_MEDIA, "All Media"),
            Pair(ViewMode.ALBUMS, "Albums"),
            Pair(ViewMode.FAVORITES, "Favorites")
        ).forEach { (mode, label) ->
            FilterChip(
                selected = currentMode == mode,
                onClick = { onModeChange(mode) },
                label = { Text(label, fontSize = 12.sp) },
                modifier = Modifier
                    .hazeChild(
                        hazeState,
                        RoundedCornerShape(20.dp),
                        HazeStyle(
                            tint = if (currentMode == mode)
                                Color(0xFFFFD700).copy(alpha = 0.2f)
                            else
                                Color.White.copy(alpha = 0.08f),
                            blurRadius = blurVal.dp,
                            noiseFactor = 0.03f
                        )
                    ),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                    selectedLabelColor = Color(0xFFFFD700),
                    labelColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }
}

// ==================== BOTTOM DOCK - PERFECTED ====================

@Composable
fun BottomDock(
    screen: String,
    hazeState: HazeState,
    blurVal: Float,
    isScrolling: Boolean,
    modifier: Modifier,
    onScreenChange: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val offsetY by animateDpAsState(
        targetValue = if (isScrolling) 100.dp else 0.dp,
        animationSpec = tween(300),
        label = "bottomDockSlide"
    )

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .padding(bottom = 24.dp, start = 24.dp, end = 24.dp)
            .fillMaxWidth()
            .height(70.dp)
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(35.dp),
                style = HazeStyle(
                    tint = Color.White.copy(alpha = 0.15f),
                    blurRadius = blurVal.dp,
                    noiseFactor = 0.05f
                )
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PHOTOS BUTTON
            DockButton(
                icon = Icons.Default.Image,
                label = "Photos",
                isActive = screen == "GALLERY",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onScreenChange("GALLERY")
                }
            )

            // EXPLORE BUTTON (Albums)
            DockButton(
                icon = Icons.Default.Explore,
                label = "Explore",
                isActive = screen == "EXPLORE",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onScreenChange("EXPLORE")
                }
            )

            // AI STUDIO BUTTON
            DockButton(
                icon = Icons.Default.AutoAwesome,
                label = "Studio",
                isActive = screen == "STUDIO",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onScreenChange("STUDIO")
                }
            )

            // VAULT BUTTON
            DockButton(
                icon = Icons.Default.Lock,
                label = "Vault",
                isActive = screen == "VAULT",
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onScreenChange("VAULT")
                }
            )
        }
    }
}

@Composable
fun DockButton(
    icon: androidx.compose.material.icons.materialIcon,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(if (isActive) 1.2f else 1f, label = "scale")
    val tint by animateColorAsState(
        if (isActive) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f),
        label = "tint"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ==================== SETTINGS HUB ====================

@Composable
fun SettingsHub(
    blurVal: Float,
    onBlurChange: (Float) -> Unit,
    hazeState: HazeState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .haze(state = hazeState)
            .padding(24.dp)
            .padding(top = 110.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "AURA SETTINGS",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(30.dp))
        Text("Liquid Glass Blur Level: ${blurVal.toInt()}", color = Color.Gray)
        Slider(
            value = blurVal,
            onValueChange = onBlurChange,
            valueRange = 10f..80f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFD700),
                activeTrackColor = Color(0xFFFFD700)
            )
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF151515),
                contentColor = Color.White
            )
        ) {
            Text("Setup Secure Vault PIN", color = Color.White)
        }
        Spacer(Modifier.weight(1f))
        PandeyJiGlow()
    }
}

// ==================== GALLERY GRID ====================

@Composable
fun GalleryGrid(
    mediaList: List<Media>,
    hasPerm: Boolean,
    hazeState: HazeState,
    gridState: LazyGridState,
    viewMode: ViewMode,
    albums: List<Album>,
    onMediaClick: (Int) -> Unit
) {
    val navBarPad = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    when (viewMode) {
        ViewMode.ALL_MEDIA -> {
            if (mediaList.isEmpty() && hasPerm) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                    color = Color.White
                )
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(state = hazeState),
                    contentPadding = PaddingValues(top = 110.dp, bottom = 130.dp + navBarPad)
                ) {
                    itemsIndexed(mediaList) { index, media ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .clickable { onMediaClick(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(media.uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (media.isVideo) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp).shadow(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        ViewMode.ALBUMS -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
                contentPadding = PaddingValues(top = 110.dp, bottom = 130.dp + navBarPad, start = 8.dp, end = 8.dp)
            ) {
                items(albums.size) { index ->
                    AlbumCard(albums[index])
                }
            }
        }

        ViewMode.FAVORITES -> {
            val favorites = mediaList.filter { it.isFavorite }
            if (favorites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(state = hazeState),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No favorites yet", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(state = hazeState),
                    contentPadding = PaddingValues(top = 110.dp, bottom = 130.dp + navBarPad)
                ) {
                    itemsIndexed(favorites) { index, media ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(1.dp)
                                .clickable { onMediaClick(mediaList.indexOf(media)) },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(media.uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Icon(
                                Icons.Default.Favorite,
                                null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp).shadow(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== ALBUM CARD ====================

@Composable
fun AlbumCard(album: Album) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1a1a1a)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1a1a1a))
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.thumbnailUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    album.bucketName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${album.count} photos",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ==================== MEDIA PAGER SCREEN ====================

@Composable
fun MediaPagerScreen(
    mediaList: List<Media>,
    initialIndex: Int,
    onBack: () -> Unit,
    onFavoriteToggle: (Int, Boolean) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex) { mediaList.size }
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        MediaViewer(
            mediaList[page],
            onBack,
            isFavorite = mediaList[page].isFavorite,
            onFavoriteToggle = { onFavoriteToggle(page, it) }
        )
    }
}

// ==================== MEDIA VIEWER WITH OVERLAY ====================

@Composable
fun MediaViewer(
    media: Media,
    onBack: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteToggle: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val bgAlpha by animateFloatAsState(1f - (abs(dragOffsetY) / 800f).coerceIn(0f, 1f), label = "a")
    var showOverlay by remember { mutableStateOf(true) }
    var isFavoritedLocal by remember { mutableStateOf(isFavorite) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = bgAlpha))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = 3f
                            offset = Offset.Zero
                        }
                    },
                    onTap = {
                        showOverlay = !showOverlay
                    }
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    val maxX = (size.width * (scale - 1)) / 2f
                    val maxY = (size.height * (scale - 1)) / 2f
                    if (scale > 1f) {
                        offset = Offset(
                            (offset.x + pan.x).coerceIn(-maxX, maxX),
                            (offset.y + pan.y).coerceIn(-maxY, maxY)
                        )
                    } else {
                        offset = Offset.Zero
                    }
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (scale == 1f) {
                            if (dragOffsetY > 250f || dragOffsetY < -250f) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBack()
                            } else {
                                dragOffsetY = 0f
                            }
                        }
                    },
                    onVerticalDrag = { change, amount ->
                        if (scale == 1f) {
                            change.consume()
                            dragOffsetY += amount
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val mod = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)

        if (media.isVideo) {
            val exoPlayer = remember {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(media.uri))
                    prepare()
                    playWhenReady = true
                }
            }

            val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_PAUSE) exoPlayer.pause()
                    else if (event == Lifecycle.Event.ON_RESUME) exoPlayer.play()
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    exoPlayer.release()
                }
            }

            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                    }
                },
                modifier = mod
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(context).data(media.uri).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = mod
            )
        }

        // AUTO-HIDE OVERLAY ON ZOOM
        if (showOverlay && scale == 1f) {
            MediaViewerOverlay(
                onClose = onBack,
                onFavorite = {
                    isFavoritedLocal = it
                    onFavoriteToggle(it)
                },
                isFavorite = isFavoritedLocal,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ==================== MEDIA VIEWER OVERLAY ====================

@Composable
fun MediaViewerOverlay(
    onClose: () -> Unit,
    onFavorite: (Boolean) -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(16.dp)
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(20.dp),
                style = HazeStyle(
                    tint = Color.White.copy(alpha = 0.15f),
                    blurRadius = 30.dp,
                    noiseFactor = 0.05f
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FAVORITE BUTTON
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFavorite(!isFavorite)
                }
            ) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFFFD700) else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // DELETE BUTTON
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // SHARE BUTTON
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // CLOSE BUTTON
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClose()
                }
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ==================== DUMMY SCREEN ====================

@Composable
fun DummyScreen(screen: String, hazeState: HazeState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .haze(state = hazeState),
        contentAlignment = Alignment.Center
    ) {
        Text("$screen Screen - Coming Soon", color = Color.White, fontSize = 24.sp)
    }
}