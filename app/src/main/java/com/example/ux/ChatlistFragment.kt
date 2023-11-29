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
import com.example.ux.ChatlistData
import com.example.ux.databinding.FragmentChatlistBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatlistFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class ChatlistFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentChatlistBinding // 바인딩 선언

    val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    lateinit var myUid: String

    private lateinit var chatlistRecyclerView: RecyclerView
    private lateinit var chatlistAdapter: ChatlistAdapter
    private lateinit var chatParticipantAdapter: ChatParticipantAdapter
    private var chatList: MutableList<ChatlistData> = mutableListOf()
    private var friendList : MutableList<FriendData> = mutableListOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatlistBinding.inflate(inflater, container, false)
        val view = binding.root

        //RecyclerView 초기화
        chatlistRecyclerView = binding.chatRecyclerView
        chatlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        //chatList = fetchChatListFromFirebase()
        //chatlistAdapter = ChatlistAdapter(chatList.toTypedArray()) // Adapter 초기화
        //chatlistRecyclerView.adapter = chatlistAdapter // RecyclerView에 Adapter 설정

        CoroutineScope(Dispatchers.Main).launch {
            val fetchedChatList = fetchChatListFromFirebase()
            chatlistAdapter = ChatlistAdapter(fetchedChatList.toTypedArray()) // Adapter 초기화
            chatlistRecyclerView.adapter = chatlistAdapter // RecyclerView에 Adapter 설정
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //firebase로부터 받아온 목록 출력
        CoroutineScope(Dispatchers.Main).launch {
            val fetchedChatList = fetchChatListFromFirebase()
            chatlistAdapter = ChatlistAdapter(fetchedChatList.toTypedArray()) // Adapter 초기화
            chatlistRecyclerView.adapter = chatlistAdapter // RecyclerView에 Adapter 설정
        }


        binding.openRoomBtn.setOnClickListener {
            openNewChatRoomDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            val fetchedChatList = fetchChatListFromFirebase()
            chatlistAdapter = ChatlistAdapter(fetchedChatList.toTypedArray()) // Adapter 초기화
            chatlistRecyclerView.adapter = chatlistAdapter // RecyclerView에 Adapter 설정
        }
    }


    private suspend fun fetchChatListFromFirebase(): MutableList<ChatlistData> {
        return suspendCoroutine { continuation ->
            val chatNodeRef = database.child("chat")
            val myUid = FirebaseAuth.getInstance().currentUser?.uid

            chatNodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val retrievedChatList: MutableList<ChatlistData> = mutableListOf()

                    for (snapshot in dataSnapshot.children) {
                        // 채팅방 item 받아오기
                        val chatRoomName = snapshot.key
                        val intRoomColor = R.drawable.bg4
                        val lastMessage = "마지막 메시지"

                        val memberRef = snapshot.child("member").child(myUid.toString())
                        if(memberRef.exists()) {
                            chatRoomName?.let {
                                retrievedChatList.add(ChatlistData(intRoomColor, chatRoomName, lastMessage))
                            }
                        }

                        System.out.println("제발좀 ㅋㅋㅋ"+chatRoomName)
                    }
                    continuation.resume(retrievedChatList)
                    Log.d("ChatlistFragment", "Chat list data retrieved from Firebase")
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    continuation.resumeWithException(Exception("Error fetching chat list data: ${databaseError.message}"))
                }
            })
        }
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

        myUid = FirebaseAuth.getInstance().currentUser?.uid!!



        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fetchedFriendList = FriendDataManager.fetchFriendDataForUser(myUid)
                withContext(Dispatchers.Main) {
                    val chatParticipantAdapter =
                        ChatParticipantAdapter(fetchedFriendList.toTypedArray())
                    chatPpRecyclerView.adapter = chatParticipantAdapter

                    builder.setPositiveButton("확인") { dialog, _ ->
                        val newRoomName = editTxtNewRoomName.text.toString().trim()
                        if (newRoomName.isNotEmpty()) {
                            val newChatMembers = chatParticipantAdapter.getChatMembers()    //선택된 친구정보 가져오기
                            newChatMembers.add(FriendData("냠모밈","status","bg6",R.drawable.bg12,myUid))
                            System.out.println("new chat members : "+newChatMembers)
                            uploadNewChatMembers(newChatMembers, newRoomName)
                            addNewChatRoom(newRoomName)
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
            val membersRef = chatRef.child("member")
            for (member in newChatMembers) {
                member.uid?.let {uid ->
                    membersRef.child(uid).setValue(true)
                }
            }
            Log.d("ChatlistFragment", "Selected chat members uploaded to Firebase")
            System.out.println("firebase uploaded - chat members")
        }
    }

    private fun addNewChatRoom(roomName: String) {
        val newChatRoom = ChatlistData(R.drawable.bg9, roomName, "새로운 메시지")
        chatList.add(0,newChatRoom)
        chatlistAdapter = ChatlistAdapter(chatList.toTypedArray()) // Adapter 갱신
        chatlistRecyclerView.adapter = chatlistAdapter
        chatlistAdapter.notifyDataSetChanged()

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatlistFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}