package com.example.miniclip

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.miniclip.adapter.VideoListAdapter
import com.example.miniclip.databinding.ActivitySingleVideoPlayerBinding
import com.example.miniclip.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SingleVideoPlayerActivity : AppCompatActivity() {

    lateinit var binding: ActivitySingleVideoPlayerBinding
    lateinit var videoId : String
    lateinit var adapter : VideoListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingleVideoPlayerBinding.inflate(layoutInflater)
//        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        videoId = intent.getStringExtra("videoId")!!
        setupViewPager()

    }

    fun setupViewPager(){
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                    .whereEqualTo("videoId",videoId),
                VideoModel::class.java
            ).build()
        adapter = VideoListAdapter(options)
        binding.viewPager.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stopListening()
    }
}