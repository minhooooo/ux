package com.example.ux

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.ux.databinding.FragmentScheduleBinding
import com.google.firebase.auth.FirebaseAuth
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


class ScheduleFragment : Fragment() {
    lateinit var mContext: Context

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private var uid: String? = null
    private var clickedTextViewIds: MutableList<String> = mutableListOf()
    private var fixList: MutableMap<String, Int> = mutableMapOf()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        uid = FirebaseAuth.getInstance().currentUser?.uid!!
        Log.d("schedule", uid.toString())

        var currentTime = LocalDateTime.now();

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentTime.format(formatter)

        Log.d("dateTest", "현재 날짜: " + formattedDate)

        var currentweek = week(formattedDate)

        // Firebase에서 데이터 로드
        loadDataFromFirebase(uid!!,currentweek)
        onUserInteractionChangedData(currentweek)
        return binding.root

    }


    private fun loadDataFromFirebase(uid: String, currentweek:Array<String>) {
        val db = Firebase.database.getReference("moi")

        val chatRoomRef = db.child(uid).child("chatRoom")
        val chatdb = Firebase.database.getReference("chat")
        var chatList: MutableList<String> = mutableListOf()

        chatRoomRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { childSnapshot ->
                    val key = childSnapshot.key
                    if (key != null) {
                        Log.d("statusChatRoom",key)
                        chatList.add(key)
                        Log.d("statusLoadDataFromFireBase",chatList.size.toString())

                    }
                }
                processChatList(chatList, uid, currentweek, chatdb, db)

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun processChatList(chatList: MutableList<String>, uid: String, currentweek: Array<String>, chatdb: DatabaseReference, db: DatabaseReference) {
        val processCount = AtomicInteger(chatList.size) // 처리할 항목 수

        Log.d("statusprocessCount",processCount.toString())
        Log.d("statuschatList",chatList.size.toString())

        chatList.forEach { chatId ->
            chatdb.child(chatId).child("chatColor")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    var chatColor: String? = null

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val chatColor = snapshot.getValue(String::class.java)
                        if (chatColor != null) {
                            Log.d("statuschatColor", chatColor)
                        }

                        chatColor?.let { color ->
                            var resourceId = mContext.resources.getIdentifier(
                                color, "color", mContext.packageName
                            )
                            if (resourceId == 0) {
                                resourceId = mContext.resources.getIdentifier(
                                    "bg6", "color", mContext.packageName
                                )
                            }
                            chatdb.child(chatId).child("meeting")
                                .child(currentweek[0]).child(currentweek[1]).child(currentweek[2]).child("fix")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        snapshot.children.forEach { childSnapshot ->
                                            val key = childSnapshot.key
                                            if (key != null) {
                                                fixList[key] = resourceId
                                            }
                                        }
                                        Log.d("statusfixList",fixList.size.toString())

                                        if (processCount.decrementAndGet() == 0) {
                                            // 마지막 chatId 처리가 완료되면, clickedTextViewIds를 채움
                                            Log.d("status",clickedTextViewIds.size.toString()+"  "+fixList.size.toString())
                                            loadClickedTextViewIds(uid, currentweek, db)
                                        }
                                    }
                                    override fun onCancelled(databaseError: DatabaseError) {
                                        // 오류 처리
                                    }
                                })
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        // 오류 처리
                    }
                })
        }
    }
    private fun loadClickedTextViewIds(uid: String, currentweek: Array<String>, db: DatabaseReference) {
        val possibleRef = db.child(uid).child("timeTable").child("possible")
            .child(currentweek[0]).child(currentweek[1]).child(currentweek[2])

        possibleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { childSnapshot ->
                    childSnapshot.getValue(String::class.java)?.let { stringValue ->
                        clickedTextViewIds.add(stringValue)
                    }
                }
                // UI 업데이트
                updateUIWithLoadedData()
            }

            override fun onCancelled(error: DatabaseError) {
                // 오류 처리
            }
        })
    }
    private fun updateUIWithLoadedData() {
        clickedTextViewIds.forEach { textViewIdName ->
            val resId = resources.getIdentifier(textViewIdName, "id", requireContext().packageName)
            val textView = binding.root.findViewById<TextView>(resId)
            textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_selected)
            textView.setTag("cell_selected") // 태그 설정
        }
        fixList.forEach { textViewIdName ->
            val resId = resources.getIdentifier(textViewIdName.key, "id", requireContext().packageName)
            val textView = binding.root.findViewById<TextView>(resId)
            textView.background = ContextCompat.getDrawable(requireContext(), textViewIdName.value)
            textView.setTag("cell_fixed") // 태그 설정
        }
    }

    // 사용자 인터랙션에 의한 데이터 변경
    fun onUserInteractionChangedData(currentweek: Array<String>) {
        binding.apply {
            var tempweek=currentweek
            nowYear.setText("${tempweek[0]}년")
            nowWeek.setText("${tempweek[1]}월${tempweek[2]}일 ~ ${tempweek[4]}월${tempweek[5]}일")
            lastWeek.setOnClickListener {
                clickedTextViewIds.forEach { textViewIdName ->
                    val resId = resources.getIdentifier(textViewIdName, "id", requireContext().packageName)
                    val textView = binding.root.findViewById<TextView>(resId)
                    textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_normal)
                    textView.setTag("cell_normal") // 태그 설정
                }
                clickedTextViewIds.clear()
                fixList.forEach { textViewIdName ->
                    val resId = resources.getIdentifier(textViewIdName.key, "id", requireContext().packageName)
                    val textView = binding.root.findViewById<TextView>(resId)
                    textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_normal)
                    textView.setTag("cell_normal") // 태그 설정
                }
                fixList.clear()

                tempweek=changeweek(tempweek,-7)

                // UI 업데이트
                nowYear.setText("${tempweek[0]}년")
                nowWeek.setText("${tempweek[1]}월${tempweek[2]}일 ~ ${tempweek[4]}월${tempweek[5]}일")

                loadDataFromFirebase(uid!!,tempweek)
            }
            nextWeek.setOnClickListener {
                clickedTextViewIds.forEach { textViewIdName ->
                    val resId = resources.getIdentifier(textViewIdName, "id", requireContext().packageName)
                    val textView = binding.root.findViewById<TextView>(resId)
                    textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_normal)
                    textView.setTag("cell_normal") // 태그 설정
                }
                clickedTextViewIds.clear()
                fixList.forEach { textViewIdName ->
                    val resId = resources.getIdentifier(textViewIdName.key, "id", requireContext().packageName)
                    val textView = binding.root.findViewById<TextView>(resId)
                    textView.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_normal)
                    textView.setTag("cell_normal") // 태그 설정
                }
                fixList.clear()

                tempweek=changeweek(tempweek,7)
                // UI 업데이트
                nowYear.setText("${tempweek[0]}년")
                nowWeek.setText("${tempweek[1]}월${tempweek[2]}일 ~ ${tempweek[4]}월${tempweek[5]}일")

                loadDataFromFirebase(uid!!,tempweek)
            }

            var isCreate = true

            create.setOnClickListener {
                if (isCreate) {
                    // 현재 'create' 상태인 경우, 'apply' 상태로 변경
                    create.text = "적용"
                    create.setBackgroundResource(R.drawable.btn_circle_blue) // 'apply' 상태의 배경
                    create.setTextColor(Color.WHITE) // 'apply' 상태의 텍스트 색상

                    //gridlayout에 반응 추가
                    for (i in 0 until timeTableView.childCount) {
                        val child = timeTableView.getChildAt(i)
                        val column = child.tag.toString().toIntOrNull()

                        if (child is TextView && column != 0) {
                            child.setOnClickListener {
                                if (!isCreate){
                                    val resourceName = resources.getResourceEntryName(child.id)

                                    if (child.tag?.toString() == "cell_selected") {
                                        child.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_normal)
                                        child.setTag("cell_normal")
                                        clickedTextViewIds.remove(resourceName)
                                    }
                                    else if (child.tag?.toString() == "cell_fixed"){

                                    }

                                    else{
                                        child.background = ContextCompat.getDrawable(requireContext(), R.drawable.cell_selected)
                                        child.setTag("cell_selected")
                                        clickedTextViewIds.add(resourceName)
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    // 현재 'apply' 상태인 경우, 'create' 상태로 변경
                    create.text = "수정"
                    create.setBackgroundResource(R.drawable.btn_circle_gray) // 'create' 상태의 배경
                    create.setTextColor(Color.parseColor("#787878")) // 'create' 상태의 텍스트 색상

                    //gridlayout에 색이 바뀐부분 db저장
                    saveDataToFirebase(currentweek)
                }
                isCreate = !isCreate // 상태 토글
            }
        }
    }
    private fun saveDataToFirebase(currentweek: Array<String>) {

        if (isNetworkAvailable()) {
            val db = Firebase.database.getReference("moi")
            val possibleRef = db.child(uid!!).child("timeTable").child("possible")
                .child(currentweek[0]).child(currentweek[1]).child(currentweek[2])
            possibleRef.setValue(clickedTextViewIds)
        } else {
            // TODO: 네트워크가 사용 불가능할 때
        }
    }

    // 네트워크 상태 체크
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        return false
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
        var newMonth = String.format("%02d", cal.get(Calendar.MONTH) + 1)  // 월은 0부터 시작하므로 1 더함
        var newDay = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))

        // tempweek 업데이트
        tempweek[0] = newYear
        tempweek[1] = newMonth
        tempweek[2] = newDay

        cal.set(tempweek[3].toInt(), tempweek[4].toInt() - 1, tempweek[5].toInt()) // 현재 주의 시작 날짜 설정
        cal.add(Calendar.DAY_OF_MONTH, num)

        newYear = cal.get(Calendar.YEAR).toString()
        newMonth = String.format("%02d", cal.get(Calendar.MONTH) + 1) // 월은 0부터 시작하므로 1 더함
        newDay = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))

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