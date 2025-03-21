package com.educate.theteachingapp.videoPlayer.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.educate.theteachingapp.videoPlayer.extensions.round

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ruddersoft.videoplayer.R
import com.ruddersoft.videoplayer.databinding.PlaybackSpeedBinding

class PlaybackSpeedControlsDialogFragment(
    private val currentSpeed: Float,
    private val onChange: (Float) -> Unit
) : DialogFragment() {

    private lateinit var binding: PlaybackSpeedBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = PlaybackSpeedBinding.inflate(layoutInflater)

        return activity?.let { activity ->
            binding.apply {
                speedText.text = currentSpeed.toString()
                speed.value = currentSpeed.round(1)
                speed.addOnChangeListener { _, _, _ ->
                    val newSpeed = speed.value.round(1)
                    onChange(newSpeed)
                    speedText.text = newSpeed.toString()
                }
                resetSpeed.setOnClickListener {
                    speed.value = 1.0f
                }
                incSpeed.setOnClickListener {
                    if (speed.value < 4.0f) {
                        speed.value = (speed.value + 0.1f).round(1)
                    }
                }
                decSpeed.setOnClickListener {
                    if (speed.value > 0.2f) {
                        speed.value = (speed.value - 0.1f).round(1)
                    }
                }
            }

            val builder = MaterialAlertDialogBuilder(activity)
            builder.setTitle(getString(R.string.select_playback_speed))
                .setView(binding.root)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
