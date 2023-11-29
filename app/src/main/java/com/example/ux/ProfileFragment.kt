package com.example.ux

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ux.FriendDataManager.fetchFriendDataForUser
import com.example.ux.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    lateinit var binding: FragmentProfileBinding
    lateinit var mContext: Context
    lateinit var myUid: String

    private lateinit var recyclerView: RecyclerView
    private lateinit var friendlistAdapter: FriendlistAdapter
    private var friendList : MutableList<FriendData> = mutableListOf()

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
        fetchFriendDataAndSetAdapter()
    }

    private fun fetchFriendDataAndSetAdapter() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val friendDataList = fetchFriendDataForUser(myUid)
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
            activity?.setResult(Activity.RESULT_OK, Intent().putExtra("selectedItemId", selectedItemId))
        } else if ((requestCode == PROFILE_MODIFY_REQUEST_CODE && resultCode == Activity.RESULT_OK)) {
            // 사용자 닉네임 정보 가져와서 화면에 설정
//            displayInfo()
            val selectedItemId = data?.getIntExtra("selectedItemId", R.id.third)
            activity?.setResult(Activity.RESULT_OK, Intent().putExtra("selectedItemId", selectedItemId))
        }
    }
}