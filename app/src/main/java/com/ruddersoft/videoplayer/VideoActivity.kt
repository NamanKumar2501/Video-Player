package com.ruddersoft.videoplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ruddersoft.videoplayer.Adapter.VideoAdapter
import com.ruddersoft.videoplayer.model.VideoModel

class VideoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private val videoList = mutableListOf<VideoModel>()

    private val REQUEST_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        recyclerView = findViewById(R.id.rv_video_player)

        recyclerView.layoutManager = LinearLayoutManager(this)
        videoAdapter = VideoAdapter(videoList) { video ->
            val intent = Intent(this@VideoActivity, ExoPlayerActivity::class.java).apply {
                putExtra("VIDEO_URL", video.videoUrl)
            }
            startActivity(intent)
        }
        recyclerView.adapter = videoAdapter

        if (isPermissionGranted()) {
            loadVideos()
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        }
    }

    private fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
        } else {
            true
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
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION // Add duration column
        )

        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val titleIndex = it.getColumnIndex(MediaStore.Video.Media.TITLE)
            val dataIndex = it.getColumnIndex(MediaStore.Video.Media.DATA)
            val durationIndex = it.getColumnIndex(MediaStore.Video.Media.DURATION)

            while (it.moveToNext()) {
                val title = it.getString(titleIndex)
                val videoUrl = it.getString(dataIndex)
                val duration = it.getLong(durationIndex) // Duration in milliseconds
                val thumbnail = getVideoThumbnail(videoUrl)
                val video = VideoModel(title, videoUrl, thumbnail, duration)
                videoList.add(video)
            }
        }

        videoAdapter.notifyDataSetChanged()
    }

    private fun getVideoThumbnail(videoPath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoPath)
        return retriever.getFrameAtTime(1000000)
    }
}