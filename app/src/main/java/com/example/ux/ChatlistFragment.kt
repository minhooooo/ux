package com.example.ux

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.FriendDataManager.database
import com.example.ux.databinding.FragmentChatlistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class ChatlistFragment : Fragment() {

    private lateinit var binding: FragmentChatlistBinding // 바인딩 선언

    lateinit var firebaseDatabase: DatabaseReference
    lateinit var recycler_chatroom: RecyclerView

    private val myUid = FirebaseAuth.getInstance().currentUser?.uid.toString()   //현재 사용자 Uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatlistBinding.inflate(inflater, container, false)
        val view = binding.root

        initializeView()
        setupRecycler()

        binding.openRoomBtn.setOnClickListener {
            openNewChatRoomDialog()
        }

        return view
    }

    private fun initializeView() { //뷰 초기화
        try {
            firebaseDatabase = FirebaseDatabase.getInstance().getReference("chat")!!
            recycler_chatroom = binding.chatRecyclerView
        }catch (e:Exception)
        {
            e.printStackTrace()
            Toast.makeText(requireContext(),"화면 초기화 중 오류가 발생하였습니다.",Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecycler() {
        recycler_chatroom.layoutManager = LinearLayoutManager(requireContext())
        recycler_chatroom.adapter = RecyclerChatRoomsAdapter(requireContext())
    }

    //채팅방 개설 팝업창
    private fun openNewChatRoomDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.open_new_chatroom_dialog, null)
        builder.setView(dialogView)

        val editTxtNewRoomName = dialogView.findViewById<EditText>(R.id.editTxt_new_room_name)
        val chatPpRecyclerView =
            dialogView.findViewById<RecyclerView>(R.id.new_room_friend_recycler_view)
        chatPpRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fetchedFriendList = FriendDataManager.fetchFriendDataForUser(myUid)
                System.out.println("fetched friend list = " + fetchedFriendList)
                withContext(Dispatchers.Main) {
                    val chatParticipantAdapter =
                        ChatParticipantAdapter(requireContext(), fetchedFriendList.toTypedArray())
                    chatPpRecyclerView.adapter = chatParticipantAdapter

                    builder.setPositiveButton("확인") { dialog, _ ->
                        val newRoomName = editTxtNewRoomName.text.toString().trim()
                        if (newRoomName.isNotEmpty()) {
                            val newChatMembers = chatParticipantAdapter.getChatMembers()    //선택된 친구정보 가져오기
                            newChatMembers.add(FriendData("thisUserName","thisUniv","bg1",R.drawable.bg12,myUid)) // add this user
                            //사실상 uploadNewChatMembers에서 uid만 뽑아다 쓰기 때문에 나머지는 dummy data 삽입
                            System.out.println("new chat members : "+newChatMembers)
                            uploadNewChatMembers(newChatMembers, newRoomName)
//                            addNewChatRoom(newRoomName)
                        } else {
                            Toast.makeText(requireContext(), "채팅방 이름을 입력하세요", Toast.LENGTH_SHORT)
                                .show()
                        }
                        dialog.dismiss()
                    }

                    builder.setNegativeButton("취소") { dialog, _ ->
                        dialog.cancel()
                    }

                    val alertDialog = builder.create()
                    alertDialog.show()
                }
            } catch (e: Exception) {
                Log.e("ChatlistFragment", "Error fetching friend data: ${e.message}")
            }
        }
    }

    private fun uploadNewChatMembers(newChatMembers: List<FriendData>, roomName: String) {
        myUid?.let {
            val chatRef = database.child("chat").child(roomName)
            chatRef.child("chatColor").setValue("bg2")
            chatRef.child("chatName").setValue(roomName)

            val membersRef = chatRef.child("member")
            for (member in newChatMembers) {
                member.uid?.let {uid ->
                    membersRef.child(uid).setValue(true)
                }
            }

            val randomBackgroundColor = getRandomBackgroundColor()
            chatRef.child("chatColor").setValue(randomBackgroundColor)

            val chatroomRef = database.child("moi").child(myUid).child("chatRoom")
            chatroomRef.child(roomName).setValue(true)

            Log.d("ChatlistFragment", "Selected chat members uploaded to Firebase")

            // 방을 업로드한 후 방 목록을 다시 불러옴
            (recycler_chatroom.adapter as? RecyclerChatRoomsAdapter)?.setupAllUserList()
        }
    }

    private fun getRandomBackgroundColor(): String {
        val backgroundColors = listOf(
            "bg1", "bg2", "bg3", "bg4", "bg5",
            "bg6", "bg7", "bg8", "bg9", "bg10",
            "bg11", "bg12"
        )

        val random = Random()
        val randomIndex = random.nextInt(backgroundColors.size)

        return backgroundColors[randomIndex]
    }
}