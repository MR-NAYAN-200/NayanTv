package com.iptv.nayantv

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import com.bumptech.glide.Glide

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val name = intent.getStringExtra("name") ?: ""
        val url  = intent.getStringExtra("url")  ?: ""
        val logo = intent.getStringExtra("logo") ?: ""

        val tvName    = findViewById<TextView>(R.id.tvName)
        val ivLogo    = findViewById<ImageView>(R.id.ivLogo)
        val tvLoading = findViewById<TextView>(R.id.tvLoading)
        val btnBack   = findViewById<ImageButton>(R.id.btnBack)
        val playerView = findViewById<PlayerView>(R.id.playerView)

        tvName.text = name
        Glide.with(this).load(logo).into(ivLogo)
        btnBack.setOnClickListener { finish() }

        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView.player = exo
            val src = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
                .createMediaSource(MediaItem.fromUri(url))
            exo.setMediaSource(src)
            exo.prepare()
            exo.playWhenReady = true

            exo.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    tvLoading.visibility =
                        if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
