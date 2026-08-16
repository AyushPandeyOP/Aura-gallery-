@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.pandeyji.aura.gallery

import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.chrisbanes.haze.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==================== PANDEY JI GLOW ====================

@Composable
fun PandeyJiGlow() {
    val anim = rememberInfiniteTransition(label = "glow")
    val alpha by anim.animateFloat(
        0.3f, 1f,
        infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "alpha"
    )
    Text(
        "Developed By Pandey Ji 👑",
        color = Color(0xFFFFD700).copy(alpha = alpha),
        style = TextStyle(shadow = Shadow(Color(0xFFFFD700), blurRadius = 25f * alpha)),
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

// ==================== TOP BAR ====================

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
            .padding(top = 16.dp, start = 20.dp, end = 20.dp)
            .fillMaxWidth()
            .height(56.dp)
            .shadow(12.dp, RoundedCornerShape(28.dp))
            .hazeChild(
                hazeState,
                RoundedCornerShape(28.dp),
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
                    .padding(start = 8.dp),
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
                    modifier = Modifier.size(22.dp)
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
                modifier = Modifier.hazeChild(
                    hazeState,
                    RoundedCornerShape(20.dp),
                    HazeStyle(
                        tint = if (currentMode == mode) Color(0xFFFFD700).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
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

// ==================== BOTTOM DOCK ====================

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
            .padding(bottom = 20.dp, start = 24.dp, end = 24.dp)
            .fillMaxWidth()
            .height(68.dp)
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(34.dp),
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
            DockButton(Icons.Default.Home, "Photos", screen == "GALLERY") {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onScreenChange("GALLERY")
            }
            DockButton(Icons.Default.Search, "Explore", screen == "EXPLORE") {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onScreenChange("EXPLORE")
            }
            DockButton(Icons.Default.Edit, "Studio", screen == "STUDIO") {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onScreenChange("STUDIO")
            }
            DockButton(Icons.Default.Lock, "Vault", screen == "VAULT") {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onScreenChange("VAULT")
            }
        }
    }
}

@Composable
fun DockButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isActive) 1.2f else 1f, label = "scale")
    val tint by animateColorAsState(
        if (isActive) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f),
        label = "tint"
    )
    IconButton(onClick = onClick, modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
    }
}

// ==================== SETTINGS HUB ====================

@Composable
fun SettingsHub(blurVal: Float, onBlurChange: (Float) -> Unit, hazeState: HazeState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .haze(state = hazeState)
            .padding(24.dp)
            .padding(top = 90.dp, bottom = 90.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AURA SETTINGS", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(24.dp))
        Text("Liquid Glass Blur: ${blurVal.toInt()} dp", color = Color.Gray)
        Slider(
            value = blurVal,
            onValueChange = onBlurChange,
            valueRange = 10f..80f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFD700),
                activeTrackColor = Color(0xFFFFD700)
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White)
        ) {
            Text("Setup Secure Vault PIN", fontSize = 15.sp)
        }
        Spacer(Modifier.height(30.dp))
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
    when (viewMode) {
        ViewMode.ALL_MEDIA -> {
            if (mediaList.isEmpty() && hasPerm) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center), color = Color(0xFFFFD700))
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize().haze(state = hazeState),
                    contentPadding = PaddingValues(top = 80.dp, bottom = 100.dp)
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
                                model = ImageRequest.Builder(LocalContext.current).data(media.uri).crossfade(true).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (media.isVideo) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp).shadow(4.dp))
                            }
                        }
                    }
                }
            }
        }
        ViewMode.ALBUMS -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().haze(state = hazeState),
                contentPadding = PaddingValues(top = 80.dp, bottom = 100.dp, start = 8.dp, end = 8.dp)
            ) {
                items(albums.size) { index -> AlbumCard(albums[index]) }
            }
        }
        ViewMode.FAVORITES -> {
            val favorites = mediaList.filter { it.isFavorite }
            if (favorites.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().haze(state = hazeState), contentAlignment = Alignment.Center) {
                    Text("No favorites yet", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize().haze(state = hazeState),
                    contentPadding = PaddingValues(top = 80.dp, bottom = 100.dp)
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
                                model = ImageRequest.Builder(LocalContext.current).data(media.uri).crossfade(true).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Icon(Icons.Default.Favorite, null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp).shadow(4.dp))
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
        modifier = Modifier.fillMaxWidth().padding(6.dp).clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181818))
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(album.thumbnailUri).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(140.dp)
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(album.bucketName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("${album.count} items", color = Color.Gray, fontSize = 11.sp)
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
    var currentZoom by remember { mutableFloatStateOf(1f) }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = currentZoom <= 1.05f,
        key = { mediaList[it].uri }
    ) { page ->
        MediaViewer(
            media = mediaList[page],
            onBack = onBack,
            isFavorite = mediaList[page].isFavorite,
            onFavoriteToggle = { onFavoriteToggle(page, it) },
            onZoomChanged = { zoom ->
                if (pagerState.currentPage == page) {
                    currentZoom = zoom
                }
            }
        )
    }
}

// ==================== MEDIA VIEWER (PHASE 1 REFINED) ====================

