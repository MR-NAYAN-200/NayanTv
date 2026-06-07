package com.iptv.nayantv.ui

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultHttpDataSource
import com.bumptech.glide.Glide
import com.iptv.nayantv.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    private var channelName = ""
    private var channelUrl = ""
    private var channelLogo = ""

    companion object {
        const val EXTRA_CHANNEL_NAME = "channel_name"
        const val EXTRA_CHANNEL_URL = "channel_url"
        const val EXTRA_CHANNEL_LOGO = "channel_logo"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        channelName = intent.getStringExtra(EXTRA_CHANNEL_NAME) ?: ""
        channelUrl = intent.getStringExtra(EXTRA_CHANNEL_URL) ?: ""
        channelLogo = intent.getStringExtra(EXTRA_CHANNEL_LOGO) ?: ""

        binding.tvChannelName.text = channelName
        Glide.with(this).load(channelLogo).into(binding.ivChannelLogo)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnPip.setOnClickListener { enterPipMode() }

        initPlayer()
    }

    private fun initPlayer() {
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer

            val dataSourceFactory = DefaultHttpDataSource.Factory()
            val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(channelUrl))

            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            binding.tvLoading.visibility = View.VISIBLE

            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            binding.tvLoading.visibility = View.GONE
                        }
                        Player.STATE_BUFFERING -> {
                            binding.tvLoading.visibility = View.VISIBLE
                        }
                        Player.STATE_ENDED, Player.STATE_IDLE -> {
                            binding.tvLoading.visibility = View.GONE
                        }
                    }
                }
            })
        }
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            enterPictureInPictureMode(params)
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode) {
            binding.topBar.visibility = View.GONE
        } else {
            binding.topBar.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isInPictureInPictureMode) {
            player?.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
