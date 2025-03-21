package com.ruddersoft.videoplayer.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruddersoft.videoplayer.R
import com.ruddersoft.videoplayer.model.VideoModel
import java.util.concurrent.TimeUnit

class VideoAdapter(
    private val onItemClick: (VideoModel) -> Unit // Callback for item clicks
) : ListAdapter<VideoModel, VideoAdapter.VideoViewHolder>(VideoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = getItem(position)  // Get the item at position
        holder.bind(video)
        holder.itemView.setOnClickListener {
            onItemClick(video)
        }
    }

    inner class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val videoThumbnail: ImageView = view.findViewById(R.id.videoThumbnail)

        fun bind(video: VideoModel) {
            tvTitle.text = video.title
            tvDuration.text = formatDuration(video.duration) // Format and set duration
            Glide.with(videoThumbnail.context)
                .load(video.thumbnail)
                .into(videoThumbnail)
        }

        private fun formatDuration(durationMs: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(durationMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60

            return when {
                hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
                else -> String.format("%d:%02d", minutes, seconds)
            }
        }
    }

    // DiffUtil callback to detect differences between old and new list
    class VideoDiffCallback : DiffUtil.ItemCallback<VideoModel>() {
        override fun areItemsTheSame(oldItem: VideoModel, newItem: VideoModel): Boolean {
            return oldItem.videoUrl == newItem.videoUrl
        }

        override fun areContentsTheSame(oldItem: VideoModel, newItem: VideoModel): Boolean {
            return oldItem == newItem
        }
    }
}
