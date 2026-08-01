package com.aura.gallery

import android.net.Uri

data class Media(
    val uri: String,
    val isVideo: Boolean,
    val bucketName: String = "Camera",
    val bucketId: Long = 0L,
    val isFavorite: Boolean = false,
    val timestamp: Long = 0L,
    val displayName: String = ""
)

data class Album(
    val bucketId: Long,
    val bucketName: String,
    val thumbnailUri: String,
    val count: Int,
    val lastModified: Long
)

enum class ViewMode {
    ALL_MEDIA,
    ALBUMS,
    FAVORITES
}