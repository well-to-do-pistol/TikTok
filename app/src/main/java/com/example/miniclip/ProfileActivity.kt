package com.example.miniclip

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
import com.bumptech.glide.request.RequestOptions
import com.example.miniclip.adapter.ProfileVideoAdapter
import com.example.miniclip.databinding.ActivityProfileBinding
import com.example.miniclip.model.UserModel
import com.example.miniclip.model.VideoModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfileBinding
    lateinit var profileUserId : String
    lateinit var currentUserId : String
    lateinit var photoLauncher: ActivityResultLauncher<Intent>

    lateinit var adapter : ProfileVideoAdapter


    lateinit var profileUserModel : UserModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        profileUserId = intent.getStringExtra("profile_user_id")!! //从主界面到个人界面要学会加extra来传不同类别但相同的数据
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        photoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result-> //使用registerForActivityResult(ActivityResultContracts.StartActivityForResult())拿到ActivityResultLauncher<Intent>
            if(result.resultCode == RESULT_OK){
                uploadToFirestore(result.data?.data!!)
            }
        }

        if (profileUserId==currentUserId){
            //Current user profile
            binding.profileBtn.text = "Logout"
            binding.profileBtn.setOnClickListener{
                logout()
            }
            binding.profilePic.setOnClickListener{
                checkPermissionAndPickPhoto()
            }
        }else{
            binding.profileBtn.text = "Follow"
            binding.profileBtn.setOnClickListener{
                followUnfollwUser()
            }
        }
        getProfileDataFromFirebase() //主要目的是设置Ui
        setupRecyclerView()  //设置Profile的video
    }

    fun followUnfollwUser(){
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener {
                var currentUserModel = it.toObject(UserModel::class.java)!! //拿到user模型
                if(profileUserModel.followerList.contains(currentUserId)){
                    //unfollow user
                    profileUserModel.followerList.remove(currentUserId)
                    currentUserModel.followingList.remove(profileUserId)
                    binding.profileBtn.text = "Follow"
                }else{
                    //follow user
                    profileUserModel.followerList.add(currentUserId)
                    currentUserModel.followingList.add(profileUserId)
                    binding.profileBtn.text = "Unfollow"
                }
                updateUserData(profileUserModel)
                updateUserData(currentUserModel)


            }


    }

    fun updateUserData(model : UserModel){
        Firebase.firestore.collection("users")
            .document(model.id)
            .set(model)
            .addOnSuccessListener {
                getProfileDataFromFirebase() //重新从仓库拿到Model重新更新Ui
            }
    }

    fun uploadToFirestore(photoUri : Uri){
        binding.progressBar.visibility = View.VISIBLE
        val photoRef = FirebaseStorage.getInstance()
            .reference. //获取根目录
            child("profilePic/"+ currentUserId)
        photoRef.putFile(photoUri) //进行图片存储
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUrl->
                    //video model store in firebase firestore(视频model存储)
                    postToFirestore(downloadUrl.toString()) //把model存储在firestore, 传入downloadUrl
                }
            }
    }

    fun postToFirestore(url : String){
        Firebase.firestore.collection("users")
            .document(currentUserId)
            .update("profilePic",url)
            .addOnSuccessListener {
                getProfileDataFromFirebase()
            }
    }

    fun checkPermissionAndPickPhoto(){
        var readExternalPhoto : String = ""
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){  //请求权限
            readExternalPhoto = android.Manifest.permission.READ_MEDIA_IMAGES      //高版本
        }else{
            readExternalPhoto = android.Manifest.permission.READ_EXTERNAL_STORAGE //低版本
        }
        if(ContextCompat.checkSelfPermission(this,readExternalPhoto)== PackageManager.PERMISSION_GRANTED){ //检查是否获得权限
            //we have permission
            openPhotoPicker()
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(readExternalPhoto),
                100
            )
        }
    }

    private fun openPhotoPicker(){
        var intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*" //指定所有图片
        photoLauncher.launch(intent) //用videoLauncher启动mediastore, 再跳转去注册(获取视频本身)
    }

    fun logout(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this,LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //logout一定要设置Intent的flag
        startActivity(intent)
    }

    fun getProfileDataFromFirebase(){
        Firebase.firestore.collection("users")
            .document(profileUserId)
            .get()
            .addOnSuccessListener {
                profileUserModel = it.toObject(UserModel::class.java)!! //拿到user模型
                setUI()
            }
    }

    fun setUI(){
        profileUserModel.apply {
            Glide.with(binding.profilePic).load(profilePic)
                .apply ( RequestOptions().placeholder(R.drawable.icon_account_circle) )
                .circleCrop()
                .into(binding.profilePic)
            binding.profileUsername.text = "@"+ username
            if(profileUserModel.followerList.contains(currentUserId))
                binding.profileBtn.text = "Unfollow"
            binding.progressBar.visibility = View.INVISIBLE
            binding.followingCount.text = followingList.size.toString()
            binding.followerCount.text = followerList.size.toString()
            Firebase.firestore.collection("videos")
                .whereEqualTo("uploaderId",profileUserId)
                .get().addOnSuccessListener {
                    binding.postCount.text = it.size().toString()
                }

        }
    }

    fun setupRecyclerView(){
        val options = FirestoreRecyclerOptions.Builder<VideoModel>()
            .setQuery(
                Firebase.firestore.collection("videos")
                    .whereEqualTo("uploaderId",profileUserId)
                    .orderBy("createdTime",Query.Direction.DES)
            )
    }

}