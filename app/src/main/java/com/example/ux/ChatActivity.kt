package com.example.ux

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.databinding.ActivityChatBinding
import com.example.ux.model.ChatRoom
import com.example.ux.model.Message
import com.example.ux.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding
    lateinit var btn_exit: ImageButton
    lateinit var btn_submit: ImageButton
    lateinit var txt_title: TextView
    lateinit var edt_message: EditText
    lateinit var firebaseDatabase: DatabaseReference
    lateinit var recycler_talks: RecyclerView
    lateinit var chatRoom: ChatRoom
    lateinit var opponentUser: User
    lateinit var chatRoomKey: String
    lateinit var myUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeProperty()
        initializeView()
        initializeListener()
        setupChatRooms()
        moveSchedule()
    }

    private fun moveSchedule() {
        binding.chatroomMenu.setOnClickListener {
//            val intent = Intent(this@ChatActivity, ChatMainActivity::class.java)
//
//            intent.putExtra("chatName", chatRoom.chatName)
//            startActivity(intent)
        }
    }

    private fun initializeProperty() {  //변수 초기화
        myUid = FirebaseAuth.getInstance().currentUser?.uid!!              //현재 로그인한 유저 id
        firebaseDatabase = FirebaseDatabase.getInstance().reference!!

        chatRoom = (intent.getSerializableExtra("ChatRoom")) as ChatRoom      //채팅방 정보
        chatRoomKey = intent.getStringExtra("ChatRoomKey")!!            //채팅방 키
        opponentUser = (intent.getSerializableExtra("Opponent")) as User    //상대방 유저 정보
    }

    private fun initializeView() {    //뷰 초기화
        btn_exit = binding.imgbtnQuit
        edt_message = binding.message
        recycler_talks = binding.recyclerMessages
        btn_submit = binding.btnSubmit
        txt_title = binding.txtTItle
        txt_title.text = chatRoomKey
    }

    private fun initializeListener() {   //버튼 클릭 시 리스너 초기화
        btn_exit.setOnClickListener()
        {
            startActivity(Intent(this@ChatActivity, MainActivity::class.java))
        }
        btn_submit.setOnClickListener()
        {
            putMessage()
        }
    }

    private fun setupChatRooms() {              //채팅방 목록 초기화 및 표시
        if (chatRoomKey.isNullOrBlank())
            setupChatRoomKey()
        else
            setupRecycler()
    }

    private fun setupChatRoomKey() {            //chatRoomKey 없을 경우 초기화 후 목록 초기화
        FirebaseDatabase.getInstance().getReference("chat")
            .orderByChild("member/${opponentUser.uid}").equalTo(true)    //상대방의 Uid가 포함된 목록이 있는지 확인
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        chatRoomKey = data.key!!          //chatRoomKey 초기화
                        setupRecycler()                  //목록 업데이트
                        break
                    }
                }
            })
    }

    private fun putMessage() {       //메시지 전송
        try {
            var message = Message(myUid, getDateTimeString(), edt_message.text.toString())    //메시지 정보 초기화
            Log.i("ChatRoomKey", chatRoomKey)
            FirebaseDatabase.getInstance().getReference("chat")
                .child(chatRoomKey).child("messages")                   //현재 채팅방에 메시지 추가
                .push().setValue(message).addOnSuccessListener {
                    Log.i("putMessage", "메시지 전송에 성공하였습니다.")
                    edt_message.text.clear()
                }.addOnCanceledListener {
                    Log.i("putMessage", "메시지 전송에 실패하였습니다")
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("putMessage", "메시지 전송 중 오류가 발생하였습니다.")
        }
    }

    private fun getDateTimeString(): String {          //메시지 보낸 시각 정보 반환
        try {
            var localDateTime = LocalDateTime.now()
            localDateTime.atZone(TimeZone.getDefault().toZoneId())
            var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            return localDateTime.format(dateTimeFormatter).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("getTimeError")
        }
    }

    fun setupRecycler() {            //목록 초기화 및 업데이트
        recycler_talks.layoutManager = LinearLayoutManager(this)
        recycler_talks.adapter = RecyclerMessagesAdapter(this, chatRoomKey, opponentUser.uid)
    }
}