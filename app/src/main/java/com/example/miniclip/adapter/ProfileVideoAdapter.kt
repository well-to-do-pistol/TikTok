package com.example.miniclip.adapter


import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.example.miniclip.SingleVideoPlayerActivity
import com.example.miniclip.databinding.ProfileVideoItemRowBinding
import com.example.miniclip.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ProfileVideoAdapter(options: FirestoreRecyclerOptions<VideoModel>) //adapter要传入FirestoreRecyclerOptions<VideoModel>参数
    : FirestoreRecyclerAdapter<VideoModel,ProfileVideoAdapter.VideoViewHolder>(options)
{


    inner class VideoViewHolder(private val binding: ProfileVideoItemRowBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(video : VideoModel){
            Glide.with(binding.thumbnailImageView) //放置视频的图片
                .load(video.url)
                .into(binding.thumbnailImageView)
            binding.thumbnailImageView.setOnClickListener{
                val intent = Intent(binding.thumbnailImageView.context,SingleVideoPlayerActivity::class.java)
                intent.putExtra("videoId",video.videoId)
                binding.thumbnailImageView.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ProfileVideoItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false) //holder都是从parent拿context
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int, model: VideoModel) {
        holder.bind(model)
    }
}