package com.example.ux

import java.io.Serializable

data class ChatlistData(
    var imgResId : Int,
    var Roomname: String,
    val lastmsg: String
) : Serializable {
}
