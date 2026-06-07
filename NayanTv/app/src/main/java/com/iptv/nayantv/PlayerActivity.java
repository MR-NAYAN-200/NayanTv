package com.iptv.nayantv;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;

public class PlayerActivity extends AppCompatActivity {

    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String name = getIntent().getStringExtra("name");
        String url  = getIntent().getStringExtra("url");
        String logo = getIntent().getStringExtra("logo");

        TextView tvName       = findViewById(R.id.tvName);
        ImageView ivLogo      = findViewById(R.id.ivLogo);
        TextView tvLoading    = findViewById(R.id.tvLoading);
        ImageButton btnBack   = findViewById(R.id.btnBack);
        StyledPlayerView playerView = findViewById(R.id.playerView);

        tvName.setText(name);
        Glide.with(this).load(logo).into(ivLogo);
        btnBack.setOnClickListener(v -> finish());

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        HlsMediaSource mediaSource = new HlsMediaSource.Factory(
                new DefaultHttpDataSource.Factory())
                .createMediaSource(MediaItem.fromUri(url));

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                tvLoading.setVisibility(
                        state == Player.STATE_BUFFERING ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}
