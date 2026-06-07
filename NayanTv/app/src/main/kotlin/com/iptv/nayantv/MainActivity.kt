package com.iptv.nayantv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// ── Model ──────────────────────────────────────────────
data class Channel(
    val id: String,
    val name: String,
    val category: String,
    val logo: String,
    val m3u8: String
)

// ── API ────────────────────────────────────────────────
object Api {
    private const val URL = "https://live-stream-api--systemfuck.replit.app/api/channels"

    suspend fun fetchChannels(): Map<String, List<Channel>> = withContext(Dispatchers.IO) {
        val json = java.net.URL(URL).readText()
        val root = JSONObject(json)
        val cats = root.getJSONObject("categories")
        val result = mutableMapOf<String, List<Channel>>()
        for (key in cats.keys()) {
            val arr = cats.getJSONArray(key)
            val list = mutableListOf<Channel>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(Channel(
                    id       = obj.getString("id"),
                    name     = obj.getString("name"),
                    category = obj.getString("category"),
                    logo     = obj.getString("logo"),
                    m3u8     = obj.getString("m3u8")
                ))
            }
            result[key] = list
        }
        result
    }
}

// ── Adapter ────────────────────────────────────────────
class ChannelAdapter(private val onClick: (Channel) -> Unit) :
    RecyclerView.Adapter<ChannelAdapter.VH>() {

    private var list = listOf<Channel>()

    fun submit(data: List<Channel>) { list = data; notifyDataSetChanged() }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val logo: ImageView = v.findViewById(R.id.ivLogo)
        val name: TextView  = v.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        return VH(v)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ch = list[position]
        holder.name.text = ch.name
        Glide.with(holder.logo).load(ch.logo)
            .placeholder(android.R.drawable.ic_media_play).into(holder.logo)
        holder.itemView.setOnClickListener { onClick(ch) }
    }
}

// ── Activity ───────────────────────────────────────────
class MainActivity : AppCompatActivity() {

    private val order = listOf("Sports", "Bangla", "Hindi", "Kids", "Other")
    private lateinit var adapter: ChannelAdapter
    private var allData = mapOf<String, List<Channel>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv       = findViewById<RecyclerView>(R.id.rvChannels)
        val spinner  = findViewById<ProgressBar>(R.id.progressBar)
        val tabStrip = findViewById<HorizontalScrollView>(R.id.tabScroll)
        val tabRow   = findViewById<LinearLayout>(R.id.tabRow)

        adapter = ChannelAdapter { ch ->
            startActivity(Intent(this, PlayerActivity::class.java).apply {
                putExtra("name", ch.name)
                putExtra("url",  ch.m3u8)
                putExtra("logo", ch.logo)
            })
        }
        rv.layoutManager = GridLayoutManager(this, 3)
        rv.adapter = adapter

        lifecycleScope.launch {
            try {
                allData = Api.fetchChannels()
                spinner.visibility = View.GONE
                rv.visibility = View.VISIBLE

                order.filter { allData.containsKey(it) }.forEach { cat ->
                    val btn = Button(this@MainActivity).apply {
                        text = cat
                        setBackgroundColor(0xFF1E1E1E.toInt())
                        setTextColor(0xFFAAAAAA.toInt())
                        setPadding(32, 8, 32, 8)
                        setOnClickListener {
                            adapter.submit(allData[cat] ?: emptyList())
                        }
                    }
                    tabRow.addView(btn)
                }

                val first = order.firstOrNull { allData.containsKey(it) }
                first?.let { adapter.submit(allData[it] ?: emptyList()) }

            } catch (e: Exception) {
                spinner.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
