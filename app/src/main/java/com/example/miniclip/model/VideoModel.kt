package com.example.miniclip.model

import com.google.firebase.Timestamp


data class VideoModel(
    var videoId : String = "",
    var title : String = "",
    var url : String = "", //url是从Uri中拿的
    var uploaderId : String = "",
    var createdTime : Timestamp = Timestamp.now()
)