@Composable
fun MediaViewer(
    media: Media,
    onBack: () -> Unit,
    isFavorite: Boolean = false,
    onFavoriteToggle: (Boolean) -> Unit = {},
    onZoomChanged: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var showOverlay by remember { mutableStateOf(true) }
    var isFavoritedLocal by remember { mutableStateOf(isFavorite) }
    var showInfoSheet by remember { mutableStateOf(false) }

    if (media.isVideo) {
        // ==================== VIDEO BRANCH (NO PARENT POINTER INTERCEPTION) ====================
        val exoPlayer = remember(media.uri) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(media.uri))
                prepare()
                playWhenReady = true
            }
        }

        val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner, exoPlayer) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) exoPlayer.play()
                else if (event == Lifecycle.Event.ON_PAUSE) exoPlayer.pause()
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                exoPlayer.release()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Minimal overlay for back button and info during video playback
            if (showOverlay) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 40.dp, start = 16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                MediaViewerOverlay(
                    onClose = onBack,
                    onFavorite = {
                        isFavoritedLocal = it
                        onFavoriteToggle(it)
                    },
                    onInfoClick = { showInfoSheet = true },
                    isFavorite = isFavoritedLocal,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    } else {
        // ==================== IMAGE BRANCH (FULL PINCH/PAN + SWIPE UP) ====================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val imageGestureModifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scope.launch {
                                if (scale.value > 1f) {
                                    scale.animateTo(1f, spring(dampingRatio = 0.8f))
                                    offset = Offset.Zero
                                    onZoomChanged(1f)
                                } else {
                                    scale.animateTo(3f, spring(dampingRatio = 0.8f))
                                    onZoomChanged(3f)
                                }
                            }
                        },
                        onTap = { showOverlay = !showOverlay }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scope.launch {
                            val nextScale = (scale.value * zoom).coerceIn(1f, 5f)
                            scale.snapTo(nextScale)
                            onZoomChanged(nextScale)
                            if (nextScale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        if (scale.value == 1f && dragAmount < -40f) {
                            change.consume()
                            showInfoSheet = true
                        }
                    }
                }

            AsyncImage(
                model = ImageRequest.Builder(context).data(media.uri).crossfade(true).build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = imageGestureModifier
            )

            if (showOverlay && scale.value == 1f) {
                MediaViewerOverlay(
                    onClose = onBack,
                    onFavorite = {
                        isFavoritedLocal = it
                        onFavoriteToggle(it)
                    },
                    onInfoClick = { showInfoSheet = true },
                    isFavorite = isFavoritedLocal,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    if (showInfoSheet) {
        MediaInfoBottomSheet(media = media, onDismiss = { showInfoSheet = false })
    }
}

// ==================== MEDIA VIEWER OVERLAY ====================

@Composable
fun MediaViewerOverlay(
    onClose: () -> Unit,
    onFavorite: (Boolean) -> Unit,
    onInfoClick: () -> Unit,
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val hazeState = remember { HazeState() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(12.dp)
            .hazeChild(
                state = hazeState,
                shape = RoundedCornerShape(20.dp),
                style = HazeStyle(tint = Color.White.copy(alpha = 0.15f), blurRadius = 30.dp, noiseFactor = 0.05f)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onFavorite(!isFavorite)
            }) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFFFD700) else Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onInfoClick()
            }) {
                Icon(Icons.Default.Info, contentDescription = "Details", tint = Color.White, modifier = Modifier.size(26.dp))
            }
            IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(26.dp))
            }
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClose()
            }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(26.dp))
            }
        }
    }
}

// ==================== MEDIA INFO BOTTOM SHEET (PHASE 1) ====================

@Composable
fun MediaInfoBottomSheet(
    media: Media,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dateString = remember(media.timestamp) {
        val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(if (media.timestamp > 1000000000000L) media.timestamp else media.timestamp * 1000L))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141414),
        scrimColor = Color.Black.copy(alpha = 0.65f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Media Details",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (media.isVideo) "VIDEO" else "IMAGE",
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(Modifier.height(16.dp))

            InfoRow(label = "File Name", value = media.displayName.ifBlank { "Unknown Name" })
            InfoRow(label = "Album", value = media.bucketName)
            InfoRow(label = "Date Modified", value = dateString)
            InfoRow(label = "Type", value = if (media.isVideo) "Video Playback" else "High-Res Image")

            Spacer(Modifier.height(20.dp))
            PandeyJiGlow()
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ==================== KSU STYLE FLOATING BUTTON ====================

@Composable
fun KSUStyleFloatingButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .padding(16.dp)
            .size(56.dp)
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFD700))
            .background(Color(0xFF1A1A1A), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Star, contentDescription = "Studio", tint = Color(0xFFFFD700), modifier = Modifier.size(26.dp))
    }
}

// ==================== DUMMY SCREEN ====================

@Composable
fun DummyScreen(screen: String, hazeState: HazeState) {
    Box(modifier = Modifier.fillMaxSize().haze(state = hazeState), contentAlignment = Alignment.Center) {
        Text("$screen Screen - Coming Soon", color = Color.White, fontSize = 20.sp)
    }
}
