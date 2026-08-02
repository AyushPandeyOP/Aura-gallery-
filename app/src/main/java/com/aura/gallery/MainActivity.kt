package com.pandeyJi.aura.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.window.DialogProperties
import dev.chrisbanes.haze.HazeState

class MainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        updatePermissionStatus(isGranted)
    }

    private var onPermissionResult: ((Boolean) -> Unit)? = null

    private fun requestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        permissionLauncher.launch(permission)
    }

    private fun updatePermissionStatus(isGranted: Boolean) {
        onPermissionResult?.invoke(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        setContent {
            AuraGalleryApp(
                hasInitialPermission = hasPermission,
                onRequestPermission = { callback ->
                    onPermissionResult = callback
                    requestPermission()
                }
            )
        }
    }
}

@Composable
fun AuraGalleryApp(
    hasInitialPermission: Boolean,
    onRequestPermission: ((Boolean) -> Unit) -> Unit
) {
    var hasPerm by remember { mutableStateOf(hasInitialPermission) }
    var mediaList by remember { mutableStateOf<List<Media>>(emptyList()) }
    var albumList by remember { mutableStateOf<List<Album>>(emptyList()) }
    var screen by remember { mutableStateOf("GALLERY") }
    var selIndex by remember { mutableStateOf(0) }
    var isFullScreen by remember { mutableStateOf(false) }
    var blurVal by remember { mutableFloatStateOf(30f) }
    var gridState by remember { mutableStateOf(LazyGridState()) }
    var viewMode by remember { mutableStateOf(ViewMode.ALL_MEDIA) }
    var searchQuery by remember { mutableStateOf("") }
    val hazeState = remember { HazeState() }
    val context = LocalContext.current

    LaunchedEffect(hasPerm) {
        if (hasPerm) {
            loadMediaAndAlbums(context) { media, albums ->
                mediaList = media
                albumList = albums
            }
        }
    }

    if (!hasPerm) {
        PermissionDialog(
            onGrantPermission = {
                onRequestPermission { granted ->
                    hasPerm = granted
                }
            }
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF050505))
        ) {
            if (isFullScreen && mediaList.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MediaPagerScreen(
                        mediaList,
                        selIndex,
                        onBack = { isFullScreen = false },
                        onFavoriteToggle = { index, isFav ->
                            mediaList = mediaList.toMutableList().apply {
                                this[index] = this[index].copy(isFavorite = isFav)
                            }
                        }
                    )
                }
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFF050505)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (screen == "GALLERY") {
                            AlbumFilterChips(
                                currentMode = viewMode,
                                onModeChange = { viewMode = it },
                                hazeState = hazeState,
                                blurVal = blurVal,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 100.dp)
                            )
                        }

                        when (screen) {
                            "GALLERY" -> {
                                gridState = rememberLazyGridState()
                                GalleryGrid(
                                    mediaList = mediaList,
                                    hasPerm = hasPerm,
                                    hazeState = hazeState,
                                    gridState = gridState,
                                    viewMode = viewMode,
                                    albums = albumList,
                                    onMediaClick = { idx ->
                                        selIndex = idx
                                        isFullScreen = true
                                    }
                                )
                            }
                            "EXPLORE" -> {
                                DummyScreen("EXPLORE", hazeState)
                            }
                            "STUDIO" -> {
                                DummyScreen("AI STUDIO", hazeState)
                            }
                            "VAULT" -> {
                                DummyScreen("VAULT", hazeState)
                            }
                            "SETTINGS" -> {
                                SettingsHub(blurVal, { blurVal = it }, hazeState)
                            }
                        }

                        if (!isFullScreen) {
                            TopBar(
                                screen = screen,
                                hazeState = hazeState,
                                blurVal = blurVal,
                                isScrolling = gridState.isScrollInProgress,
                                modifier = Modifier.align(Alignment.TopCenter),
                                onProfileClick = {
                                    screen = if (screen == "SETTINGS") "GALLERY" else "SETTINGS"
                                },
                                onSearchChange = { query ->
                                    searchQuery = query
                                }
                            )
                        }

                        if (!isFullScreen) {
                            BottomDock(
                                screen = screen,
                                hazeState = hazeState,
                                blurVal = blurVal,
                                isScrolling = gridState.isScrollInProgress,
                                modifier = Modifier.align(Alignment.BottomCenter),
                                onScreenChange = { newScreen ->
                                    screen = newScreen
                                    viewMode = ViewMode.ALL_MEDIA
                                }
                            )
                        }

                        // KSU STYLE FLOATING BUTTON (Bottom Right)
                        if (screen == "GALLERY" && !isFullScreen) {
                            KSUStyleFloatingButton(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 24.dp, bottom = 110.dp),
                                onClick = { }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun loadMediaAndAlbums(
    context: android.content.Context,
    onLoaded: (List<Media>, List<Album>) -> Unit
) {
    val media = mutableListOf<Media>()
    val albums = mutableMapOf<Long, Album>()
    
    val cursor = context.contentResolver.query(
        MediaStore.Files.getContentUri("external"),
        arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.BUCKET_ID,
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Files.FileColumns.DATE_MODIFIED,
            MediaStore.Files.FileColumns.DISPLAY_NAME
        ),
        "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?",
        arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        ),
        "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
    )

    cursor?.use {
        val idCol = it.getColumnIndex(MediaStore.Files.FileColumns._ID)
        val dataCol = it.getColumnIndex(MediaStore.Files.FileColumns.DATA)
        val typeCol = it.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
        val bucketIdCol = it.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_ID)
        val bucketNameCol = it.getColumnIndex(MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME)
        val dateCol = it.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)
        val nameCol = it.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)

        while (it.moveToNext()) {
            val id = it.getLong(idCol)
            val data = it.getString(dataCol)
            val type = it.getInt(typeCol)
            val bucketId = it.getLong(bucketIdCol)
            val bucketName = it.getString(bucketNameCol) ?: "Camera"
            val timestamp = it.getLong(dateCol)
            val displayName = it.getString(nameCol)

            val uri = if (type == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build().toString()
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build().toString()
            }

            media.add(
                Media(
                    uri = uri,
                    isVideo = type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    bucketName = bucketName,
                    bucketId = bucketId,
                    timestamp = timestamp,
                    displayName = displayName
                )
            )

            if (!albums.containsKey(bucketId)) {
                albums[bucketId] = Album(
                    bucketId = bucketId,
                    bucketName = bucketName,
                    thumbnailUri = uri,
                    count = 1,
                    lastModified = timestamp
                )
            } else {
                albums[bucketId] = albums[bucketId]!!.copy(count = albums[bucketId]!!.count + 1)
            }
        }
    }

    onLoaded(media, albums.values.toList())
}

@Composable
fun PermissionDialog(onGrantPermission: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Permission Required", color = Color.White) },
        text = { Text("Aura Gallery needs access to your photos and videos", color = Color.White) },
        confirmButton = {
            Button(
                onClick = onGrantPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) {
                Text("Grant Permission", color = Color.Black)
            }
        },
        containerColor = Color(0xFF1a1a1a),
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}
