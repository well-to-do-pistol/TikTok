package com.example.miniclip.adapter

import android.content.Intent
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.miniclip.ProfileActivity
import com.example.miniclip.R
import com.example.miniclip.databinding.VideoItemRowBinding
import com.example.miniclip.model.UserModel
import com.example.miniclip.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VideoListAdapter(
    options: FirestoreRecyclerOptions<VideoModel>
) : FirestoreRecyclerAdapter<VideoModel,VideoListAdapter.VideoViewHolder>(options) {


    inner class VideoViewHolder(private val binding : VideoItemRowBinding) : RecyclerView.ViewHolder(binding.root){ //创建Holder时传入binding
        fun bindVideo(videoModel: VideoModel){
            //bindUserData
            Firebase.firestore.collection("users")
                .document(videoModel.uploaderId)
                .get().addOnSuccessListener {
                    val userModel = it?.toObject(UserModel::class.java)
                    userModel?.apply {
                        binding.usernameView.text = username
                        //bind profilepic
                        Glide.with(binding.profileIcon).load(profilePic) //加载用户头像, Glide执行网络请求
                            .circleCrop()
                            .apply( //这会将附加选项应用于 Glide 请求。在这里，它设置了一个占位符图像 (`R.drawable.icon_profile`)，当主图像加载或主图像加载失败时会显示该图像。
                                RequestOptions().placeholder(R.drawable.icon_account_circle)
                            )
                            .into(binding.profileIcon)

                        binding.userDetailLayout.setOnClickListener {
                            val intent = Intent(binding.userDetailLayout.context, ProfileActivity::class.java)
                            intent.putExtra("profile_user_id", id )
                            binding.userDetailLayout.context.startActivity(intent) //要用linerlayout来启动Intent和获取linerlayout的context作为参考?
                        }

                    }
                }

            binding.captionView.text = videoModel.title
            binding.progressBar.visibility = View.VISIBLE

            //bindVideo
            binding.videoView.apply {
                setVideoPath(videoModel.url) //拿到路径
                setOnPreparedListener{
                    binding.progressBar.visibility = View.GONE
                    it.start() //直接开播
                    it.isLooping = true
                }
                //play pause
                setOnClickListener{
                    if(isPlaying){
                        pause()
                        binding.pauseIcon.visibility = View.VISIBLE
                    }else{
                        start()
                        binding.pauseIcon.visibility = View.GONE
                    }
                }
            }
            binding.videoView.setOnInfoListener { mp, what, extra -> //设置缓冲提醒
                when (what) {
                    MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                        binding.progressBar.visibility = View.VISIBLE // Show loading animation
                        true
                    }
                    MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        binding.progressBar.visibility = View.GONE // Hide loading animation
                        true
                    }
                    MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                        binding.progressBar.visibility = View.GONE // Hide loading animation when video starts playing
                        true
                    }
                    else -> false
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideoItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false) //Video视图利用parent来膨胀
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
        holder.bindVideo(model)
    }
}