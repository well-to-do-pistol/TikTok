package com.example.miniclip

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.miniclip.adapter.VideoListAdapter
import com.example.miniclip.databinding.ActivityLoginBinding
import com.example.miniclip.databinding.ActivityMainBinding
import com.example.miniclip.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var adapter: VideoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
//        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.bottomNavBar.setOnItemSelectedListener {menuItem->
            when(menuItem.itemId){
                R.id.bottom_menu_home->{

                }
                R.id.bottom_menu_add_video->{
                    startActivity(Intent(this,VideoUploadActivity::class.java))
                }
                R.id.bottom_menu_profile->{
                    //Goto ProfileActivity
                    val intent = Intent(this,ProfileActivity::class.java)
                    intent.putExtra("profile_user_id", FirebaseAuth.getInstance().currentUser?.uid )
                    startActivity(intent)
                }
            }
            false
        }
        setupViewPaper()

    }

    private fun setupViewPaper(){
        val options = FirestoreRecyclerOptions.Builder<VideoModel>() //用FirestoreRecyclerOptions准备启动adapter, 使用Query
            .setQuery(
                Firebase.firestore.collection("videos"),
                VideoModel::class.java
            ).build()
        adapter = VideoListAdapter(options) //创建VideoListAdapter并传入FirestoreRecyclerOptions<VideoModel>
        binding.viewPaper.adapter = adapter //不用写recyclerView直接写视图名字(androidx.viewpager2.widget.ViewPager2)
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
//        adapter.stopListening() //这里和教程不一样???
        adapter.startListening()
    }

}