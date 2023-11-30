package com.example.ux.model

import java.io.Serializable

data class ChatRoom(
    val chatColor : String = "",
    val chatName : String = "",
    val member: Map<String, Boolean>? = HashMap(),
    var messages: Map<String, Message>? = HashMap()
) : Serializable {
}
