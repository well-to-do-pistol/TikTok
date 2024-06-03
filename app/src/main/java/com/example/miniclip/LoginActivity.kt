package com.example.miniclip

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.miniclip.databinding.ActivityLoginBinding
import com.example.miniclip.model.UserModel
import com.example.miniclip.util.UiUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseAuth.getInstance().currentUser?.let {
            //user is there logged in
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        binding.submitBtn.setOnClickListener{
            login();
        }

        binding.goToSignupBtn.setOnClickListener{
            startActivity(Intent(this,SignupActivity::class.java))
            finish()
        }

    }

    fun setInProgress(inProgress : Boolean){
        if(inProgress){
            binding.progressBar.visibility = View.VISIBLE
            binding.submitBtn.visibility = View.GONE
        }else{
            binding.progressBar.visibility = View.GONE
            binding.submitBtn.visibility = View.VISIBLE
        }
    }

    fun login(){
        val email = binding.emailInput.text.toString()
        val password = binding.passwordInput.text.toString()

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInput.setError("Email not valid")
            return
        }
        if(password.length<6){
            binding.passwordInput.setError("Minimum 6 character")
            return
        }

        loginWithFirebase(email,password)
    }

    fun loginWithFirebase(email : String, password : String){ //注册函数
        setInProgress(true)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            email,
            password
        ).addOnSuccessListener {
            UiUtil.showToast(this,"Login successfully")
            setInProgress(false)
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }.addOnFailureListener{
            UiUtil.showToast(applicationContext,it.localizedMessage?:"Something went wrong")
            setInProgress(false)
        }
    }
}