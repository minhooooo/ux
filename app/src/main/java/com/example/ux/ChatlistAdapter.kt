package com.example.ux

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.databinding.ActivityLoginBinding.bind
import com.example.ux.databinding.ChatlistItemViewBinding

class ChatlistAdapter(private val items: Array<ChatlistData>) :
    RecyclerView.Adapter<ChatlistAdapter.ChatlistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatlistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ChatlistItemViewBinding.inflate(inflater, parent, false)
        return ChatlistViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ChatlistViewHolder, position: Int) {
        val item = items[position]
        val listener = View.OnClickListener { it ->
            Toast.makeText(
                it.context,
                "Clicked -> Roomname : ${item.Roomname}, last msg : ${item.lastmsg}",
                Toast.LENGTH_SHORT
            ).show()
        }
        holder.bind(listener, item)
    }


    class ChatlistViewHolder(private val binding: ChatlistItemViewBinding) : RecyclerView.ViewHolder(binding.root) {
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

