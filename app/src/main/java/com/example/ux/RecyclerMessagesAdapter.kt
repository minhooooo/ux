package com.example.ux

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.databinding.DateMsgBinding
import com.example.ux.databinding.MessageListMineBinding
import com.example.ux.databinding.MessageListOthersBinding
import com.example.ux.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.util.*
import kotlin.collections.ArrayList

class RecyclerMessagesAdapter(
    val context: Context,
    var chatRoomKey: String?,
    val opponentUid: String?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var messages: ArrayList<Message> = arrayListOf()     //메시지 목록
    var messageKeys: ArrayList<String> = arrayListOf()   //메시지 키 목록
    val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val recyclerView = (context as ChatActivity).recycler_talks   //목록이 표시될 리사이클러 뷰

    init {
        setupMessages()
    }

    private fun setupMessages() {
        getMessages()
    }

    private fun getMessages() {
        FirebaseDatabase.getInstance().getReference("chat")
            .child(chatRoomKey!!).child("messages")   //전체 메시지 목록 가져오기
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (data in snapshot.children) {
                        messages.add(data.getValue<Message>()!!)         //메시지 목록에 추가
                        messageKeys.add(data.key!!)                        //메시지 키 목록에 추가
                    }
                    notifyDataSetChanged()          //화면 업데이트
                    recyclerView.scrollToPosition(messages.size - 1)    //스크롤 최 하단으로 내리기
                }
            })
    }

    override fun getItemViewType(position: Int): Int {               //메시지의 id에 따라 내 메시지/상대 메시지 구분
        return if (messages[position].senderUid == myUid) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {            //메시지가 내 메시지인 경우
                val view =
                    LayoutInflater.from(context)
                        .inflate(R.layout.message_list_mine, parent, false)   //내 메시지 레이아웃으로 초기화

                MyMessageViewHolder(MessageListMineBinding.bind(view))
            }
            else -> {      //메시지가 상대 메시지인 경우
                val view =
                    LayoutInflater.from(context)
                        .inflate(R.layout.message_list_others, parent, false)  //상대 메시지 레이아웃으로 초기화
                OtherMessageViewHolder(MessageListOthersBinding.bind(view))
            }
//            2 -> {      //메시지가 상대 메시지인 경우
//                val view =
//                    LayoutInflater.from(context)
//                        .inflate(R.layout.message_list_others, parent, false)  //상대 메시지 레이아웃으로 초기화
//                OtherMessageViewHolder(MessageListOthersBinding.bind(view))
//            } else -> {
//                val view =
//                    LayoutInflater.from(parent.context)
//                        .inflate(R.layout.date_msg, parent, false)
//                DatePrintViewHolder(DateMsgBinding.bind(view))
//            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (messages[position].senderUid == myUid) {       //레이아웃 항목 초기화
            (holder as MyMessageViewHolder).bind(position)
        } else {
            (holder as OtherMessageViewHolder).bind(position)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class OtherMessageViewHolder(itemView: MessageListOthersBinding) :         //상대 메시지 뷰홀더
        RecyclerView.ViewHolder(itemView.root) {
        var background = itemView.background
        var txtName = itemView.tvName
        var txtColor = itemView.profileBg
        var txtMessage = itemView.tvMessage
        var txtDate = itemView.tvDate
        var txtIsShown = itemView.txtIsShown

        fun bind(position: Int) {           //메시지 UI 항목 초기화
            var message = messages[position]
            var sendDate = message.sended_date

            val opponentReference = FirebaseDatabase.getInstance().getReference("moi")
                .child(message.senderUid)

            opponentReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").value.toString()
                        txtName.text = username

                        val bgColor = snapshot.child("profileColor").value.toString()

                        // bgColor를 기반으로 리소스 ID 동적으로 생성
                        val resourceId = context.resources.getIdentifier(bgColor, "drawable", context.packageName)
                        txtColor.setBackgroundResource(resourceId)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OtherMessageViewHolder", "Error fetching opponent username: ${error.message}")
                }
            })

            txtMessage.text = message.content
            txtDate.text = getDateText(sendDate)

            if (message.confirmed) {
                txtIsShown.visibility = View.GONE
            } else {
                txtIsShown.visibility = View.GONE
//                txtIsShown.visibility = View.VISIBLE
            }

            setShown(position)             //해당 메시지 확인하여 서버로 전송
        }

        fun getDateText(sendDate: String): String {    //메시지 전송 시각 생성

            var dateText = ""
            var timeString = ""
            if (sendDate.isNotBlank()) {
                timeString = sendDate.substring(8, 12)
                var hour = timeString.substring(0, 2)
                var minute = timeString.substring(2, 4)

                var timeformat = "%02d:%02d"

                if (hour.toInt() > 11) {
                    dateText += "오후 "
                    dateText += timeformat.format(hour.toInt() - 12, minute.toInt())
                } else {
                    dateText += "오전 "
                    dateText += timeformat.format(hour.toInt(), minute.toInt())
                }
            }
            return dateText
        }

        fun setShown(position: Int) {          //메시지 확인하여 서버로 전송
            FirebaseDatabase.getInstance().getReference("chat")
                .child(chatRoomKey!!).child("messages")
                .child(messageKeys[position]).child("confirmed").setValue(true)
                .addOnSuccessListener {
                    Log.i("checkShown", "성공")
                }
        }
    }

    inner class MyMessageViewHolder(itemView: MessageListMineBinding) :       // 내 메시지용 ViewHolder
        RecyclerView.ViewHolder(itemView.root) {
        var background = itemView.background
        var txtMessage = itemView.txtMessage
        var txtDate = itemView.txtDate
        var txtIsShown = itemView.txtIsShown

        fun bind(position: Int) {            //메시지 UI 레이아웃 초기화
            var message = messages[position]
            var sendDate = message.sended_date
            txtMessage.text = message.content

            txtDate.text = getDateText(sendDate)

            if (message.confirmed) {
                txtIsShown.visibility = View.GONE
            } else {
                txtIsShown.visibility = View.GONE
//                txtIsShown.visibility = View.VISIBLE
            }
        }

        fun getDateText(sendDate: String): String {        //메시지 전송 시각 생성
            var dateText = ""
            var timeString = ""
            if (sendDate.isNotBlank()) {
                timeString = sendDate.substring(8, 12)
                var hour = timeString.substring(0, 2)
                var minute = timeString.substring(2, 4)

                var timeformat = "%02d:%02d"

                if (hour.toInt() > 11) {
                    dateText += "오후 "
                    dateText += timeformat.format(hour.toInt() - 12, minute.toInt())
                } else {
                    dateText += "오전 "
                    dateText += timeformat.format(hour.toInt(), minute.toInt())
                }
            }
            return dateText
        }
    }

    inner class DatePrintViewHolder(itemView: DateMsgBinding) :
        RecyclerView.ViewHolder(itemView.root){
        private val dateTextView = itemView.dateMsg

        fun bind(position: Int){
            val dateString = messages[position].sended_date ?: ""

            // 날짜 형식: "yyyyMMddHHmmss"
            val year = dateString.substring(0, 4)
            val month = dateString.substring(4, 6)
            val day = dateString.substring(6, 8)

            // 시간 형식: "HHmmss"
            // 시, 분, 초는 필요하지 않기 때문에 추출하지 않음

            // Calendar 객체를 사용하여 요일 계산
            val calendar = Calendar.getInstance()
            calendar.set(year.toInt(), month.toInt() - 1, day.toInt()) // 월은 0부터 시작하므로 -1

            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "일요일"
                Calendar.MONDAY -> "월요일"
                Calendar.TUESDAY -> "화요일"
                Calendar.WEDNESDAY -> "수요일"
                Calendar.THURSDAY -> "목요일"
                Calendar.FRIDAY -> "금요일"
                Calendar.SATURDAY -> "토요일"
                else -> ""
            }

            val dateText = "${year}년 ${month}월 ${day}일 ${dayOfWeek}"
            dateTextView.text = dateText
        }
    }
}