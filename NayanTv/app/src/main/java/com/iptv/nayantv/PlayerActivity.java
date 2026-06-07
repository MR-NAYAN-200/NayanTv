package com.iptv.nayantv;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        String name = getIntent().getStringExtra("name");
        String url  = getIntent().getStringExtra("url");

        TextView tvName     = findViewById(R.id.tvName);
        TextView tvLoading  = findViewById(R.id.tvLoading);
        ImageButton btnBack = findViewById(R.id.btnBack);
        videoView           = findViewById(R.id.videoView);

        tvName.setText(name);
        btnBack.setOnClickListener(v -> finish());

        MediaController mc = new MediaController(this);
        mc.setAnchorView(videoView);
        videoView.setMediaController(mc);
        videoView.setVideoURI(Uri.parse(url));

        tvLoading.setVisibility(View.VISIBLE);

        videoView.setOnPreparedListener(mp -> {
            tvLoading.setVisibility(View.GONE);
            mp.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            tvLoading.setText("Stream error. Try another channel.");
            return true;
        });

        videoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) videoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) videoView.stopPlayback();
    }
}
