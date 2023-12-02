package com.example.ux

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
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
import com.example.ux.FriendDataManager.fetchFriendDataForUser
import com.example.ux.databinding.FragmentProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.protobuf.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    lateinit var binding: FragmentProfileBinding
    lateinit var mContext: Context
    lateinit var myUid: String
    lateinit var auth: FirebaseAuth

    private lateinit var recyclerView: RecyclerView
    private lateinit var friendlistAdapter: FriendlistAdapter
    private var friendList: MutableList<FriendData> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        //RecyclerView 초기화
        recyclerView = binding.profileFriendRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        //friendList = ProfileColorStringToInt(friendList) //bg11 -> imgResID
        friendlistAdapter = FriendlistAdapter(friendList.toTypedArray()) // Adapter 초기화
        recyclerView.adapter = friendlistAdapter // RecyclerView에 Adapter 설정


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 사용자 정보 가져와서 화면에 설정
//        displayInfo()

        // 사용자 배경색 정보 가져와서 화면에 설정
        displayProfileColor()

        // 정보 수정 버튼 이벤트
//        modifyInfo()

        //


        //로그아웃 버튼 이벤트
//        logoutInfo()

        //회원탈퇴 버튼 이벤트
//        deleteMember()

        // profileImg 버튼 클릭 이벤트 처리
        binding.profileImg.setOnClickListener {
            val intent = Intent(requireContext(), ProfileColorActivity::class.java)
            startActivityForResult(intent, PROFILE_COLOR_REQUEST_CODE)
        }

        // profileBtn 버튼 클릭 이벤트 처리
        binding.profileBtn.setOnClickListener {
            val intent = Intent(requireContext(), ProfileColorActivity::class.java)
            startActivityForResult(intent, PROFILE_COLOR_REQUEST_CODE)
        }

        // 친구 추가 버튼 클릭 이벤트 처리
        binding.addFriendBtn.setOnClickListener {
            openAddFriendDialog()
        }


        fetchFriendDataAndSetAdapter()
    }

    private fun openAddFriendDialog() {

        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.add_friend_dialog, null)
        builder.setView(dialogView)

        val editTextFriendName = dialogView.findViewById<EditText>(R.id.editTxt_add_friend_email)

        builder.setPositiveButton("추가") { dialog, _ ->
            val friendEmail = editTextFriendName.text.toString().trim()
            val usersRef = database.child("moi")
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            val currentUserFriendRef =
                database.child("moi").child(currentUserUid ?: "").child("friend")

            var friendUid = ""
            var isEmailRegistered = false

            // 입력한 이메일이 존재하는 이메일인지 확인
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        val uid = userSnapshot.key // 사용자 uid
                        val email =
                            userSnapshot.child("email").getValue(String::class.java) // 사용자 이메일

                        if (friendEmail == email) {
                            // 사용자 이메일이 존재하는 경우
                            friendUid = uid!!
                            isEmailRegistered = true

                            // 현재 사용자의 친구 목록에 추가된 친구가 있는지 확인
                            currentUserFriendRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val value = snapshot.getValue<Map<String, Boolean>>()

                                    if (value != null && value.containsKey(friendUid)) {
                                        // 이미 친구 목록에 존재하는 경우
                                        System.out.println("이미 추가된 친구")
                                        Toast.makeText(
                                            requireContext(),
                                            "이미 추가된 친구입니다",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        // 친구가 아직 추가되지 않은 경우
                                        addFriendToFirebase(friendUid)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })



                            break
                        }
                    }

                    if (!isEmailRegistered) {
                        // 가입된 이메일이 아닌 경우
                        System.out.println("가입된 이메일이 아님")
                        Toast.makeText(
                            requireContext(),
                            "가입된 이메일이 아닙니다",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    val errorMessage = error.message

                    Toast.makeText(
                        requireContext(),
                        "Database Error: $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("ProfileFragment", "Database Error: $errorMessage")

                }
            })

            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.cancel()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun addFriendToFirebase(friendUid: String) {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        val databaseRef = FirebaseDatabase.getInstance().reference.child("moi")

        val newFriendNameRef = databaseRef.child(friendUid).child("username")


        newFriendNameRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val newFriendName = snapshot.getValue(String::class.java)

                if (newFriendName != null){
                    //현재 사용자의 friend 노드에 친구 추가
                    val currentUserFriendRef = databaseRef.child(currentUserUid ?: "").child("friend")
                    currentUserFriendRef.child(friendUid).setValue(true)
                        .addOnSuccessListener {
                            val newFriendStatusMsg = "status msg" //상태메시지 삭제???

                            // 친구 정보를 friendList에 추가하고 RecyclerView 갱신
                            val newFriend = FriendData(newFriendName,newFriendStatusMsg,"bg11",R.drawable.bg11,friendUid)
                            friendList.add(newFriend)

                            // RecyclerView 어댑터에 변경된 데이ㅂ터를 알림
                            friendlistAdapter = FriendlistAdapter(friendList.toTypedArray())
                            recyclerView.adapter = friendlistAdapter
                            friendlistAdapter.notifyDataSetChanged()

                            System.out.println("친구 추가 성공")
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                "친구 추가 실패: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            System.out.println("친구 추가 실패")
                        }

                    // 친구의 friend 노드에 현재 사용자 추가
                    val friendRef = databaseRef.child(friendUid).child("friend")
                    friendRef.child(currentUserUid ?: "").setValue(true)
                        .addOnSuccessListener {

                            System.out.println("친구에게 나 추가 성공")
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                "친구 정보 업데이트 실패: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "친구 추가 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


    private fun fetchFriendDataAndSetAdapter() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val friendDataList = fetchFriendDataForUser(myUid)
                System.out.println("friendDatalist : "+friendDataList)
                friendList = friendDataList as MutableList<FriendData>
                withContext(Dispatchers.Main) {
                    friendlistAdapter = FriendlistAdapter(friendDataList.toTypedArray())
                    recyclerView.adapter = friendlistAdapter
                }
            } catch (e: Exception) {
                Log.e("ProfileFragment", "Error fetching friend data: ${e.message}")
            }
        }
    }


    companion object {
        private const val PROFILE_COLOR_REQUEST_CODE = 100
        private const val PROFILE_MODIFY_REQUEST_CODE = 101
    }

    //삭제?
    /* private fun ProfileColorStringToInt(friendlist: MutableList<FriendData>): MutableList<FriendData> {
         for (friendData in friendlist) {
             val resourceId = mContext.resources.getIdentifier(
                 friendData.imgName, "drawable", mContext.packageName
             )
             resourceId?.let {
                 friendData.imgResId = it
             }
         }
         return friendlist
     }*/

    private fun displayProfileColor() {
        myUid = FirebaseAuth.getInstance().currentUser?.uid!!

        val db = Firebase.database.getReference("moi")
        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (users in dataSnapshot.children) {
                    val uid = users.child("uid").value.toString()

                    if (myUid == uid) {
                        val profileDB = users.child("profileColor").value.toString()

                        // 리소스 식별자 가져오기
                        val resourceId = mContext.resources.getIdentifier(
                            profileDB, "drawable", mContext.packageName
                        )

                        if (resourceId != 0) {
                            // 기존의 배경 리소스 제거 후 새로운 배경 리소스 설정
                            binding.profileBG.setBackgroundResource(0)
                            binding.profileBG.setBackgroundResource(resourceId)
                        } else {
                            Toast.makeText(
                                mContext,
                                "프로필 배경색 리소스를 찾을 수 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Error handling
            }
        })
    }

    // 프로필 배경 변경 후 다시 프로필 화면으로 돌아감
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == PROFILE_COLOR_REQUEST_CODE && resultCode == Activity.RESULT_OK)) {
            // 사용자 배경색 정보 가져와서 화면에 설정
            displayProfileColor()
            val selectedItemId = data?.getIntExtra("selectedItemId", R.id.third)
            activity?.setResult(
                Activity.RESULT_OK,
                Intent().putExtra("selectedItemId", selectedItemId)
            )
        } else if ((requestCode == PROFILE_MODIFY_REQUEST_CODE && resultCode == Activity.RESULT_OK)) {
            // 사용자 닉네임 정보 가져와서 화면에 설정
//            displayInfo()
            val selectedItemId = data?.getIntExtra("selectedItemId", R.id.third)
            activity?.setResult(
                Activity.RESULT_OK,
                Intent().putExtra("selectedItemId", selectedItemId)
            )
        }
    }
}