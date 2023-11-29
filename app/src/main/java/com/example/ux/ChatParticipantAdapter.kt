package com.example.ux

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.databinding.FriendItemViewBinding

class ChatParticipantAdapter(private val items: Array<FriendData>) :
    RecyclerView.Adapter<ChatParticipantAdapter.ChatParticipantViewHolder>() {

    private val chatMembers: MutableList<FriendData> = mutableListOf() // 선택된 멤버 리스트
    inner class ChatParticipantViewHolder(private val binding: FriendItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val friend = items[position]
                    toggleMemberSelection(friend)
                }
            }
        }
        fun bind(item: FriendData) {
            binding.apply {
                val circleBackgroundDrawable = friendItemPictureIv.background as GradientDrawable
                circleBackgroundDrawable.setColor(Color.LTGRAY)
                friendItemNameTv.text = item.name
                friendItemStatusTv.text = item.statusMsg
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatParticipantAdapter.ChatParticipantViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = FriendItemViewBinding.inflate(inflater, parent, false)
        return ChatParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ChatParticipantAdapter.ChatParticipantViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    fun toggleMemberSelection(friend: FriendData) {
        if (chatMembers.contains(friend)) {
            chatMembers.remove(friend)
        } else {
            chatMembers.add(friend)
        }
        notifyDataSetChanged()
    }

    fun getChatMembers(): MutableList<FriendData> {
        return chatMembers
    }

}