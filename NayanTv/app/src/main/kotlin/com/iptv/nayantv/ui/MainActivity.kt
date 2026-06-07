package com.iptv.nayantv.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.iptv.nayantv.api.ApiService
import com.iptv.nayantv.adapter.ChannelAdapter
import com.iptv.nayantv.databinding.ActivityMainBinding
import com.iptv.nayantv.model.Channel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ChannelAdapter
    private val apiService = ApiService.create()

    private var allCategories: Map<String, List<Channel>> = emptyMap()
    private val categoryOrder = listOf("Sports", "Bangla", "Hindi", "Kids", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupTabLayout()
        loadChannels()
    }

    private fun setupRecyclerView() {
        adapter = ChannelAdapter { channel ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_CHANNEL_NAME, channel.name)
                putExtra(PlayerActivity.EXTRA_CHANNEL_URL, channel.m3u8)
                putExtra(PlayerActivity.EXTRA_CHANNEL_LOGO, channel.logo)
            }
            startActivity(intent)
        }

        binding.rvChannels.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val category = tab?.text?.toString() ?: return
                val channels = allCategories[category] ?: emptyList()
                adapter.submitList(channels)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadChannels() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvChannels.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = apiService.getChannels()
                if (response.isSuccessful) {
                    val body = response.body()!!
                    allCategories = body.categories

                    binding.tabLayout.removeAllTabs()
                    categoryOrder.forEach { category ->
                        if (allCategories.containsKey(category)) {
                            binding.tabLayout.addTab(
                                binding.tabLayout.newTab().setText(category)
                            )
                        }
                    }

                    val firstCategory = categoryOrder.firstOrNull { allCategories.containsKey(it) }
                    firstCategory?.let {
                        adapter.submitList(allCategories[it] ?: emptyList())
                    }

                    binding.progressBar.visibility = View.GONE
                    binding.rvChannels.visibility = View.VISIBLE
                } else {
                    showError("Failed to load channels")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
