package com.example.miniclip

import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.miniclip.databinding.ActivityMainBinding
import com.example.miniclip.databinding.ActivityVideoUploadBinding
import com.example.miniclip.model.VideoModel
import com.example.miniclip.util.UiUtil
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class VideoUploadActivity : AppCompatActivity() {

    lateinit var binding: ActivityVideoUploadBinding
    private var selectedVideoUri: Uri? = null
    lateinit var videoLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoUploadBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result-> //使用registerForActivityResult(ActivityResultContracts.StartActivityForResult())拿到ActivityResultLauncher<Intent>
            if(result.resultCode == RESULT_OK){
                selectedVideoUri = result.data?.data //拿到Uri(和拿照相机或图片的Picker库有点像)
                showPostView();
            }
        }
        binding.uploadView.setOnClickListener{
            checkPermissionAndOpenVideoPicker()
        }

        binding.submitPostBtn.setOnClickListener{
            postVideo();
        }

        binding.cancelPostBtn.setOnClickListener{
            finish()
        }

    }

    private fun postVideo(){
        if(binding.postCaptionInput.text.toString().isEmpty()){
            binding.postCaptionInput.setError("Write something")
            return
        }
        setInProgress(true)
        selectedVideoUri?.apply { //用拿到的Uri进行存储
            //store in firebase cloud storage(视频存储)

            val videoRef = FirebaseStorage.getInstance()
                .reference. //获取根目录
                child("videos/"+ this.lastPathSegment ) //this是selectedVideoUri, 用uri的最后路径名
            videoRef.putFile(this) //进行视频存储
                .addOnSuccessListener {
                    videoRef.downloadUrl.addOnSuccessListener { downloadUrl->
                        //video model store in firebase firestore(视频model存储)
                        postToFirestore(downloadUrl.toString()) //把model存储在firestore, 传入downloadUrl
                    }
                }


        }
    }

    private fun postToFirestore(url : String){ //要传入url
        val videoModel = VideoModel(
            FirebaseAuth.getInstance().currentUser?.uid!! + "_"+Timestamp.now().toString(), //videoId为用户Id+时间戳
            binding.postCaptionInput.text.toString(), //输入的标题
            url,
            FirebaseAuth.getInstance().currentUser?.uid!!, //当前用户id
            Timestamp.now(),
        )
        Firebase.firestore.collection("videos") //存在firestore不是storage里, 文件名为videoId, 内容为videoModel
            .document(videoModel.videoId)
            .set(videoModel)
            .addOnSuccessListener {
                setInProgress(false)
                UiUtil.showToast(applicationContext, "Video uploaded")
                finish()
            }.addOnFailureListener{
                setInProgress(false)
                UiUtil.showToast(applicationContext,"Video failed to upload")
            }
    }

    private fun setInProgress(inProgress : Boolean){
        if(inProgress){
            binding.progressBar.visibility = View.VISIBLE
            binding.submitPostBtn.visibility = View.GONE
        }else{
            binding.progressBar.visibility = View.GONE
            binding.submitPostBtn.visibility = View.VISIBLE
        }
    }

    private fun showPostView(){
        selectedVideoUri?.let {
            binding.postView.visibility = View.VISIBLE //显示post视图
            binding.uploadView.visibility = View.GONE
            Glide.with(binding.postThumbnailView).load(it).into(binding.postThumbnailView)//直接把Uri放进postThumbnailView
        }
    }

    private fun checkPermissionAndOpenVideoPicker(){
        var readExternalVideo : String = ""
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){  //请求权限
            readExternalVideo = android.Manifest.permission.READ_MEDIA_VIDEO      //高版本
        }else{
            readExternalVideo = android.Manifest.permission.READ_EXTERNAL_STORAGE //低版本
        }
        if(ContextCompat.checkSelfPermission(this,readExternalVideo)==PackageManager.PERMISSION_GRANTED){ //检查是否获得权限
            //we have permission
            openVideoPicker()
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(readExternalVideo),
                100
            )
        }
    }

    private fun openVideoPicker(){
        var intent = Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "video/*"
        videoLauncher.launch(intent) //用videoLauncher启动mediastore, 再跳转去注册(获取视频本身)
    }
}