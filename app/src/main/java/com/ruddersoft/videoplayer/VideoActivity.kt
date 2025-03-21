package com.ruddersoft.videoplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ruddersoft.videoplayer.Adapter.VideoAdapter
import com.ruddersoft.videoplayer.model.VideoModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private val videoList = mutableListOf<VideoModel>()
    private val REQUEST_PERMISSION = 1

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        recyclerView = findViewById(R.id.rv_video_player)

        recyclerView.layoutManager = LinearLayoutManager(this)
        videoAdapter = VideoAdapter { video ->
            val intent = Intent(this@VideoActivity, ExoPlayerActivity::class.java).apply {
                putExtra("VIDEO_URL", video.videoUrl)
            }
            startActivity(intent)
        }
        recyclerView.adapter = videoAdapter

        if (isPermissionGranted()) {
            loadVideos()
        } else {
            requestPermissionsIfNeeded()
        }
    }

    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
                else -> {
                    true
                }
            }
        } else {
            true
        }
    }

    private fun requestPermissionsIfNeeded() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_VIDEO), REQUEST_PERMISSION)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
            }
            else -> {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVideos()
            } else {
                Toast.makeText(this, "Permission denied! Can't load videos.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadVideos() {
        // Run on background thread to avoid UI freeze
        CoroutineScope(Dispatchers.IO).launch {
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION
            )

            val cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Video.Media.DATE_ADDED + " DESC" // Sort by recently added
            )

            cursor?.use {
                val titleIndex = it.getColumnIndex(MediaStore.Video.Media.TITLE)
                val dataIndex = it.getColumnIndex(MediaStore.Video.Media.DATA)
                val durationIndex = it.getColumnIndex(MediaStore.Video.Media.DURATION)

                videoList.clear() // Clear any previous videos

                while (it.moveToNext()) {
                    val title = it.getString(titleIndex)
                    val videoUrl = it.getString(dataIndex)
                    val duration = it.getLong(durationIndex)
                    val thumbnail = getVideoThumbnail(videoUrl)

                    val video = VideoModel(title, videoUrl, thumbnail, duration)
                    videoList.add(video) // Add the new video to the list
                }
            }

            // Update UI on the main thread after loading data
            withContext(Dispatchers.Main) {
                videoAdapter.submitList(ArrayList(videoList)) // Use ListAdapter's submitList
                Log.d("VideoActivity", "Loaded ${videoList.size} videos")
            }
        }
    }

    private fun getVideoThumbnail(videoPath: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val thumbnail = retriever.getFrameAtTime(1000000) // Get thumbnail at 1 second
            if (thumbnail == null) {
                Log.e("VideoActivity", "Failed to retrieve thumbnail for $videoPath")
            }
            thumbnail
        } catch (e: Exception) {
            Log.e("VideoActivity", "Error retrieving thumbnail: ${e.message}")
            null
        }
    }
}
