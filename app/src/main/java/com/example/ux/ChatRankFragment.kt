package com.example.ux

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ux.databinding.FragmentChatRankBinding
import com.example.ux.model.VoteData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.concurrent.atomic.AtomicInteger


class ChatRankFragment : Fragment() {
    private var _binding: FragmentChatRankBinding? = null
    private val binding get() = _binding!!
    private var chatId: String? = null
    private var uid: String? = null
    private var allData : MutableList<VoteData> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatRankBinding.inflate(inflater, container, false)
        chatId = arguments?.getString("chatId")
        uid = arguments?.getString("uid")

        Log.d("chatrank", chatId.toString())

        var currentTime = LocalDateTime.now();

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentTime.format(formatter)

        Log.d("chatrank", "현재 날짜: " + formattedDate)

        var currentweek = week(formattedDate)

        // 초기 어댑터 설정
        val initialAdapter = VoteAdapter(allData)
        binding.rankRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.rankRecyclerView.adapter = initialAdapter

        loadDataFromFirebase(chatId!!,currentweek) //db가져오기
        onUserInteractionChangedData(currentweek)
        // Firebase에서 데이터 로드
        return binding.root

    }
    private fun loadDataFromFirebase(chatId: String, currentweek:Array<String>) {
        val db = Firebase.database.getReference("chat")

        val meetingRef = db.child(chatId).child("meeting")
            .child(currentweek[0]).child(currentweek[1]).child(currentweek[2]).child("rank")
        var currentUserVote: String? = null

        meetingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allData.clear()

                val itemCount = snapshot.childrenCount.toInt()
                var processedCount = 0

                snapshot.children.forEach { itemSnapshot ->
                    val item = itemSnapshot.key ?: return

                    val fixedRef = db.child(chatId).child("meeting")
                        .child(currentweek[0]).child(currentweek[1]).child(currentweek[2])
                        .child("rank").child(item).child("isChecked")

                    fixedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(fixedSnapshot: DataSnapshot) {
                            val isFixed = fixedSnapshot.getValue(Boolean::class.java) ?: false

                            // Check for the user's vote
                            val voteOptions = listOf("agree", "disagree", "yet")
                            var voteCheckedCount = 0

                            for (option in voteOptions) {
                                meetingRef.child(item).child(option).child(uid!!).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        voteCheckedCount++

                                        if (snapshot.exists()) {
                                            currentUserVote = option
                                        }

                                        // Check if all vote options are checked
                                        if (voteCheckedCount == voteOptions.size) {
                                            allData.add(VoteData(chatId, uid!!, currentweek, item, true, isFixed, currentUserVote.toString()))
                                            processedCount++
                                            Log.d("ADDalldata", "====")

                                            if (processedCount == itemCount) {
                                                setupRecyclerView(allData)
                                                Log.d("alldata", allData.size.toString())
                                            }
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // 에러 처리
                                    }
                                })
                            }
                        }

                        override fun onCancelled(fixedError: DatabaseError) {
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun setupRecyclerView(data: List<VoteData>) {
        val adapter = VoteAdapter(data)
        binding.rankRecyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun ButtonFromFirebase(chatId: String, currentweek:Array<String>) {
        val db = Firebase.database.getReference("chat")
        val moidb = Firebase.database.getReference("moi")
        //채팅맴버
        val possibleRef = db.child(chatId).child("member")
        val rankRef = db.child(chatId).child("meeting")
            .child(currentweek[0]).child(currentweek[1]).child(currentweek[2])
        rankRef.setValue(null)
        Log.d("ChatRankFragment", "initialize data")

        val membersListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var members = mutableListOf<String>()
                dataSnapshot.children.forEach { memberSnapshot ->
                    var member = memberSnapshot.key
                    member?.let { members.add(it) }
                }
                Log.d("memberslistsize", members.size.toString())
                var loadCount = AtomicInteger(0)
                var allTimeTables = mutableListOf<String>()

                // 여기서 members 리스트를 사용
                members.forEach { member ->
                    fetchTimeTableForMember(member, moidb, currentweek) { memberTimeTable ->
                        allTimeTables.addAll(memberTimeTable)
                        if (loadCount.incrementAndGet() == members.size) {
                            // 모든 데이터 로드 완료
                            val sortedByFrequency = allTimeTables.groupingBy { it }
                                .eachCount()
                                .toList()
                                .sortedByDescending { it.second }
                            //Log.d("sortedData", sortedByFrequency.size.toString())

                            val topFrequencies = sortedByFrequency.map { it.second }.distinct().take(3)
                            val topItems = sortedByFrequency.filter { it.second in topFrequencies }.toMap()
                            //Log.d("topItems", topItems.size.toString())

                            topItems.forEach { (item, count) ->
                                rankRef.child("rank").child(item).child("count").setValue(count)
                                //Log.d("itemsave", "item save at rank")
                                members.forEach { member ->
                                    rankRef.child("rank").child(item).child("yet").child(member)
                                        .setValue(true)
                                    //Log.d("membersave", "member save at yet")
                                }
                            }
                            loadDataFromFirebase(chatId!!,currentweek)
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // 에러 처리
            }
        }
        possibleRef.addListenerForSingleValueEvent(membersListener)
    }

    fun fetchTimeTableForMember(
        member: String,
        db: DatabaseReference,
        currentweek: Array<String>,
        onTimeTablesFetched: (List<String>) -> Unit
    ) {

        var tempTimeTable= mutableListOf<String>()
        val ref = db.child(member).child("timeTable").child("possible").child(currentweek[0])
            .child(currentweek[1]).child(currentweek[2])
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 해당 요일의 모든 데이터를 가져옵니다.
                snapshot.children.forEach { timeSnapshot ->
                    val time = timeSnapshot.getValue(String::class.java)
                    time?.let { tempTimeTable.add(it) }
                }
                onTimeTablesFetched(tempTimeTable)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // 에러 처리
            }
        })
    }

    fun onUserInteractionChangedData(currentweek: Array<String>) {
        binding.apply {
            var tempweek=currentweek
            nowYear.setText("${tempweek[0]}년")
            nowWeek.setText("${tempweek[1]}월${tempweek[2]}일 ~ ${tempweek[4]}월${tempweek[5]}일")
            lastWeek.setOnClickListener {
                tempweek=changeweek(tempweek,-7)

                // UI 업데이트
                nowYear.setText("${tempweek[0]}년")
                nowWeek.setText("${tempweek[1]}월${tempweek[2]}일 ~ ${tempweek[4]}월${tempweek[5]}일")

                loadDataFromFirebase(chatId!!,tempweek)
            }
            nextWeek.setOnClickListener {
                tempweek=changeweek(tempweek,7)
                // UI 업데이트

                nowYear.setText("${tempweek[0]}년")
                nowWeek.setText("${tempweek[1]}월${tempweek[2]}일 ~ ${tempweek[4]}월${tempweek[5]}일")

                loadDataFromFirebase(chatId!!,tempweek)
            }

            create.setOnClickListener {
                // Firebase에서 데이터 로드
                ButtonFromFirebase(chatId!!,tempweek) //db가져오기
                Log.d("ChatRankFragment", "done initialize data")



            }

        }
    }

    private fun week(eventDate: String): Array<String> {
        val dateArray = eventDate.split("-").toTypedArray()

        val cal = Calendar.getInstance()
        cal[dateArray[0].toInt(), dateArray[1].toInt() - 1] = dateArray[2].toInt()

        // 일주일의 첫날을 일요일
        cal.firstDayOfWeek = Calendar.SUNDAY

        // 시작일과 특정날짜의 차이
        val dayOfWeek = cal[Calendar.DAY_OF_WEEK] - cal.firstDayOfWeek

        // 해당 주차의 첫째날을 지정
        cal.add(Calendar.DAY_OF_MONTH, -dayOfWeek)

        val sf = SimpleDateFormat("yyyy-MM-dd")

        // 해당 주차의 첫째 날짜
        val startDt = sf.format(cal.time)

        cal.add(Calendar.DAY_OF_MONTH, 6)

        val endDt = sf.format(cal.time)

        val startDtArray = startDt.split("-").toTypedArray()
        val endDtArray = endDt.split("-").toTypedArray()

        Log.d("schedule", startDtArray.joinToString(",")+"~"+endDtArray.joinToString(","))

        return startDtArray + endDtArray
    }

    private fun changeweek(tempweek:Array<String>,num:Int):Array<String>{
        // 현재 주의 시작 날짜를 기반으로 계산
        var cal = Calendar.getInstance()
        cal.set(tempweek[0].toInt(), tempweek[1].toInt() - 1, tempweek[2].toInt()) // 현재 주의 시작 날짜 설정
        cal.add(Calendar.DAY_OF_MONTH, num)

        // 새로운 주의 날짜로 tempweek 업데이트
        var newYear = cal.get(Calendar.YEAR).toString()
        var newMonth = (cal.get(Calendar.MONTH) + 1).toString() // 월은 0부터 시작하므로 1 더함
        var newDay = cal.get(Calendar.DAY_OF_MONTH).toString()

        // tempweek 업데이트
        tempweek[0] = newYear
        tempweek[1] = newMonth
        tempweek[2] = newDay

        cal.set(tempweek[3].toInt(), tempweek[4].toInt() - 1, tempweek[5].toInt()) // 현재 주의 시작 날짜 설정
        cal.add(Calendar.DAY_OF_MONTH, num)

        newYear = cal.get(Calendar.YEAR).toString()
        newMonth = (cal.get(Calendar.MONTH) + 1).toString() // 월은 0부터 시작하므로 1 더함
        newDay = cal.get(Calendar.DAY_OF_MONTH).toString()

        tempweek[3] = newYear
        tempweek[4] = newMonth
        tempweek[5] = newDay

        Log.d("schedule", tempweek.joinToString(","))

        return tempweek
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}