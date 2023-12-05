package com.example.ux

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.databinding.ChatlistItemViewBinding
import com.example.ux.model.ChatRoom
import com.example.ux.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class RecyclerChatRoomsAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerChatRoomsAdapter.ViewHolder>() {
    var chatRooms: ArrayList<ChatRoom> = arrayListOf()   //채팅방 목록
    var chatRoomKeys: ArrayList<String> = arrayListOf()  //채팅방 키 목록
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()   //현재 사용자 Uid

    init {
        setupAllUserList()
    }

    fun setupAllUserList() {
        FirebaseDatabase.getInstance().getReference("chat")
            .orderByChild("member/$myUid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    val newChatRooms = mutableListOf<ChatRoom>()  // 새로운 정렬된 리스트
                    val newChatRoomKeys = mutableListOf<String>() // 새로운 키 리스트

                    for (data in snapshot.children) {
                        val chatRoom = data.getValue<ChatRoom>()!!
                        newChatRooms.add(chatRoom)
                        newChatRoomKeys.add(data.key!!)
                    }

                    val sortedPairs = newChatRooms.zip(newChatRoomKeys)
                        .sortedByDescending { (chatRoom, _) -> calculateLastMessageTimestamp(chatRoom) }

                    // 언팩해서 갱신
                    chatRooms.clear()
                    chatRoomKeys.clear()
                    for ((room, key) in sortedPairs) {
                        chatRooms.add(room)
                        chatRoomKeys.add(key)
                    }

                    notifyDataSetChanged()
                }
            })
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.chatlist_item_view, parent, false)
        return ViewHolder(ChatlistItemViewBinding.bind(view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var userIdList = chatRooms[position].member!!.keys    //채팅방에 포함된 사용자 키 목록
        var opponent = userIdList.firstOrNull { it != myUid } // 상대방 사용자 키를 찾음
        if (opponent != null) {
            FirebaseDatabase.getInstance().getReference("moi").orderByChild("uid")   //상대방 사용자 키를 포함하는 채팅방 불러오기
                .equalTo(opponent)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (data in snapshot.children) {
                            holder.chatRoomKey = data.key.toString()!!             //채팅방 키 초기화
                            holder.opponentUser = data.getValue<User>()!!         //상대방 정보 초기화
                        }
                    }
                })
            holder.background.setOnClickListener()               //채팅방 항목 선택 시

            {
                try {
                    var intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("ChatRoom", chatRooms[position])      //채팅방 정보
                    intent.putExtra("Opponent", holder.opponentUser)          //상대방 사용자 정보
                    intent.putExtra("ChatRoomKey", chatRoomKeys[position])     //채팅방 키 정보
                    context.startActivity(intent)                            //해당 채팅방으로 이동
                    (context as AppCompatActivity).finish()
                }catch (e:Exception)
                {
                    e.printStackTrace()
                    Toast.makeText(context,"채팅방 이동 중 문제가 발생하였습니다.",Toast.LENGTH_SHORT).show()
                }
            }

            if (chatRooms[position].chatName!!.isNotEmpty()) {
                holder.txt_name.text = chatRooms[position].chatName
            }

            if (chatRooms[position].chatColor!!.isNotEmpty()) {
                // chatColor 기반으로 리소스 ID 동적으로 생성
                val resourceId = context.resources.getIdentifier(chatRooms[position].chatColor, "drawable", context.packageName)
                holder.chatroomIcon.setBackgroundResource(resourceId)
            }

            if (chatRooms[position].messages!!.isNotEmpty()) {         //채팅방 메시지가 존재하는 경우
                setupLastMessageAndDate(holder, position)        //마지막 메시지 및 시각 초기화
//                setupMessageCount(holder, position)
            }
        }
    }

    private fun calculateLastMessageTimestamp(chatRoom: ChatRoom): Long {
        val lastMessage = chatRoom.messages?.values?.maxByOrNull { it.sended_date }
        return lastMessage?.sended_date?.toLongOrNull() ?: 0
    }

    private fun setupLastMessageAndDate(holder: ViewHolder, position: Int) { //마지막 메시지 및 시각 초기화
        try {
            var lastMessage =
                chatRooms[position].messages!!.values.sortedWith(compareBy { it.sended_date })    //메시지 목록에서 시각을 비교하여 가장 마지막 메시지  가져오기
                    .last()
            holder.txt_message.text = lastMessage.content                 //마지막 메시지 표시
            holder.txt_date.text = getLastMessageTimeString(lastMessage.sended_date)   //마지막으로 전송된 시각 표시
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupMessageCount(holder: ViewHolder, position: Int) {            //확인되지 않은 메시지 개수 표시
//        try {
//            var unconfirmedCount =
//                chatRooms[position].messages!!.filter {
//                    !it.value.confirmed && !it.value.senderUid.equals(               //메시지 중 확인되지 않은 메시지 개수 가져오기
//                        myUid
//                    )
//                }.size
//            if (unconfirmedCount > 0) {              //확인되지 않은 메시지가 있을 경우
//                holder.txt_chatCount.visibility = View.VISIBLE           //개수 표시
//                holder.txt_chatCount.text = unconfirmedCount.toString()
//            } else
//                holder.txt_chatCount.visibility = View.GONE
//        } catch (e: Exception) {
//            e.printStackTrace()
//            holder.txt_chatCount.visibility = View.GONE
//        }
    }

    fun getLastMessageTimeString(lastTimeString: String): String {           //마지막 메시지가 전송된 시각 구하기
        try {
            var currentTime = LocalDateTime.now().atZone(TimeZone.getDefault().toZoneId()) //현재 시각
            var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

            var messageMonth = lastTimeString.substring(4, 6).toInt()                   //마지막 메시지 시각 월,일,시,분
            var messageDate = lastTimeString.substring(6, 8).toInt()
            var messageHour = lastTimeString.substring(8, 10).toInt()
            var messageMinute = lastTimeString.substring(10, 12).toInt()

            var formattedCurrentTimeString = currentTime.format(dateTimeFormatter)     //현 시각 월,일,시,분
            var currentMonth = formattedCurrentTimeString.substring(4, 6).toInt()
            var currentDate = formattedCurrentTimeString.substring(6, 8).toInt()
            var currentHour = formattedCurrentTimeString.substring(8, 10).toInt()
            var currentMinute = formattedCurrentTimeString.substring(10, 12).toInt()

            var monthAgo = currentMonth - messageMonth                           //현 시각과 마지막 메시지 시각과의 차이. 월,일,시,분
            var dayAgo = currentDate - messageDate
            var hourAgo = currentHour - messageHour
            var minuteAgo = currentMinute - messageMinute

            if (monthAgo > 0)                                         //1개월 이상 차이 나는 경우
                return monthAgo.toString() + "개월 전"
            else {
                if (dayAgo > 0) {                                  //1일 이상 차이 나는 경우
                    if (dayAgo == 1)
                        return "어제"
                    else
                        return dayAgo.toString() + "일 전"
                } else {
                    if (hourAgo > 0)
                        return hourAgo.toString() + "시간 전"     //1시간 이상 차이 나는 경우
                    else {
                        if (minuteAgo > 0)                       //1분 이상 차이 나는 경우
                            return minuteAgo.toString() + "분 전"
                        else
                            return "방금"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    override fun getItemCount(): Int {
        return chatRooms.size
    }

    inner class ViewHolder(itemView: ChatlistItemViewBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var opponentUser = User("", "", "", "", "")
        var chatRoomKey = ""
        var background = itemView.background
        var chatroomIcon = itemView.chatroomIcon
        var txt_name = itemView.txtChatlistRoomTitle
        var txt_message = itemView.txtChatlistLastMsg
        var txt_date = itemView.txtMessageDate
//        var txt_chatCount = itemView.txtChatCount
    }
}