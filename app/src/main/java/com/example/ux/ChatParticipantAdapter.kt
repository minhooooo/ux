package com.example.ux

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.databinding.FriendItemViewBinding

class ChatParticipantAdapter(val context: Context,  private val items: Array<FriendData>) :
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
                System.out.println("chat pp"+item)
                val bgColor = item.imgName
                val resourceId = context.resources.getIdentifier(bgColor, "drawable", context.packageName)
                circleBackgroundDrawable.setColor(resourceId)

                if (chatMembers.contains(item)) {
                    friendItemLayout.background.setTint(Color.parseColor("#59002AFF"))
                    //circleBackgroundDrawable.setColor(R.drawable.btn_blue) // 선택된 아이템의 배경색을 변경
                } else {
                    friendItemLayout.background.setTint(Color.WHITE)
                    //circleBackgroundDrawable.setColor(Color.LTGRAY) // 선택되지 않은 아이템의 배경색을 기본 값으로 변경
                }

                friendItemNameTv.text = item.name
                friendItemUnivTv.text = item.university
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
            //선택해제
            chatMembers.remove(friend)
        } else {
            //선택
            chatMembers.add(friend)
        }
        notifyDataSetChanged()
    }

    fun getChatMembers(): MutableList<FriendData> {
        return chatMembers
    }

}