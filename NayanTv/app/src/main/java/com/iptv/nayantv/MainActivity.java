package com.iptv.nayantv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // ── Model ──────────────────────────────────────────
    static class Channel {
        String id, name, category, logo, m3u8;
        Channel(String id, String name, String category, String logo, String m3u8) {
            this.id = id; this.name = name; this.category = category;
            this.logo = logo; this.m3u8 = m3u8;
        }
    }

    // ── Adapter ────────────────────────────────────────
    static class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.VH> {
        interface OnClick { void onClick(Channel ch); }
        private List<Channel> list = new ArrayList<>();
        private final OnClick listener;
        ChannelAdapter(OnClick listener) { this.listener = listener; }
        void submit(List<Channel> data) { list = data; notifyDataSetChanged(); }

        static class VH extends RecyclerView.ViewHolder {
            ImageView logo; TextView name;
            VH(View v) {
                super(v);
                logo = v.findViewById(R.id.ivLogo);
                name = v.findViewById(R.id.tvName);
            }
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_channel, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            Channel ch = list.get(position);
            holder.name.setText(ch.name);
            Glide.with(holder.logo)
                    .load(ch.logo)
                    .placeholder(android.R.drawable.ic_media_play)
                    .into(holder.logo);
            holder.itemView.setOnClickListener(v -> listener.onClick(ch));
        }

        @Override public int getItemCount() { return list.size(); }
    }

    // ── Activity ───────────────────────────────────────
    private static final String API_URL =
            "https://live-stream-api--systemfuck.replit.app/api/channels";
    private static final List<String> ORDER =
            Arrays.asList("Sports", "Bangla", "Hindi", "Kids", "Other");

    private ChannelAdapter adapter;
    private Map<String, List<Channel>> allData = new HashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv       = findViewById(R.id.rvChannels);
        ProgressBar spinner   = findViewById(R.id.progressBar);
        LinearLayout tabRow   = findViewById(R.id.tabRow);

        adapter = new ChannelAdapter(ch -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("name", ch.name);
            intent.putExtra("url",  ch.m3u8);
            intent.putExtra("logo", ch.logo);
            startActivity(intent);
        });

        rv.setLayoutManager(new GridLayoutManager(this, 3));
        rv.setAdapter(adapter);

        executor.execute(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                JSONObject root = new JSONObject(sb.toString());
                JSONObject cats = root.getJSONObject("categories");
                Map<String, List<Channel>> data = new HashMap<>();

                Iterator<String> keys = cats.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONArray arr = cats.getJSONArray(key);
                    List<Channel> ch = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject o = arr.getJSONObject(i);
                        ch.add(new Channel(
                                o.getString("id"),
                                o.getString("name"),
                                o.getString("category"),
                                o.getString("logo"),
                                o.getString("m3u8")
                        ));
                    }
                    data.put(key, ch);
                }

                mainHandler.post(() -> {
                    allData = data;
                    spinner.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);

                    for (String cat : ORDER) {
                        if (!allData.containsKey(cat)) continue;
                        Button btn = new Button(this);
                        btn.setText(cat);
                        btn.setBackgroundColor(0xFF1E1E1E);
                        btn.setTextColor(0xFFAAAAAA);
                        btn.setPadding(32, 8, 32, 8);
                        String finalCat = cat;
                        btn.setOnClickListener(v ->
                                adapter.submit(allData.get(finalCat)));
                        tabRow.addView(btn);
                    }

                    for (String cat : ORDER) {
                        if (allData.containsKey(cat)) {
                            adapter.submit(allData.get(cat));
                            break;
                        }
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    spinner.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
