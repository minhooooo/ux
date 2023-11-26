package com.example.ux.model

data class VoteData(val chatId : String, val userId:String, val currentweek:Array<String>, val item: String, var isopend: Boolean =true)