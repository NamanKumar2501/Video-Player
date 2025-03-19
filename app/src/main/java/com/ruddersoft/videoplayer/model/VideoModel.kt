package com.ruddersoft.videoplayer.model

import android.graphics.Bitmap

data class VideoModel(
    val title: String,
    val videoUrl: String,
    val thumbnail: Bitmap?,
    val duration: Long // Duration in milliseconds
)
