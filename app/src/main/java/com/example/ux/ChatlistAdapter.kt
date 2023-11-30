package com.example.ux

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.databinding.ChatlistItemViewBinding
import com.example.ux.model.ChatRoom
import com.google.firebase.auth.FirebaseAuth

class ChatlistAdapter(val context: Context, private val items: Array<ChatlistData>) :
    RecyclerView.Adapter<ChatlistAdapter.ChatlistViewHolder>() {

    var chatRooms: ArrayList<ChatRoom> = arrayListOf()   //채팅방 목록
    var chatRoomKeys: ArrayList<String> = arrayListOf()  //채팅방 키 목록
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()   //현재 사용자 Uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatlistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ChatlistItemViewBinding.inflate(inflater, parent, false)
        return ChatlistViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ChatlistViewHolder, position: Int) {
        val item = items[position]

        var userIdList = chatRooms[position].member!!.keys    //채팅방에 포함된 사용자 키 목록
//        opponent = userIdList  //상대방 사용자 키

        val listener = View.OnClickListener { it ->
            Toast.makeText(
                it.context,
                "Clicked -> Roomname : ${item.Roomname}, last msg : ${item.lastmsg}",
                Toast.LENGTH_SHORT
            ).show()
            for (room in chatRoomKeys) {
                if (item.Roomname == room) {
                    try {
                        var intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra("ChatRoom", chatRooms[position])      //채팅방 정보
                        intent.putExtra("Opponent", "Sf0s6nZztYhF2xMBTynpxl1ZjPm2")          //상대방 사용자 정보
                        intent.putExtra("ChatRoomKey", chatRoomKeys[position])     //채팅방 키 정보
                        context.startActivity(intent)                            //해당 채팅방으로 이동
                        (context as AppCompatActivity).finish()
                    }catch (e:Exception)
                    {
                        e.printStackTrace()
                        Toast.makeText(context,"채팅방 이동 중 문제가 발생하였습니다.",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        holder.bind(listener, item)
    }


    class ChatlistViewHolder(private val binding: ChatlistItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        //var textField = view.chat_title
        fun bind(listener: View.OnClickListener, item: ChatlistData) {
            binding.apply {
                chatroomIcon.setBackgroundResource(item.imgResId)
                txtChatlistRoomTitle.text = item.Roomname
                txtChatlistLastMsg.text = item.lastmsg
                root.setOnClickListener(listener)
            }
        }
    }
}

